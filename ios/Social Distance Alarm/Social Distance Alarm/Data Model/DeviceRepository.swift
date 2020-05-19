//
//  DeviceRepository.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/16/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit
import CoreData

class DeviceRepository {
    
    public var currentListener: DeviceRepositoryListener? = nil
    
    let context = (UIApplication.shared.delegate as! AppDelegate).persistentContainer.viewContext
    
    static let sharedInstance: DeviceRepository = {
        let instance = DeviceRepository()
        // setup code
        return instance
    }()
    
    func insert(deviceUuid: String, rssi: Int32, txPower: Int32?, scanDate: Date) {
        let newDevice = Device(context: self.context)
        newDevice.deviceUuid = deviceUuid
        newDevice.rssi = rssi
        if (txPower != nil) {
            newDevice.txPower = txPower!
        }
        
        newDevice.scanDate = scanDate

        do {
            try context.save()
        } catch {
            print("Error saving context \(error)")
        }
        
        // TODO: remove old devices
    }
    
    func updateCurrentDevices() {
        currentListener?.onRepositoryUpdate()
    }
    
    func noCurrentDevices() {
        currentListener?.onRepositoryUpdate()
    }
    
    func getCurrentDevices() -> [Device] {
        var deviceArray = [Device]()
        let startTime = (Date() - 10) as NSDate
        let timePredicate = NSPredicate(format: "scanDate >= %@", startTime)
        let request: NSFetchRequest<Device> = Device.fetchRequest()
        request.predicate = timePredicate
        let sortDesc = NSSortDescriptor(key: "scanDate", ascending: false)
        request.sortDescriptors = [sortDesc]
        
        do {
            deviceArray = try context.fetch(request)
        } catch {
            print("Error fetching current devices \(error)")
        }
        
        return deviceArray
    }
    
    func getAllDevices() -> [Device] {
        var deviceArray = [Device]()
        let request: NSFetchRequest<Device> = Device.fetchRequest()
        let sortDesc = NSSortDescriptor(key: "scanDate", ascending: false)
        request.sortDescriptors = [sortDesc]
        
        do {
            deviceArray = try context.fetch(request)
        } catch {
            print("Error fetching all devices \(error)")
        }
        
        return deviceArray
    }
}

protocol DeviceRepositoryListener {
    func onRepositoryUpdate()
}
