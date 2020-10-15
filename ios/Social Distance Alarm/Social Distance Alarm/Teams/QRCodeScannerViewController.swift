//
//  QRCodeScannerViewController.swift
//  Social Distance Alarm
//
//  Created by Andrii Gorishnii on 10.06.2020.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit
import AVFoundation

protocol QRCodeScannerViewControllerDelegate {
    func foundQRCode(value: String)
}


/// Implements pocket mode which allows the user to eliminate other devices
class QRCodeScannerViewController: UIViewController, AVCaptureMetadataOutputObjectsDelegate {
    private var captureSession: AVCaptureSession!
    private var previewLayer: AVCaptureVideoPreviewLayer!
    
    public var delegate: QRCodeScannerViewControllerDelegate?
    
    /// Once the view loads set up the camera
    override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = UIColor.black
        captureSession = AVCaptureSession()

        guard let videoCaptureDevice = AVCaptureDevice.default(for: .video) else { return }
        let videoInput: AVCaptureDeviceInput

        do {
            videoInput = try AVCaptureDeviceInput(device: videoCaptureDevice)
        } catch {
            return
        }

        if (captureSession.canAddInput(videoInput)) {
            captureSession.addInput(videoInput)
        } else {
            failed()
            return
        }

        let metadataOutput = AVCaptureMetadataOutput()

        if (captureSession.canAddOutput(metadataOutput)) {
            captureSession.addOutput(metadataOutput)

            metadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
            metadataOutput.metadataObjectTypes = [.qr]
        } else {
            failed()
            return
        }

        previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        previewLayer.frame = view.layer.bounds
        previewLayer.videoGravity = .resizeAspectFill
        view.layer.addSublayer(previewLayer)
        
        createOverlay()

        captureSession.startRunning()
    }
    
    /// Start a QR Code capture session
    /// - Parameter animated: <#animated description#>
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        if (captureSession?.isRunning == false) {
            captureSession.startRunning()
        }
    }
    
    /// End the QR code capture session
    /// - Parameter animated: <#animated description#>
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)

        if (captureSession?.isRunning == true) {
            captureSession.stopRunning()
        }
    }
    
    /// Gather the results off the QR code scan
    /// - Parameters:
    ///   - output: <#output description#>
    ///   - metadataObjects: <#metadataObjects description#>
    ///   - connection: <#connection description#>
    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        captureSession.stopRunning()

        if let metadataObject = metadataObjects.first {
            guard let readableObject = metadataObject as? AVMetadataMachineReadableCodeObject else { return }
            guard let stringValue = readableObject.stringValue else { return }
            AudioServicesPlaySystemSound(SystemSoundID(kSystemSoundID_Vibrate))
            found(code: stringValue)
        }

        dismiss(animated: true)
    }
    
    /// No camera.  No clue what iOS device doesn't have a camera, but I guess it's possible
    private func failed() {
        let ac = UIAlertController(title: "Scanning not supported", message: "Your device does not support scanning a code from an item. Please use a device with a camera.", preferredStyle: .alert)
        ac.addAction(
            UIAlertAction(
                title: "OK",
                style: .default,
                handler: {_ in self.dismiss(animated: true, completion: nil)}))
        present(ac, animated: true)
        captureSession = nil
    }
    
    /// Handle the collection of a valid QR code
    /// - Parameter code: Should be a UUID
    private func found(code: String) {
        dismiss(animated: true, completion: nil)
        delegate?.foundQRCode(value: code)
    }
    
    /// Create the overlay
    private func createOverlay() {
        let screenWidth = UIScreen.main.bounds.width
        let screenHeight = UIScreen.main.bounds.height
        
        let transparentHoleWidth = screenWidth * 0.6
        let transparentHoleHeight = transparentHoleWidth
        
        let overlayView = UIView(frame: CGRect(x: 0, y: 0, width: screenWidth, height: screenHeight))
        overlayView.backgroundColor = UIColor.black.withAlphaComponent(0.6)
        
        let path = CGMutablePath()
        
        path.addRect(CGRect(x: (screenWidth - transparentHoleWidth) / 2.0, y: screenHeight * 0.26, width: transparentHoleWidth, height: transparentHoleHeight))
        path.addRect(CGRect(origin: .zero, size: overlayView.frame.size))

        let maskLayer = CAShapeLayer()
        maskLayer.backgroundColor = UIColor.black.cgColor
        maskLayer.path = path
        
        maskLayer.fillRule = .evenOdd
        
        overlayView.layer.mask = maskLayer
        overlayView.clipsToBounds = true
        
        self.view.addSubview(overlayView)
    }
    
    /// hide the status bar
    override var prefersStatusBarHidden: Bool {
        return true
    }
    
    /// portrait mode only
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        return .portrait
    }
}
