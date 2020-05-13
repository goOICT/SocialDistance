import SwiftUI

public final class SettingsViewController: UIHostingController<SettingsView> {
  override public init(rootView: SettingsView) {
    super.init(rootView: rootView)
    navigationItem.title = String.Local.settingsSceneTitle
  }

  @objc dynamic required init?(coder _: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
}

public struct SettingsView: View {
  public init() {}

  public var body: some View {
    Text(String.Local.settingsSceneTitle)
  }
}

struct SettingsView_Previews: PreviewProvider {
  static var previews: some View {
    SettingsView()
  }
}
