//
//  PocketViewController.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/20/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit

class PocketViewController: UIViewController, NotificationListener {

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        NotificationHandler.sharedInstance.currentListener = self
    }
    
    @IBAction func unwindToPocketView( _ seg: UIStoryboardSegue) {}
    
    func onNotification(isLockScreen: Bool) {
        // Check to see if we need to navigate to the lock screen
        if (isLockScreen) {}
    }

}
