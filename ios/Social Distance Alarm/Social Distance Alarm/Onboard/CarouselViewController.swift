import UIKit

/// The onboarding is pretty simple.  It shows what the app does with some short descriptions.
class CarouselViewController: UIViewController, UIPageViewControllerDelegate {

    var carouselViewConntroller: CarouselPageViewController? = nil
    @IBOutlet weak var viewContainer: UIView!
    @IBOutlet weak var finnishedButton: UIButton!
    
    
    /// Set up the navigation through the onboarding screens once the view has loaded.
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
    
    /// Once the user has gone through all of the screens they can tap finish and we navigate to the distance screen
    /// - Parameter sender: The sender
    @IBAction func onFinnishPressed(_ sender: Any) {
        self.navigationController?.setNavigationBarHidden(false, animated: false)
        self.tabBarController?.tabBar.isHidden = false
        performSegue(withIdentifier: "unwindToDistanceView", sender: self)
    }
    
    /// a controller that hides the finished button until the user has seen the last page in the carousel
    /// - Parameters:
    ///   - pageViewController: The controller
    ///   - pendingViewControllers: The pending view controler
    func pageViewController(_ pageViewController: UIPageViewController,
                            willTransitionTo pendingViewControllers: [UIViewController]) {
        if pendingViewControllers[0] == carouselViewConntroller?.items.last {
            finnishedButton.isHidden = false
        } else {
            finnishedButton.isHidden = true
        }
    }
}

