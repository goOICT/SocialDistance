import UIKit

class CarouselViewController: UIViewController {

    var carouselViewConntroller: CarouselPageViewController? = nil
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        guard let conroller = self.children.first as? CarouselPageViewController else {
            fatalError("Check storyboard for missing CarouselPageViewController")
        }
        carouselViewConntroller = conroller
    }
    
    @IBAction func onNextPressed(_ sender: Any) {
        //TODO: No clue how to make this work
    }
}

