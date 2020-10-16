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

/// The iOS app is different from Android.  On Android we use notifications to alert the user that someone may be too close, but
/// iOS doesn't allow enough notifications.  Also iOS doesn't let us do what we need to do in the background.  Because of this
/// we decided that for iOS we would use notifications to let the user know that they app was no longer broadcasting or
/// scanning, so basically any time the app is taken out of the foreground a notification is sent that will let the user bring
/// it back.
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
