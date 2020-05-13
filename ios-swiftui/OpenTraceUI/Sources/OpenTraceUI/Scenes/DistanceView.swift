import OpenTraceSupport
import SwiftUI

public final class DistanceViewController: UIHostingController<DistanceView> {
  override public init(rootView: DistanceView) {
    super.init(rootView: rootView)
    navigationItem.title = String.Local.distanceSceneTitle
  }

  @objc dynamic required init?(coder _: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
}

public struct DistanceView: View {
  public init() {}

  public var body: some View {
    Text(String.Local.distanceSceneTitle)
  }
}

struct DistanceView_Previews: PreviewProvider {
  static var previews: some View {
    DistanceView()
  }
}
