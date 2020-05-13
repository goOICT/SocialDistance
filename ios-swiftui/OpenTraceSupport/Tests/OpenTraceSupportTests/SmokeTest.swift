import Nimble
import Quick

class SmokeTest: QuickSpec {
  override func spec() {
    describe("a smoke test") {
      it("passes the most basic test") {
        expect(1).to(equal(1))
      }
    }
  }
}
