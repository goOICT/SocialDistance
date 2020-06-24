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
            navBarAppearance.backgroundColor = .theme
            navigationBar.standardAppearance = navBarAppearance
            navigationBar.scrollEdgeAppearance = navBarAppearance
        }
    }
}

extension UIViewController {
    func addSettingsIcon() {
        let icon = UIImage(named: "settingsIcon")?.withRenderingMode(.alwaysOriginal)
        let settingsButton = UIBarButtonItem(image: icon, landscapeImagePhone: icon, style: .plain, target: self, action: #selector(moveToSettings))
        navigationItem.rightBarButtonItem = settingsButton
    }
    
    @objc func moveToSettings() {
        UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
    }
}
