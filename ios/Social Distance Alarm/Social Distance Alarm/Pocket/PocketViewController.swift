//
//  PocketViewController.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/20/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit

/// SInce apple doesn't really let us get detected unless the app is running we implemented pocket mode.  That lets the app stay running in your pocket with minimal battery drain.
class PocketViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    @IBAction func unwindToPocketView( _ seg: UIStoryboardSegue) {}
}
