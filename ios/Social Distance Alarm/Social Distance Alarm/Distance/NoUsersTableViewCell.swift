//
//  NoUsersTableViewCell.swift
//  Social Distance Alarm
//
//  Created by Andrii Gorishnii on 03.06.2020.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit

class NoUsersTableViewCell: UITableViewCell {
    
    @IBOutlet weak var btSignalImageView: UIImageView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        btSignalImageView.image = btSignalImageView.image?.withRenderingMode(.alwaysTemplate)
        btSignalImageView.tintColor = UIColor.green
    }
}
