//
//  LockScreenViewController.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/21/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit


/// This is the actual pocket mode.  It looks a bit like a lock screen, hence the name
class LockScreenViewController: UIViewController, SlideButtonDelegate {
    

    @IBOutlet weak var navigation: UINavigationItem!
    @IBOutlet weak var slidingButton: MMSlidingButton!
    
    
    /// Set up the slider that allows the user to go back to the pocket mode page used to launch the lock screen
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
