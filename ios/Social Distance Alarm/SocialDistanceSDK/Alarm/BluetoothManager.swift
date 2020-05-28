import Foundation
import CoreBluetooth
import AudioToolbox

enum Constants: String {
    case SERVICE_UUID = "d2b86f9a-264e-3ac6-838a-0d00c1f549ed"
    case CENTRAL_MANAGER_ID = "ai.kun.socialdistancealarm.central"
    case PERIPHERAL_MANAGER_ID = "ai.kun.socialdistancealarm.peripheral"
}

public protocol BluetoothManagerDelegate: AnyObject {
    func peripheralsDidUpdate()
}

public protocol BluetoothManager {
    var peripherals: Dictionary<UUID, CBPeripheral> { get }
    var delegate: BluetoothManagerDelegate? { get set }
    func pause(_ with: Bool)
    func startAdvertising()
    func startScanning()
}

public class CoreBluetoothManager: NSObject, BluetoothManager {
    
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

            let uuid = CBUUID(string: Constants.SERVICE_UUID.rawValue)
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
            let uuid = CBUUID(string: Constants.SERVICE_UUID.rawValue)
            var advertisingData: [String : Any] = [
                CBAdvertisementDataLocalNameKey: "social-distance-alarm",
                CBAdvertisementDataServiceUUIDsKey: [uuid]
            ]

            advertisingData[CBAdvertisementDataLocalNameKey] = "SDAlarm"
        
            peripheralManager.startAdvertising(advertisingData)

            Timer.scheduledTimer(withTimeInterval: 5, repeats: false) { _ in
                self.broadcastToApps(peripheralManager: peripheralManager, advertisingData: advertisingData)
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

            let uuid = CBUUID(string: Constants.SERVICE_UUID.rawValue)
            scanForApps(central: central, uuid: uuid)
        } else {
            #warning("Error handling")
        }
    }
    
    func scanForApps(central: CBCentralManager, uuid: CBUUID) {
        if central.isScanning {
            central.stopScan()
        }

        if (!isPaused) {
            DeviceRepository.sharedInstance.updateCurrentDevices()
            central.scanForPeripherals(withServices: [uuid])

            Timer.scheduledTimer(withTimeInterval: AppConstants.traceInterval, repeats: false) { _ in
                self.scanForApps(central: central, uuid: uuid)
            }
        }
    }

    public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String: Any], rssi RSSI: NSNumber) {
        peripherals[peripheral.identifier] = peripheral
        let uuid = advertisementData[CBAdvertisementDataServiceUUIDsKey]
        let uuidOverflow = advertisementData[CBAdvertisementDataOverflowServiceUUIDsKey]
        let rssi = RSSI
        let txPower = advertisementData[CBAdvertisementDataTxPowerLevelKey] as! Int32?
        let date = Date()
        if (uuid != nil) {
            DeviceRepository.sharedInstance.insert(deviceUuid: uuid.debugDescription, rssi: Int32(truncating: rssi), txPower: txPower, scanDate: date)
        }
        print("------------")
        print(uuid)
        print(rssi)
        print(txPower)
        let dateFormatter : DateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        let dateString = dateFormatter.string(from: date)
        print(dateString)
        print("++++++++++++")

    }
    
    public func centralManager(_ central: CBCentralManager, willRestoreState dict: [String : Any]) {
        print("Central Manager willRestoreState called")
    }
}
