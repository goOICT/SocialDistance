//
//  FirstViewController.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/9/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit
import SocialDistanceSDK

/// The distance view shows the other app users that have been detected in the last scan period.
/// The idea here is to show the user that the app is working more than anything else.
class DistanceTableViewController: UITableViewController,  DeviceRepositoryListener {
    
    var deviceArray = [Device]()
    let defaults = UserDefaults.standard
    
    /// Show a list of devices scanned in the last scan period after the view is loaded.
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationItem.title = "Active Users"
        if !defaults.bool(forKey: AppConstants.onboardedKey) {
            performSegue(withIdentifier: "showOnboardView", sender: self)
        }
        
        tableView.delegate = self
        tableView.dataSource = self
        tableView.tableFooterView = UIView()
        
        tableView.register(DeviceDistanceTableViewCell.classForCoder(), forCellReuseIdentifier: "DeviceItemCell")
    }
    
    /// If the view is getting loaded listen for changes to the Device Repository
    /// - Parameter animated: <#animated description#>
    override func viewWillAppear(_ animated: Bool) {
        DeviceRepository.sharedInstance.currentListener = self
        onRepositoryUpdate()
    }

    //MARK: - Tableview Datasource Methods
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return deviceArray.count > 0 ? deviceArray.count : 1
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if (deviceArray.count > 0) {
            return configureDeviceItemCell(indexPath: indexPath)
        } else {
            return configureNoUsersCell(indexPath: indexPath)
        }
    }
    
    /// Each cell shows a device present during the last scan
    /// - Parameter indexPath: <#indexPath description#>
    /// - Returns: The table cell
    private func configureDeviceItemCell(indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "DeviceItemCell", for: indexPath) as! DeviceDistanceTableViewCell
          
        let device = deviceArray[indexPath.row]
        
        let power = Util.signlaStrength(rssi: device.rssi, txPower: device.txPower, isAndroid: device.isAndroid)
        let classification = Util.classifySignalStrength(power)
        
        cell.signalClassification = classification
        cell.signalStrength = power
        cell.isTeamMember = device.isTeamMember
        return cell
    }

    private func configureNoUsersCell(indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "NoUsersItemCell", for: indexPath) as! NoUsersTableViewCell
        
        return cell
    }
    
    /// If the repository has new devices to show, show them
    func onRepositoryUpdate() {
        deviceArray = DeviceRepository.sharedInstance.getCurrentDevices()

        if (deviceArray.count != 0) {

        } else {

        }
        tableView.reloadData()
    }
    
    @IBAction func unwindToDistanceView( _ seg: UIStoryboardSegue) {
        defaults.set(true, forKey: AppConstants.onboardedKey)
        let bluetoothManager = CoreBluetoothManager.sharedInstance
        bluetoothManager.delegate = (DeviceRepository.sharedInstance as BluetoothManagerDelegate)
        CoreBluetoothManager.sharedInstance.startScanning()
        CoreBluetoothManager.sharedInstance.startAdvertising()
    }
}

