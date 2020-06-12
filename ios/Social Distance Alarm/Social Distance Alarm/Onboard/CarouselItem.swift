import Foundation
import UIKit

@IBDesignable
class CarouselItem: UIView {
    static let CAROUSEL_ITEM_NIB = "CarouselItem"
    
    @IBOutlet weak var onboardImage: UIImageView!
    @IBOutlet weak var onboardTitleLabel: UILabel!
    @IBOutlet weak var onboardTextLabel: UILabel!
    @IBOutlet var vwContent: UIView!
    
    // MARK: - Init
    override init(frame: CGRect) {
        super.init(frame: frame)
        initWithNib()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        initWithNib()
    }
    
    convenience init(image: UIImage, title: String, text: String) {
        self.init()
        onboardImage.image = image
        onboardTitleLabel.text = title
        onboardTextLabel.text = text
    }
    
    fileprivate func initWithNib() {
        Bundle.main.loadNibNamed(CarouselItem.CAROUSEL_ITEM_NIB, owner: self, options: nil)
        vwContent.frame = bounds
        vwContent.autoresizingMask = [.flexibleHeight, .flexibleWidth]
        addSubview(vwContent)
    }
}
