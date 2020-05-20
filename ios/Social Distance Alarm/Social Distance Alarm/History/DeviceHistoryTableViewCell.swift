//
//  DeviceHistoryTableViewCell.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/19/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit

class DeviceHistoryTableViewCell: UITableViewCell {

    // MARK: Properties
    @IBOutlet weak var date: UILabel!
    @IBOutlet weak var personIcon: UIImageView!
    @IBOutlet weak var distanceDescription: UILabel!
    @IBOutlet weak var signalStrength: UILabel!
    @IBOutlet weak var bluetoothIcon: UIImageView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
