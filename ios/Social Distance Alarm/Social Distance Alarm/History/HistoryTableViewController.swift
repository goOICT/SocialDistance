//
//  SecondViewController.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/9/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit
import SocialDistanceSDK

class HistoryTableViewController: UITableViewController, DeviceRepositoryListener {
    lazy var dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        return formatter
    }()
    
    var deviceArray = [Device]()

    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.register(DeviceDistanceTableViewCell.classForCoder(), forCellReuseIdentifier: "HistoryItemCell")
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
        let cell = tableView.dequeueReusableCell(withIdentifier: "HistoryItemCell", for: indexPath) as! DeviceDistanceTableViewCell
          
        let device = deviceArray[indexPath.row]
        
        let power = Util.signlaStrength(rssi: device.rssi, txPower: device.txPower, isAndroid: device.isAndroid)
        let classification = Util.classifySignalStrength(power)
        
        cell.signalClassification = classification
        cell.signalStrength = power
        cell.isTeamMember = device.isTeamMember
        
        if cell.extraViewOnRightSide == nil {
            let dateLabel = UILabel()
            dateLabel.textColor = .gray
            dateLabel.font = .systemFont(ofSize: 14.0, weight: .medium)
            cell.extraViewOnRightSide = dateLabel
        }
        
        let dateLabel = cell.extraViewOnRightSide as? UILabel
        dateLabel?.text = dateFormatter.string(from: device.scanDate!)
        
        
        return cell
    }

    func onRepositoryUpdate() {
        deviceArray = DeviceRepository.sharedInstance.getAllDevices()
        tableView.reloadData()
    }
}
