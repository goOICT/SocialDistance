//
//  SceneDelegate.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/9/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit
import SocialDistanceSDK

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    
    let notificationCenter = UNUserNotificationCenter.current()

    var window: UIWindow?
    let defaults = UserDefaults.standard

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        
        notificationCenter.delegate = self
        
        let options: UNAuthorizationOptions = [.alert, .sound, .badge]
        
        notificationCenter.requestAuthorization(options: options) {
            (didAllow, error) in
            if !didAllow {
                print("User has declined notifications")
            }
        }

        // Use this method to optionally configure and attach the UIWindow `window` to the provided UIWindowScene `scene`.
        // If using a storyboard, the `window` property will automatically be initialized and attached to the scene.
        // This delegate does not imply the connecting scene or session are new (see `application:configurationForConnectingSceneSession` instead).
        guard let _ = (scene as? UIWindowScene) else { return }
    }

    func sceneDidDisconnect(_ scene: UIScene) {
        // Called as the scene is being released by the system.
        // This occurs shortly after the scene enters the background, or when its session is discarded.
        // Release any resources associated with this scene that can be re-created the next time the scene connects.
        // The scene may re-connect later, as its session was not neccessarily discarded (see `application:didDiscardSceneSessions` instead).
    }

    func sceneDidBecomeActive(_ scene: UIScene) {
        // Called when the scene has moved from an inactive state to an active state.
        // Use this method to restart any tasks that were paused (or not yet started) when the scene was inactive.
    }

    func sceneWillResignActive(_ scene: UIScene) {
        // Called when the scene will move from an active state to an inactive state.
        // This may occur due to temporary interruptions (ex. an incoming phone call).
    }

    func sceneWillEnterForeground(_ scene: UIScene) {
        // Called as the scene transitions from the background to the foreground.
        // Use this method to undo the changes made on entering the background.
        
        if defaults.bool(forKey: AppConstants.onboardedKey) {
            let bluetoothManager = CoreBluetoothManager.sharedInstance
            bluetoothManager.delegate = (DeviceRepository.sharedInstance as BluetoothManagerDelegate)
            bluetoothManager.startScanning()
            bluetoothManager.startAdvertising()
        }
    }

    func sceneDidEnterBackground(_ scene: UIScene) {
        // Called as the scene transitions from the foreground to the background.
        // Use this method to save data, release shared resources, and store enough scene-specific state information
        // to restore the scene back to its current state.

        // Save changes in the application's managed object context when the application transitions to the background.
        (UIApplication.shared.delegate as? AppDelegate)?.saveContext()
    
        CoreBluetoothManager.sharedInstance.pause(true)
        
        // Going into the background pauses our ability to scan other iPhones that
        // are also in the background, so we pause scanning and let the user know
        // with a notification...
        scheduleNotification()
    }
}

extension SceneDelegate: UNUserNotificationCenterDelegate {
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        
        completionHandler([.alert, .sound])
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        
        guard let rootViewController = (UIApplication.shared.connectedScenes.first?.delegate as? SceneDelegate)?.window?.rootViewController as? UITabBarController else {
            perror("Cannot instantiate UITabBarController from rootViewController")
            completionHandler()
            return
        }
        
        // Default to Pocket Mode tab
        rootViewController.selectedIndex = 3
        
        completionHandler()
    }
    
    func scheduleNotification() {
        
        let content = UNMutableNotificationContent()
        let categoryIdentifire = "Pause Notification Type"
        
        content.title = "Paused"
        
        let appTitle = Bundle.notificationAppTitle
        content.body = "\(appTitle) is paused. Tap here to resume scanning for devices."
        content.sound = UNNotificationSound.default
        content.badge = 1
        content.categoryIdentifier = categoryIdentifire
        
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
        let identifier = AppConstants.pauseNotificationId
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
        
        notificationCenter.add(request) { (error) in
            if let error = error {
                print("Error \(error.localizedDescription)")
            }
        }
                
        let category = UNNotificationCategory(identifier: categoryIdentifire,
                                              actions: [],
                                              intentIdentifiers: [],
                                              options: [])
        
        notificationCenter.setNotificationCategories([category])
    }
}

extension Bundle {
    static var appTitle: String {
        let appTitle = Bundle.main.infoDictionary?["AppTitle"] as? String ?? "Open Trace"
        return appTitle
    }
    
    static var notificationAppTitle: String {
        let appTitle = Bundle.main.infoDictionary?["NotificationAppTitle"] as? String ?? "Social Distance Alarm"
        return appTitle
    }
}
