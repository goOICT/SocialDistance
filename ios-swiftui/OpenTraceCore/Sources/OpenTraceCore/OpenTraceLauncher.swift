//
//  OpenTraceLauncher.swift
//  OpenTrace
//
//  Created by Satindar Dhillon on 5/9/20.
//

import OpenTraceSupport
import UIKit

public final class OpenTraceLauncher: Launcher {
  private let navigator: Navigator

  public init?(window: UIWindow?) {
    if let window = window {
      self.navigator = OpenTraceNavigator(window: window)
    } else {
      return nil
    }
  }

  public func launch() {
    navigator.navigate(to: WelcomeDestination())
  }
}
