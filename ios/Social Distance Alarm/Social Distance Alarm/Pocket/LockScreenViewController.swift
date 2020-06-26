//
//  LockScreenViewController.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/21/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit

class LockScreenViewController: UIViewController, SlideButtonDelegate {
    

    @IBOutlet weak var navigation: UINavigationItem!
    @IBOutlet weak var slidingButton: MMSlidingButton!
    override func viewDidLoad() {
        super.viewDidLoad()

        self.navigationController?.setNavigationBarHidden(true, animated: false)
        slidingButton.delegate = self
    }
    
    // MARK: - Navigation
    func buttonStatus(status: String, sender: MMSlidingButton) {
        performSegue(withIdentifier: "unwindToPocketView", sender: self)
        self.navigationController?.setNavigationBarHidden(false, animated: false)
    }
    
    override var prefersStatusBarHidden: Bool {
        return true
    }
    
    override var prefersHomeIndicatorAutoHidden: Bool {
        return true
    }
}
