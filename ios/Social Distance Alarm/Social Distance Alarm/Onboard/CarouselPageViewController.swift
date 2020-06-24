import Foundation
import UIKit

class CarouselPageViewController: UIPageViewController {
    
    public var items: [UIViewController] = []
    
    override func viewDidLoad() {
        super.viewDidLoad()
        dataSource = self
        
        decoratePageControl()
        
        populateItems()
        if let firstViewController = items.first {
            setViewControllers([firstViewController], direction: .forward, animated: true, completion: nil)
        }
    }
    
    fileprivate func decoratePageControl() {
        let pc = UIPageControl.appearance(whenContainedInInstancesOf: [CarouselPageViewController.self])
        pc.currentPageIndicatorTintColor = #colorLiteral(red: 0.0431372549, green: 0.3450980392, blue: 0.6784313725, alpha: 1)
        pc.pageIndicatorTintColor = #colorLiteral(red: 0.4392156863, green: 0.4392156863, blue: 0.4274509804, alpha: 1)
    }
    
    fileprivate func populateItems() {
        let images = [#imageLiteral(resourceName: "proximitySvg"), #imageLiteral(resourceName: "teamsSvg"), #imageLiteral(resourceName: "pocketModeSvgFullScreen"), #imageLiteral(resourceName: "mapSvg")]
        let titles = ["Introduction", "Teams", "Pocket mode", "Location permission"]
        let texts = ["\(Bundle.appTitle) helps maintain social distance. It can see other devices with the app ON and tell you when they are too close.",
                    "Use this for teams of people. Everyone’s app must be turned ON. All data is stored locally.",
                    "You can put the app in pocket mode when you’re not using your phone so the app stays on and detects other devices.",
                    "It works by using Bluetooth’s signal strength. It needs location, and Bluetooth access, to alert you when other app users are too close."]
        
        for (index, t) in texts.enumerated() {
            let c = createCarouselItemControler(image: images[index], title: titles[index], text: t)
            items.append(c)
        }
    }
    
    fileprivate func createCarouselItemControler(image: UIImage, title: String, text: String) -> UIViewController {
        let c = UIViewController()
        c.view = CarouselItem(image: image, title: title, text: text)

        return c
    }
}

// MARK: - DataSource

extension CarouselPageViewController: UIPageViewControllerDataSource {
    func pageViewController(_: UIPageViewController, viewControllerBefore viewController: UIViewController) -> UIViewController? {
        guard let viewControllerIndex = items.firstIndex(of: viewController) else {
            return nil
        }
        
        let previousIndex = viewControllerIndex - 1
        
        guard previousIndex >= 0 else {
            return nil
        }
        
        guard items.count > previousIndex else {
            return nil
        }
        

        return items[previousIndex]
    }
    
    func pageViewController(_: UIPageViewController, viewControllerAfter viewController: UIViewController) -> UIViewController? {
        guard let viewControllerIndex = items.firstIndex(of: viewController) else {
            return nil
        }
        
        let nextIndex = viewControllerIndex + 1
        guard items.count != nextIndex else {
            return nil
        }
        
        guard items.count > nextIndex else {
            return nil
        }
        
        return items[nextIndex]
    }
    
    func presentationCount(for _: UIPageViewController) -> Int {
        return items.count
    }
    
    func presentationIndex(for _: UIPageViewController) -> Int {
        guard let firstViewController = viewControllers?.first,
            let firstViewControllerIndex = items.firstIndex(of: firstViewController) else {
                return 0
        }
        
        return firstViewControllerIndex
    }
}
