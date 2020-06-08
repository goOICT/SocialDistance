import Foundation
import CoreBluetooth
import AudioToolbox

enum Constants: String {
    case IOS_SERVICE_UUID =     "00086f9a-264e-3ac6-838a-000000000000"
    case ANDROID_SERVICE_UUID = "d2b86f9a-264e-3ac6-838a-0d00c1f549ed"
    case CENTRAL_MANAGER_ID = "ai.kun.socialdistancealarm.central"
    case PERIPHERAL_MANAGER_ID = "ai.kun.socialdistancealarm.peripheral"
    case DEFAULTS_UUID_KEY = "Device UUID"
    case TEAM_UUIDS_KEY = "Team UUIDs"
}

@objc
public protocol BluetoothManagerDelegate: AnyObject {
    func peripheralsDidUpdate()
    func advertisingStarted()
    func scanningStarted()
    func didDiscoverPeripheral(uuid: String, rssi: NSNumber, txPower: NSNumber?, isAndroid: Bool)
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
    return Constants.IOS_SERVICE_UUID.rawValue.replacingOccurrences(of: "000000000000", with: postfix)
}

@objcMembers
public class CoreBluetoothManager: NSObject, BluetoothManager {
    
    public var uuidString: String =  {
        let currentId = UserDefaults.standard.string(forKey: Constants.DEFAULTS_UUID_KEY.rawValue)
        if (currentId == nil) {
           let newId = getNewUniqueId()
            UserDefaults.standard.set(newId, forKey: Constants.DEFAULTS_UUID_KEY.rawValue)
            return newId
        } else {
            return currentId!
        }
    }()
    
    
    let androidPrefix = String(Constants.ANDROID_SERVICE_UUID.rawValue.prefix(7))
    let iosPrefix = String(Constants.IOS_SERVICE_UUID.rawValue.prefix(7))
    
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
                                                options: [CBCentralManagerOptionRestoreIdentifierKey: "ai.kun.socialdistancealarm.peripheral"])
    }

    public func startScanning() {
        isPaused = false
        centralManager = CBCentralManager(delegate: self, queue: nil,
            options: [CBCentralManagerOptionRestoreIdentifierKey: "ai.kun.socialdistancealarm.central"])

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
        if (peripheralManager.isAdvertising) {
            peripheralManager.stopAdvertising()
        }

        if (!isPaused) {
            let uuid = CBUUID(string: Constants.IOS_SERVICE_UUID.rawValue)
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
    
    public func peripheralManager(_ peripheral: CBPeripheralManager, willRestoreState dict: [String : Any]) {
        print("Peripheral Manager willRestoreState called")
        peripheralManager?.stopAdvertising()
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
        if central.isScanning {
            central.stopScan()
        }

        if (!isPaused) {
            delegate?.scanningStarted()
            central.scanForPeripherals(withServices: nil)

            Timer.scheduledTimer(withTimeInterval: AppConstants.traceInterval, repeats: false) { [weak self] _ in
                self?.scanForApps(central: central)
            }
        }
    }

    public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String: Any], rssi RSSI: NSNumber) {
        peripherals[peripheral.identifier] = peripheral
        let uuids = advertisementData[CBAdvertisementDataServiceUUIDsKey] as? [CBUUID]
        // let uuidOverflow = advertisementData[CBAdvertisementDataOverflowServiceUUIDsKey]
        let txPowerLevel = advertisementData[CBAdvertisementDataTxPowerLevelKey] as? NSNumber
 
        guard let uuid = uuids?.first else { return }
        
        let isAndroid = uuid.uuidString.lowercased().hasPrefix(androidPrefix)
        
        if (isAndroid || uuid.uuidString.lowercased().hasPrefix(iosPrefix)) {
                delegate?.didDiscoverPeripheral(uuid: uuid.uuidString, rssi: RSSI, txPower: txPowerLevel, isAndroid: isAndroid)

        }

    }
    
    public func centralManager(_ central: CBCentralManager, willRestoreState dict: [String : Any]) {
        print("Central Manager willRestoreState called")
    }
}
