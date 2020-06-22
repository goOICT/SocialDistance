//
//  Util.swift
//  SocialDistanceSDK
//
//  Created by Piotr on 28/05/2020.
//  Copyright © 2020 kunai. All rights reserved.
//

import Foundation

public enum SignalClassification {
    case tooClose
    case danger
    case warning
    case ok
}

public struct Util {
    static let signalDistanceOk = 30 // This or lower is socially distant = green
    static let signlaDistanceLightWarn = 33 // This to SIGNAL_DISTANCE_OK warning = yellow
    static let signalDistanceStrongWarn = 40 // This to SIGNAL_DISTANCE_LIGHT_WARN strong warning = orange
    
    public static func signlaStrength(rssi: Int32, txPower: Int32, isAndroid: Bool) -> Int32 {
        var signal = SdkConstants.assumedTxPower + rssi
        
        if (!isAndroid) { signal -= SdkConstants.iosSignalReduction }
        
        return signal
    }
    
    public static func classifySignalStrength(_ power: Int32) -> SignalClassification {
        if (power > Util.signalDistanceStrongWarn) {
            return .tooClose
        } else if (power > Util.signlaDistanceLightWarn) {
            return .danger
        } else if (power > Util.signalDistanceOk) {
            return .warning
        } else {
            return .ok
        }
    }
}
