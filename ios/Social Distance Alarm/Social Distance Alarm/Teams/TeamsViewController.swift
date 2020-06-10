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

    @IBOutlet weak var qrCodeImage: UIImageView!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Build and show the current UUID String as a QRCode
        qrCodeImage.image = generateQRCode(from: CoreBluetoothManager.sharedInstance.uuidString)
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
    }
    
    // QRCodeScannerViewControllerDelegate
    func foundQRCode(value: String) {
        // TODO: Aaron: the `value` is your scanned QR code
    }
}
