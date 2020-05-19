//
//  FirstViewController.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/9/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit

class DistanceViewController: UIViewController, UITableViewDataSource, UITableViewDelegate, DeviceRepositoryListener {
    
    @IBOutlet weak var distanceTableView: UITableView!
    var deviceArray = [Device]()

    override func viewDidLoad() {
        super.viewDidLoad()
        
        distanceTableView.delegate = self
        distanceTableView.dataSource = self
        
        DeviceRepository.sharedInstance.currentListener = self
        onRepositoryUpdate()
    }

    //MARK: - Tableview Datasource Methods
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return deviceArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "DeviceItemCell", for: indexPath) as! DeviceDistanceTableViewCell
          
        let device = deviceArray[indexPath.row]
        
        let power = (device.rssi - device.txPower) * -1
        cell.distanceDescription.text = String(format: "Signal strength: %d", power)
        
        tableView.sizeToFit()
          
        return cell
    }

    func onRepositoryUpdate() {
        deviceArray = DeviceRepository.sharedInstance.getCurrentDevices()
        
        if (deviceArray.count != 0) {
            distanceTableView.isHidden = false
            distanceTableView.reloadData()
        } else {
            distanceTableView.isHidden = true
        }
    }
}

