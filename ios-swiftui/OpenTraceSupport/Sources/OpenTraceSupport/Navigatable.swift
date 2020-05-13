//
//  Navigatable.swift
//  OpenTrace
//
//  Created by Satindar Dhillon on 5/9/20.
//

import UIKit

public protocol Navigatable {
  var requiresAuthentication: Bool { get }
  var navigationStyle: NavigationStyle { get }
  func tabBarIndex(activeCategories: [Navigatable]) -> Int?
  func configuredViewController(services: Services, navigator: Navigator) -> UIViewController
}

public extension Navigatable {
  func tabBarIndex(activeCategories _: [Navigatable]) -> Int? {
    nil
  }
}
