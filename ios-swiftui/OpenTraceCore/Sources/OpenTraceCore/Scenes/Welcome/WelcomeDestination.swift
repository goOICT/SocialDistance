//
//  WelcomeDestination.swift
//  OpenTrace
//
//  Created by Satindar Dhillon on 5/9/20.
//

import OpenTraceSupport
import OpenTraceUI
import SwiftUI

struct WelcomeDestination: Navigatable {
  let requiresAuthentication = false
  let navigationStyle = NavigationStyle.root

  func configuredViewController(services _: Services, navigator: Navigator) -> UIViewController {
    let viewModel = WelcomeViewModel(navigator: navigator)
    return UIHostingController(rootView: WelcomeView(viewModel: viewModel))
  }
}
