// swift-tools-version:5.2
import PackageDescription

let package = Package(
  name: "OpenTraceCore",
  platforms: [
    .iOS(.v13)
  ],
  products: [
    .library(name: "OpenTraceCore", targets: ["OpenTraceCore"])
  ],
  dependencies: [
    .package(url: "../OpenTraceSupport", from: "1.0.0"),
    .package(url: "../OpenTraceUI", from: "1.0.0")
  ],
  targets: [
    .target(
      name: "OpenTraceCore",
      dependencies: ["OpenTraceSupport", "OpenTraceUI"]
    )
  ]
)
