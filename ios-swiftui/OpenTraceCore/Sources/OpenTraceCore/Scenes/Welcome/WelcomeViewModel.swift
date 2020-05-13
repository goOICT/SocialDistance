//
//  WelcomeViewModel.swift
//  OpenTrace
//
//  Created by Satindar Dhillon on 5/9/20.
//

import Foundation
import OpenTraceSupport
import OpenTraceUI

final class WelcomeViewModel: WelcomeViewModelType {
  let navigator: Navigator

  init(navigator: Navigator) {
    self.navigator = navigator
  }

  func navigateForward() {
    navigator.navigate(to: TabBarDestination.distance)
  }
}
