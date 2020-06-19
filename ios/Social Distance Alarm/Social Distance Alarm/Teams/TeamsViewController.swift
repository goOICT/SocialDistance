//
//  TeamsViewController.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 6/9/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit
import SocialDistanceSDK

class TeamsViewController: UIViewController, QRCodeScannerViewControllerDelegate {
    let teamString = "Your Team has 0 people"

    @IBOutlet weak var teamCountText: UILabel!
    @IBOutlet weak var qrCodeImage: UIImageView!
    let defaults = UserDefaults.standard
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Build and show the current UUID String as a QRCode
        qrCodeImage.image = generateQRCode(from: CoreBluetoothManager.sharedInstance.uuidString)
        teamCountText.text = teamString.replacingOccurrences(of: "0", with: String(DeviceRepository.sharedInstance.teamCount()))
    }
    
    private func generateQRCode(from string: String) -> UIImage? {
        let data = string.data(using: String.Encoding.ascii)

        if let filter = CIFilter(name: "CIQRCodeGenerator") {
            filter.setValue(data, forKey: "inputMessage")
            let transform = CGAffineTransform(scaleX: 6, y: 6)

            if let output = filter.outputImage?.transformed(by: transform) {
                return UIImage(ciImage: output)
            }
        }

        return nil
    }
    
    @IBAction func addTeamMemberAction(_ sender: Any) {
        let vc = QRCodeScannerViewController()
        vc.delegate = self
        vc.modalPresentationStyle = .automatic
        self.tabBarController?.present(vc, animated: true, completion: nil)
    }
    
    @IBAction func resetTeamsAction(_ sender: Any) {
        let alert = UIAlertController(title: "Are you sure?", message: "Resetting your team will remove handsets you scanned and remove you from handsets that have scanned you.", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Yes, Reset", style: .default, handler: { _ in
            CoreBluetoothManager.sharedInstance.resetUuidString()
            DeviceRepository.sharedInstance.resetTeam()
            self.qrCodeImage.image = self.generateQRCode(from: CoreBluetoothManager.sharedInstance.uuidString)
            self.teamCountText.text = self.teamString
        }))
        alert.addAction(UIAlertAction(title: "Cancel", style: .default, handler: nil))
        self.present(alert, animated: true)
    }
    
    // QRCodeScannerViewControllerDelegate
    func foundQRCode(value: String) {
        if (!DeviceRepository.sharedInstance.addTeamMember(uuidString: value)) {
            let alert = UIAlertController(title: "Oops!", message: "The QR code you scanned wasn't from our app.  Try again.", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
            self.present(alert, animated: true)
        } else {
            let alert = UIAlertController(title: "Scanned!", message: "The handset you scanned has been added to your existing team.", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
            self.present(alert, animated: true)
            self.teamCountText.text = self.teamString.replacingOccurrences(of: "0", with: String(DeviceRepository.sharedInstance.teamCount()))
        }
        
    }
}
