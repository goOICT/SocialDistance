//
//  NotificationHandler.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/22/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit

protocol NotificationListener {
    func onNotification(isLockScreen: Bool)
}

class NotificationHandler {

    public var currentListener: NotificationListener? = nil

    static let sharedInstance: NotificationHandler = {
        let instance = NotificationHandler()
        // setup code
        return instance
    }()
    
    public func notify(isLockScreen: Bool) {
        self.currentListener?.onNotification(isLockScreen: isLockScreen)
    }
}
