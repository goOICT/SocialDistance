//
//  OpenTraceNavigator.swift
//  OpenTrace
//
//  Created by Satindar Dhillon on 5/9/20.
//

import OpenTraceSupport
import UIKit

final class OpenTraceNavigator: Navigator {
  let window: UIWindow
  let services = Services()

  init(window: UIWindow) {
    self.window = window
  }
}
