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
public class DeviceId: NSObject {
    public let uuid: String
    public let isAndroid: Bool
    
    init(CBUUID: CBUUID) {
        self.uuid = CBUUID.uuidString
        self.isAndroid = CBUUID.isAndroid
    }
}

@objc
public protocol BluetoothManagerDelegate: AnyObject {
    func peripheralsDidUpdate()
    func advertisingStarted()
    func scanningStarted()
    func didDiscoverPeripheral(ids: [DeviceId], rssi: NSNumber, txPower: NSNumber?)
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
public class CoreBluetoothManager: NSObject, BluetoothManager {
    
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
        }
    }
    
    // MARK: - Public properties
    var isPaused: Bool = false
    weak public var delegate: BluetoothManagerDelegate?
    private(set) public var peripherals = Dictionary<UUID, CBPeripheral>() {
        didSet {
            delegate?.peripheralsDidUpdate()
        }
    }

    // MARK: - Public methods
    public func startAdvertising() {
        isPaused = false
        peripheralManager = CBPeripheralManager(delegate: self, queue: nil,
                                                options: nil)
    }

    public func startScanning() {
        isPaused = false
        centralManager = CBCentralManager(delegate: self, queue: nil,
            options: nil)

    }

    // MARK: - Private properties
    private var peripheralManager: CBPeripheralManager?
    private var centralManager: CBCentralManager?
    private var name: String?
}

extension CoreBluetoothManager: CBPeripheralManagerDelegate {
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
    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn {
            scanForApps(central: central)
        } else {
            #warning("Error handling")
        }
    }
    
    func scanForApps(central: CBCentralManager) {
        // Default to the packaged repository if none was present...
        if (delegate == nil) { delegate = DeviceRepository.sharedInstance }

        if central.isScanning {
            central.stopScan()
        }

        if (!isPaused) {
            delegate?.scanningStarted()
            central.scanForPeripherals(withServices: nil)

            Timer.scheduledTimer(withTimeInterval: SdkConstants.traceInterval, repeats: false) { [weak self] _ in
                self?.scanForApps(central: central)
            }
        }
    }

    public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String: Any], rssi RSSI: NSNumber) {
        peripherals[peripheral.identifier] = peripheral
        let uuids = advertisementData[CBAdvertisementDataServiceUUIDsKey] as? [CBUUID]
        let txPowerLevel = advertisementData[CBAdvertisementDataTxPowerLevelKey] as? NSNumber
        
        guard let ids = uuids, !ids.filter({ (cbUid) -> Bool in
            return cbUid.hasSocialDistancePrefix
        }).isEmpty else { return }
    
        delegate?.didDiscoverPeripheral(ids: ids.map(DeviceId.init), rssi: RSSI, txPower: txPowerLevel)
    }
}


