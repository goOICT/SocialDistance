//
//  Constants.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/18/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import Foundation

enum AppConstants {
    static let traceInterval: Double = 10
    static let signalDistanceOk = 31 // This or lower is socially distant = green
    static let signlaDistanceLightWarn = 41 // This to SIGNAL_DISTANCE_OK warning = yellow
    static let signalDistanceStrongWarn = 55 // This to SIGNAL_DISTANCE_LIGHT_WARN strong warning = orange
    // ...and above this is not socially distant = red
    
    static let pauseNotificationId = "Pause Notification"
    static let onboardedKey = "onboardedKey"

}
