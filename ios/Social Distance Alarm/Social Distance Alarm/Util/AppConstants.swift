//
//  AppConstants.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 6/4/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import Foundation
/// These constants control things like when the app alerts about the presence of another app.  iOS was really hard to calibrate.
/// The different models of Apple handsets behave differently with respect to BLE signal strength.  You would think one manufa
enum AppConstants {
    static let signalDistanceOk = 30 // This or lower is socially distant = green
    static let signlaDistanceLightWarn = 33 // This to SIGNAL_DISTANCE_OK warning = yellow
    static let signalDistanceStrongWarn = 40 // This to SIGNAL_DISTANCE_LIGHT_WARN strong warning = orange
    // ...and above this is not socially distant = red

    static let pauseNotificationId = "Pause Notification"
    static let onboardedKey = "onboardedKey"
}
