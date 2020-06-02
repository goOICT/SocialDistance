//
//  Util.swift
//  SocialDistanceSDK
//
//  Created by Piotr on 28/05/2020.
//  Copyright © 2020 kunai. All rights reserved.
//

import Foundation

public struct Util {
    public static func signlaStrength(rssi: Int32, txPower: Int32) -> Int32 {
        return 127 + (rssi)
    }
}
