import OpenTraceSupport
import SwiftUI

struct WelcomePageViewModel {
  let headlineText: String
  let descriptionText: String
  let image: UIImage
}

struct WelcomePageView: View {
  let viewModel: WelcomePageViewModel

  public var body: some View {
    VStack(spacing: 20) {
      Image(uiImage: viewModel.image)
        .resizable()
        .aspectRatio(contentMode: .fit)

      Text(viewModel.headlineText)
        .font(.headline)
        .padding(.horizontal, 40)

      Text(viewModel.descriptionText)
        .font(.subheadline)
        .multilineTextAlignment(.center)
        .padding(.horizontal, 40)
    }
    .edgesIgnoringSafeArea(.top)
  }
}
