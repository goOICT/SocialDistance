//
//  AppNavigationViewController.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/19/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit

class AppNavigationViewController: UINavigationController {
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        if #available(iOS 13.0, *) {
            let navBarAppearance = UINavigationBarAppearance()
            navBarAppearance.configureWithOpaqueBackground()
            navBarAppearance.titleTextAttributes = [.foregroundColor: UIColor.white]
            navBarAppearance.largeTitleTextAttributes = [.foregroundColor: UIColor.white]
            navBarAppearance.backgroundColor = #colorLiteral(red: 0.7333333333, green: 0, blue: 0.1764705882, alpha: 1)
            navigationBar.standardAppearance = navBarAppearance
            navigationBar.scrollEdgeAppearance = navBarAppearance
        }
    }    
}
