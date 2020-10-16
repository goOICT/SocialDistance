import Foundation
import CoreBluetooth
import AudioToolbox

public enum SocialDistanceSdkConstants: String {
    case IOS_SERVICE_UUID =     "00086f9a-264e-3ac6-838a-000000000000"
    case ANDROID_SERVICE_UUID = "d2b86f9a-264e-3ac6-838a-0d00c1f549ed"
    case DEFAULTS_UUID_KEY = "Device UUID"
    case TEAMS_KEY = "teamsKey"
}

public extension CBUUID {
    static let androidPrefix = String(SocialDistanceSdkConstants.ANDROID_SERVICE_UUID.rawValue.prefix(7))
    static let iosPrefix = String(SocialDistanceSdkConstants.IOS_SERVICE_UUID.rawValue.prefix(7))
    
    var hasSocialDistancePrefix: Bool {
        let lowercased = uuidString.lowercased()
        return lowercased.hasPrefix(CBUUID.iosPrefix) || lowercased.hasPrefix(CBUUID.androidPrefix)
    }
    
    var isAndroid: Bool {
        uuidString.lowercased().hasPrefix(CBUUID.androidPrefix)
    }
}

@objcMembers
public class DiscoverdDevice: NSObject {
    public let uuid: String
    public let isAndroid: Bool
    public var rssi: NSNumber
    public var txPower: NSNumber?
    
    init(CBUUID: CBUUID, rssi: NSNumber, txPower: NSNumber?) {
        self.uuid = CBUUID.uuidString
        self.isAndroid = CBUUID.isAndroid
        self.rssi = rssi
        self.txPower = txPower
    }
}

@objc
public protocol BluetoothManagerDelegate: AnyObject {
    func peripheralsDidUpdate()
    func advertisingStarted()
    func scanningStarted()
    func didDiscoverPeripheral(ids: [DiscoverdDevice])
}

public protocol BluetoothManager {
    var peripherals: Dictionary<UUID, CBPeripheral> { get }
    var delegate: BluetoothManagerDelegate? { get set }
    var uuidString: String { get set }
    func pause(_ with: Bool)
    func startAdvertising()
    func startScanning()
}

func getNewUniqueId() -> String {
    let stringChars = "0123456789abcdef"
    let postfix = String((0...11).map{ _ in stringChars.randomElement()! })
    return SocialDistanceSdkConstants.IOS_SERVICE_UUID.rawValue.replacingOccurrences(of: "000000000000", with: postfix)
}


@objcMembers
/// The core BLE manager.  Originally this was just part of the app, but we broke it out so that it can be used as a lib via React or used without
/// changes in your own app
public class CoreBluetoothManager: NSObject, BluetoothManager {
    
    /// get the UUID string making a new one if needed
    public var uuidString: String =  {
        let currentId = UserDefaults.standard.string(forKey: SocialDistanceSdkConstants.DEFAULTS_UUID_KEY.rawValue)
        if (currentId == nil) {
            let newId = getNewUniqueId()
            UserDefaults.standard.set(newId, forKey: SocialDistanceSdkConstants.DEFAULTS_UUID_KEY.rawValue)
            return newId
        } else {
            return currentId!
        }
    }()
    
    /// reset the UUID string
    public func resetUuidString(){
        let newId = getNewUniqueId()
        uuidString = newId
        UserDefaults.standard.set(newId, forKey: SocialDistanceSdkConstants.DEFAULTS_UUID_KEY.rawValue)
    }
    
    let androidPrefix = String(SocialDistanceSdkConstants.ANDROID_SERVICE_UUID.rawValue.prefix(7))
    let iosPrefix = String(SocialDistanceSdkConstants.IOS_SERVICE_UUID.rawValue.prefix(7))
    
    public static let sharedInstance: CoreBluetoothManager = {
        let instance = CoreBluetoothManager()
        // setup code
        return instance
    }()
    
    /// Pause scanning and broadcasting and let the user know that it's happening with a series of 5 vibrations
    /// - Parameter with: True to pause
    public func pause(_ with: Bool) {
        self.isPaused = with
        
        if (isPaused) {
            // Make sure the user knows we are paused...
            AudioServicesPlayAlertSoundWithCompletion(SystemSoundID(kSystemSoundID_Vibrate)) {
                AudioServicesPlayAlertSoundWithCompletion(SystemSoundID(kSystemSoundID_Vibrate)) {
                    AudioServicesPlayAlertSoundWithCompletion(SystemSoundID(kSystemSoundID_Vibrate)) {
                        AudioServicesPlayAlertSoundWithCompletion(SystemSoundID(kSystemSoundID_Vibrate)) {
                            AudioServicesPlayAlertSoundWithCompletion(SystemSoundID(kSystemSoundID_Vibrate)) {}
                        }
                    }
                }
            }
            print("Pausing...")
            peripheralManager?.stopAdvertising()
            peripheralManager?.delegate = nil
            centralManager?.delegate = nil
            centralManager?.stopScan()
        }
    }
    
    // MARK: - Public properties
    public var isPaused: Bool = true
    weak public var delegate: BluetoothManagerDelegate?
    private(set) public var peripherals = Dictionary<UUID, CBPeripheral>() {
        didSet {
            delegate?.peripheralsDidUpdate()
        }
    }

    // MARK: - Public methods
    /// Start BLE advertising so that other apps can detect this one
    public func startAdvertising() {
        isPaused = false
        peripheralManager = CBPeripheralManager(delegate: self, queue: nil,
                                                options: nil)
    }
    
    /// Start BLE scanning for other devices with the app running on them
    public func startScanning() {
        isPaused = false
        centralManager = CBCentralManager(delegate: self, queue: nil,
            options: nil)

    }

    // MARK: - Private properties
    private var peripheralManager: CBPeripheralManager?
    private var centralManager: CBCentralManager?
    private var name: String?
    private var discoveredDevices: Dictionary<CBUUID, DiscoverdDevice> = [:]
}

extension CoreBluetoothManager: CBPeripheralManagerDelegate {
    /// Start advertising
    /// - Parameter peripheral: The BLE peripheral
    public func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        if peripheral.state == .poweredOn {
            if peripheral.isAdvertising {
                peripheral.stopAdvertising()
            }

            let uuid = CBUUID(string: uuidString)
            var advertisingData: [String : Any] = [
                CBAdvertisementDataLocalNameKey: "social-distance-alarm",
                CBAdvertisementDataServiceUUIDsKey: [uuid]
            ]
                advertisingData[CBAdvertisementDataLocalNameKey] = name
            self.broadcastToApps(peripheralManager: peripheralManager!, advertisingData: advertisingData)
            
        } else {
            #warning("handle other states")
        }
    }
    
    /// Start broadcasting.  We use a timer to stop the broadcast so that it can be done at intervals which you could use to preserve battery.  In our testing
    ///  the app didn't really have much effect on batter life on Android or iOS, but the code matches the code written for Android and allows the broadcast
    ///  and scan intervals to change.
    ///
    /// - Parameters:
    ///   - peripheralManager: The peripheral
    ///   - advertisingData: advertising data
    func broadcastToApps(peripheralManager: CBPeripheralManager, advertisingData: [String : Any]) {
        // Default to the packaged repository if none was present...
        if (delegate == nil) { delegate = DeviceRepository.sharedInstance }
        
        if (peripheralManager.isAdvertising) {
            peripheralManager.stopAdvertising()
        }

        if (!isPaused) {
            let uuid = CBUUID(string: uuidString)
            var advertisingData: [String : Any] = [
                CBAdvertisementDataLocalNameKey: "social-distance-alarm",
                CBAdvertisementDataServiceUUIDsKey: [uuid]
            ]

            advertisingData[CBAdvertisementDataLocalNameKey] = "SDAlarm"
        
            delegate?.advertisingStarted()
            peripheralManager.startAdvertising(advertisingData)

            Timer.scheduledTimer(withTimeInterval: 5, repeats: false) { [weak self] _ in
                self?.broadcastToApps(peripheralManager: peripheralManager, advertisingData: advertisingData)
            }
        }
    }
}

extension CoreBluetoothManager: CBCentralManagerDelegate {
    
    /// Called when the state of the central manager changes.  This is where we set up scanning.
    /// - Parameter central: The central manager
    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn {
            scanForApps(central: central)
        } else {
            #warning("Error handling")
        }
    }
    
    /// Set up scaning for other apps
    /// - Parameter central: The central manager
    func scanForApps(central: CBCentralManager) {
        // Default to the packaged repository if none was present...
        if (delegate == nil) { delegate = DeviceRepository.sharedInstance }

        if central.isScanning {
            central.stopScan()
            
            // Deliver discovered devices
            delegate?.didDiscoverPeripheral(ids: Array(discoveredDevices.values))
        }

        if (!isPaused) {
            discoveredDevices = [:]
            delegate?.scanningStarted()
            central.scanForPeripherals(withServices: nil)

            Timer.scheduledTimer(withTimeInterval: SdkConstants.traceInterval, repeats: false) { [weak self] _ in
                self?.scanForApps(central: central)
            }
        }
    }
    
    /// This is the function that gets called when there are devices detected.  We filter the devices for ones that start with the app's UUID (Android or iOS).  If a device
    /// was previously detected we use a rolling average over the scan period to calcualte the signal strength for the device.
    ///
    /// - Parameters:
    ///   - central: The Central manager
    ///   - peripheral: The peripheral
    ///   - advertisementData: The advertisement data
    ///   - RSSI: The relative signal strength
    public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String: Any], rssi RSSI: NSNumber) {
        peripherals[peripheral.identifier] = peripheral
        let uuids = advertisementData[CBAdvertisementDataServiceUUIDsKey] as? [CBUUID]
        let txPowerLevel = advertisementData[CBAdvertisementDataTxPowerLevelKey] as? NSNumber
        
        guard let ids = uuids, !ids.filter({ (cbUid) -> Bool in
            return cbUid.hasSocialDistancePrefix
        }).isEmpty else { return }
        
        // Check to see if the device was already present and calculate a rolling average...
        for id in ids {
            let currentDevice = discoveredDevices[id]
            if (currentDevice == nil) {
                discoveredDevices[id] = DiscoverdDevice(CBUUID: id, rssi: RSSI, txPower: txPowerLevel)
            }
            else {
                discoveredDevices[id]?.rssi = NSNumber(value: (currentDevice!.rssi.intValue + RSSI.intValue) / 2)
            }
        }
    }
}


