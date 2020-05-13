// swift-tools-version:5.2
import PackageDescription

let package = Package(
  name: "OpenTraceSupport",
  platforms: [
    .iOS(.v13)
  ],
  products: [
    .library(name: "OpenTraceSupport", targets: ["OpenTraceSupport"])
  ],
  dependencies: [
    .package(url: "https://github.com/Quick/Quick", .exact("2.2.0")),
    .package(url: "https://github.com/Quick/Nimble", .exact("8.0.7"))
  ],
  targets: [
    .target(
      name: "OpenTraceSupport",
      dependencies: []
    ),
    .testTarget(
      name: "OpenTraceSupportTests",
      dependencies: ["OpenTraceSupport", "Quick", "Nimble"]
    )
  ]
)
