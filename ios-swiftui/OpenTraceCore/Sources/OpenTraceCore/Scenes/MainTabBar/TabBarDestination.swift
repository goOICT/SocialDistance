import OpenTraceSupport
import OpenTraceUI
import SwiftUI
import UIKit

enum TabBarDestination: CaseIterable {
  case distance
  case history
  case settings
}

extension TabBarDestination: Navigatable {
  var requiresAuthentication: Bool { false }
  var navigationStyle: NavigationStyle { .tabBar }

  func configuredViewController(services: Services, navigator: Navigator) -> UIViewController {
    TabBarDestination.tabBarVC(services: services, navigator: navigator)
  }

  private func viewController(services _: Services, navigator _: Navigator) -> UINavigationController {
    let navCon = UINavigationController(
      rootViewController: UIHostingController(rootView: DistanceView())
    )

    switch self {
    case .distance:
      navCon.viewControllers = [DistanceViewController(rootView: DistanceView())]
    case .history:
      navCon.viewControllers = [HistoryViewController(rootView: HistoryView())]
    case .settings:
      navCon.viewControllers = [SettingsViewController(rootView: SettingsView())]
    }

    navCon.tabBarItem = tabBarItem
    navCon.applyStandardStyle()
    return navCon
  }

  private var tabBarItem: UITabBarItem {
    switch self {
    case .distance:
      return UITabBarItem(
        title: String.Local.distanceSceneTitle,
        image: UIImage(systemName: "wifi"),
        selectedImage: UIImage(systemName: "wifi")
      )
    case .history:
      return UITabBarItem(
        title: String.Local.historySceneTitle,
        image: UIImage(systemName: "timer"),
        selectedImage: UIImage(systemName: "timer")
      )
    case .settings:
      return UITabBarItem(
        title: String.Local.settingsSceneTitle,
        image: UIImage(systemName: "gear"),
        selectedImage: UIImage(systemName: "gear")
      )
    }
  }

  private static func tabBarVC(services: Services, navigator: Navigator) -> UITabBarController {
    let tabBarController = UITabBarController()
    tabBarController.tabBar.tintColor = .appRed
    tabBarController.viewControllers = TabBarDestination.allCases.map {
      $0.viewController(services: services, navigator: navigator)
    }
    return tabBarController
  }
}
