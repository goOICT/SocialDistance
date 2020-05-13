import OpenTraceSupport
import UIKit

public extension UINavigationController {
  private static let navBarAppearance: UINavigationBarAppearance = {
    let appearance = UINavigationBarAppearance()
    appearance.configureWithOpaqueBackground()
    appearance.titleTextAttributes = [.foregroundColor: UIColor.appWhite]
    appearance.largeTitleTextAttributes = [.foregroundColor: UIColor.appWhite]
    appearance.backgroundColor = .appRed
    return appearance
  }()

  func applyStandardStyle() {
    navigationBar.prefersLargeTitles = true
    navigationBar.standardAppearance = UINavigationController.navBarAppearance
    navigationBar.scrollEdgeAppearance = UINavigationController.navBarAppearance
  }
}
