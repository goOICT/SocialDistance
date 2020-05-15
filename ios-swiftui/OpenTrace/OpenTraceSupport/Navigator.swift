//
//  Navigator.swift
//  OpenTrace
//
//  Created by Satindar Dhillon on 5/9/20.
//

import UIKit

public protocol Navigator {
  var window: UIWindow { get }
  var services: Services { get }
  var tabBarController: UITabBarController? { get }
  var navigationController: UINavigationController? { get }
  func navigate(to destination: Navigatable)
}

public extension Navigator {
  var tabBarController: UITabBarController? {
    window.rootViewController as? UITabBarController
  }

  var navigationController: UINavigationController? {
    if let navCon = window.rootViewController as? UINavigationController {
      return navCon
    }

    return tabBarController?.selectedViewController as? UINavigationController
  }

  func navigate(to destination: Navigatable) {
    switch destination.navigationStyle {
    case .push:
      break
    case .root:
      window.rootViewController = destination.configuredViewController(
        services: services,
        navigator: self
      )
    case .modal:
      break
    case .tabBar:
      window.rootViewController = destination.configuredViewController(
        services: services,
        navigator: self
      )
    case .none:
      break
    }
  }
}
