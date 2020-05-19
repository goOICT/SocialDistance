//
//  SecondViewController.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/9/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit

class HistoryTableViewController: UITableViewController, DeviceRepositoryListener {
    var deviceArray = [Device]()

    override func viewDidLoad() {
        super.viewDidLoad()
        
    }
    
    override func viewWillAppear(_ animated: Bool) {
        DeviceRepository.sharedInstance.currentListener = self
        onRepositoryUpdate()
        
    }

    //MARK: - Tableview Datasource Methods
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return deviceArray.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "DeviceItemCell", for: indexPath) as! DeviceDistanceTableViewCell
          
        let device = deviceArray[indexPath.row]
        
        let power = Util.signlaStrength(rssi: device.rssi, txPower: device.txPower)
        cell.signalStrength.text = String(format: "Signal strength: %d", power)
        
        if (power > AppConstants.signalDistanceStrongWarn) {
            cell.bluetoothIcon.image = #imageLiteral(resourceName: "bluetoothTooCloseIcon.pdf").withRenderingMode(.alwaysTemplate)
            cell.bluetoothIcon.tintColor = #colorLiteral(red: 0.7333333333, green: 0, blue: 0.1764705882, alpha: 1)
            cell.personIcon.image = #imageLiteral(resourceName: "personIcon.pdf").withRenderingMode(.alwaysTemplate)
            cell.personIcon.tintColor = #colorLiteral(red: 0.7333333333, green: 0, blue: 0.1764705882, alpha: 1)
            cell.distanceDescription.text = "Too Close"
        } else if (power > AppConstants.signlaDistanceLightWarn) {
            cell.bluetoothIcon.image = #imageLiteral(resourceName: "bluetoothDangerIcon")
            cell.bluetoothIcon.tintColor = #colorLiteral(red: 0.9294117647, green: 0.2784313725, blue: 0.09411764706, alpha: 1)
            cell.personIcon.image = #imageLiteral(resourceName: "personIcon.pdf").withRenderingMode(.alwaysTemplate)
            cell.personIcon.tintColor = #colorLiteral(red: 0.9294117647, green: 0.2784313725, blue: 0.09411764706, alpha: 1)
            cell.distanceDescription.text = "Danger"
        } else if (power > AppConstants.signalDistanceOk) {
            cell.bluetoothIcon.image = #imageLiteral(resourceName: "bluetoothWarningIcon")
            cell.bluetoothIcon.tintColor = #colorLiteral(red: 0.7294117647, green: 0.6901960784, blue: 0.07450980392, alpha: 1)
            cell.personIcon.image = #imageLiteral(resourceName: "personIcon.pdf").withRenderingMode(.alwaysTemplate)
            cell.personIcon.tintColor = #colorLiteral(red: 0.7294117647, green: 0.6901960784, blue: 0.07450980392, alpha: 1)
            cell.distanceDescription.text = "Warning"
        } else {
            cell.bluetoothIcon.image = #imageLiteral(resourceName: "bluetoothGoodIcon")
            cell.bluetoothIcon.tintColor = #colorLiteral(red: 0.07450980392, green: 0.7294117647, blue: 0.1725490196, alpha: 1)
            cell.personIcon.image = #imageLiteral(resourceName: "personIcon.pdf").withRenderingMode(.alwaysTemplate)
            cell.personIcon.tintColor = #colorLiteral(red: 0.07450980392, green: 0.7294117647, blue: 0.1725490196, alpha: 1)
            cell.distanceDescription.text = "Ok"
        }
          
        return cell
    }

    func onRepositoryUpdate() {
        deviceArray = DeviceRepository.sharedInstance.getAllDevices()
        tableView.reloadData()

    }
}

