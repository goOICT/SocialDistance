import UIKit

class CarouselViewController: UIViewController, UIPageViewControllerDelegate {

    var carouselViewConntroller: CarouselPageViewController? = nil
    @IBOutlet weak var viewContainer: UIView!
    @IBOutlet weak var finnishedButton: UIButton!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        guard let conroller = self.children.first as? CarouselPageViewController else {
            fatalError("Check storyboard for missing CarouselPageViewController")
        }
        carouselViewConntroller = conroller
        carouselViewConntroller?.delegate = self
        finnishedButton.isHidden = true
        self.navigationController?.setNavigationBarHidden(true, animated: false)
        self.tabBarController?.tabBar.isHidden = true
    }
    
    @IBAction func onFinnishPressed(_ sender: Any) {
        self.navigationController?.setNavigationBarHidden(false, animated: false)
        self.tabBarController?.tabBar.isHidden = false
        performSegue(withIdentifier: "unwindToDistanceView", sender: self)
    }
    
    func pageViewController(_ pageViewController: UIPageViewController,
                            willTransitionTo pendingViewControllers: [UIViewController]) {
        if pendingViewControllers[0] == carouselViewConntroller?.items.last {
            finnishedButton.isHidden = false
        } else {
            finnishedButton.isHidden = true
        }
    }
}

