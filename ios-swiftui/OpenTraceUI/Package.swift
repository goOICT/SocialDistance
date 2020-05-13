// swift-tools-version:5.2
import PackageDescription

let package = Package(
  name: "OpenTraceUI",
  platforms: [
    .iOS(.v13)
  ],
  products: [
    .library(name: "OpenTraceUI", targets: ["OpenTraceUI"])
  ],
  dependencies: [
    .package(url: "../OpenTraceSupport", from: "1.0.0")
  ],
  targets: [
    .target(name: "OpenTraceUI", dependencies: ["OpenTraceSupport"])
  ]
)
