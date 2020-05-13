//
//  SceneDelegate.swift
//  OpenTrace
//
//  Created by Satindar Dhillon on 5/8/20.
//  Copyright © 2020 Jaya. All rights reserved.
//

import OpenTraceCore
import UIKit

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
  var launcher: OpenTraceLauncher?
  var window: UIWindow?

  func scene(_ scene: UIScene, willConnectTo _: UISceneSession, options _: UIScene.ConnectionOptions) {
    guard let windowScene = scene as? UIWindowScene else { return }

    window = UIWindow(windowScene: windowScene)
    launcher = OpenTraceLauncher(window: window)

    launcher?.launch()
    window?.makeKeyAndVisible()
  }
}
