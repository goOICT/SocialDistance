import OpenTraceSupport
import SwiftUI

public protocol WelcomeViewModelType {
  func navigateForward()
}

public struct WelcomeView: View {
  private let viewModel: WelcomeViewModelType

  let pageViewModels = [
    WelcomePageViewModel(
      headlineText: String.Local.onboardOneSectionLabel,
      descriptionText: String.Local.onboardOneSectionDescription,
      image: .welcomeScreenImageOne
    ),
    WelcomePageViewModel(
      headlineText: String.Local.onboardTwoSectionLabel,
      descriptionText: String.Local.onboardTwoSectionDescription,
      image: .welcomeScreenImageTwo
    ),
    WelcomePageViewModel(
      headlineText: String.Local.onboardThreeSectionLabel,
      descriptionText: String.Local.onboardThreeSectionDescription,
      image: .welcomeScreenImageThree
    ),
    WelcomePageViewModel(
      headlineText: String.Local.onboardFourSectionLabel,
      descriptionText: String.Local.onboardFourSectionDescription,
      image: .welcomeScreenImageFour
    )
  ]

  @State private var currentPage = 0

  public init(viewModel: WelcomeViewModelType) {
    self.viewModel = viewModel
  }

  public var body: some View {
    VStack(spacing: 20) {
      PagerView(pageCount: pageViewModels.count, currentIndex: $currentPage) {
        ForEach(pageViewModels, id: \.headlineText) { pageViewModel in
          WelcomePageView(viewModel: pageViewModel)
        }
      }

      HStack(spacing: 8) {
        ForEach(0 ..< pageViewModels.count) { index in
          Circle()
            .fill(index == self.currentPage ? Color.appPrimaryBlue : Color.appGrey)
            .frame(width: 8, height: 8)
        }
      }

      Button(action: handleButtonTap) {
        Text("Continue")
          .foregroundColor(.appPrimaryBlue)
      }
    }
  }

  private func handleButtonTap() {
    viewModel.navigateForward()
  }
}
