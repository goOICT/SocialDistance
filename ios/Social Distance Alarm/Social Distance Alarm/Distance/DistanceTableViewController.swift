//
//  FirstViewController.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/9/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit
import SocialDistanceSDK

class DistanceTableViewController: UITableViewController,  DeviceRepositoryListener {
    
    var deviceArray = [Device]()
    let defaults = UserDefaults.standard

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

