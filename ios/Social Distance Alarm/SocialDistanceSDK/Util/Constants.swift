//
//  Constants.swift
//  SocialDistanceSDK
//
//  Created by Piotr on 28/05/2020.
//  Copyright © 2020 kunai. All rights reserved.
//


import Foundation

enum SdkConstants {
    static let traceInterval: Double = 5
    static let signalDistanceOk = 30 // This or lower is socially distant = green
    static let signlaDistanceLightWarn = 33 // This to SIGNAL_DISTANCE_OK warning = yellow
    static let signalDistanceStrongWarn = 40 // This to SIGNAL_DISTANCE_LIGHT_WARN strong warning = orange
    // ...and above this is not socially distant = red
    
    static let assumedTxPower: Int32 = 127
    static let iosSignalReduction: Int32 = 17
}
