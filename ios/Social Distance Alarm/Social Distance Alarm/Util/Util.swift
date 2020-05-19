//
//  Util.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/18/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import Foundation

struct Util {
    static func signlaStrength(rssi: Int32, txPower: Int32) -> Int32 {
        return 127 + (rssi)
    }
}
