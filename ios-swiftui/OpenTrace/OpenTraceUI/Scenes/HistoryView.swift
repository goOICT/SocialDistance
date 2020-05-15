import SwiftUI

public final class HistoryViewController: UIHostingController<HistoryView> {
  override public init(rootView: HistoryView) {
    super.init(rootView: rootView)
    navigationItem.title = String.Local.historySceneTitle
  }

  @objc dynamic required init?(coder _: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
}

public struct HistoryView: View {
  public init() {}

  public var body: some View {
    Text(String.Local.historySceneTitle)
  }
}

struct HistoryView_Previews: PreviewProvider {
  static var previews: some View {
    HistoryView()
  }
}
