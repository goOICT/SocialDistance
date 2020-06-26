//
//  DeviceDistanceTableViewCell.swift
//  Social Distance Alarm
//
//  Created by Aaron Cooley on 5/17/20.
//  Copyright © 2020 Kunai. All rights reserved.
//

import UIKit
import SocialDistanceSDK

fileprivate extension SignalClassification {
    var icon: UIImage? {
        switch self {
        case .danger: return UIImage(named: "personDanger")
        case .tooClose: return UIImage(named: "personTooClose")
        case .warning: return UIImage(named: "personWarning")
        case .ok: return UIImage(named: "personOk")
        }
    }
    
    var text: String {
        switch self {
        case .danger: return "Warning"
        case .tooClose: return "High Risk, Danger"
        case .warning: return "Caution"
        case .ok: return "Safer"
        }
    }
}

class DeviceDistanceTableViewCell: UITableViewCell {
    
    var isTeamMember: Bool = false {
        didSet {
            if isTeamMember {
                personIcon.image = UIImage(systemName: "person.2.fill")?.withRenderingMode(.alwaysTemplate)
                personIcon.tintColor = #colorLiteral(red: 0.07450980392, green: 0.7294117647, blue: 0.1725490196, alpha: 1)
                distanceDescription.text = "Ok"
            }
        }
    }
    
    var signalClassification: SignalClassification? {
        didSet {
            guard let value = signalClassification else { return }
            
            personIcon.image = value.icon
            distanceDescription.text = value.text
        }
    }
    
    var signalStrength: Int32? {
        didSet {
            guard let value = signalStrength else { return }
            let prefix = "Signal strength:"
            let attrString = NSMutableAttributedString(string: "\(prefix) \(value)")
            attrString.addAttributes([.foregroundColor: UIColor.black], range: NSRange(location: prefix.count, length: attrString.string.count - prefix.count))
            
            signalStrengthLabel.attributedText = attrString
        }
    }
    
    var extraViewOnRightSide: UIView? {
        get {
            guard let mainStack = contentView.subviews.first as? UIStackView else { return nil }
            guard let rightStack = mainStack.arrangedSubviews.last as? UIStackView else { return nil }
            guard rightStack.arrangedSubviews.count > 1 else { return nil }
            return rightStack.arrangedSubviews.last
        }
        
        set {
            guard let mainStack = contentView.subviews.first as? UIStackView else { return }
            guard let rightStack = mainStack.arrangedSubviews.last as? UIStackView else { return }
            
            if let extraView = newValue {
                rightStack.addArrangedSubview(extraView)
            } else {
                if let last = rightStack.arrangedSubviews.last {
                    rightStack.removeArrangedSubview(last)
                }
            }
        }
    }
    
    private lazy var personIcon: UIImageView = {
        let imageView = UIImageView()
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    private lazy var signalStrengthLabel: UILabel = {
        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.textColor = .gray
        label.font = .systemFont(ofSize: 14, weight: .medium)
        return label
    }()
    
    private lazy var distanceDescription: UILabel = {
       let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.textColor = .gray
        label.font = .systemFont(ofSize: 14, weight: .medium)
        return label
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupViews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupViews() {
        contentView.backgroundColor = .white
        
        let leftStack = UIStackView(arrangedSubviews: [personIcon])
        leftStack.alignment = .leading
        leftStack.distribution = .equalSpacing
        leftStack.translatesAutoresizingMaskIntoConstraints = false
        
        let rightStack = UIStackView(arrangedSubviews: [distanceDescription])
        rightStack.axis = .vertical
        rightStack.translatesAutoresizingMaskIntoConstraints = false
        rightStack.alignment = .top
        rightStack.spacing = 5.0
        
        let mainStack = UIStackView(arrangedSubviews: [leftStack, rightStack])
        mainStack.translatesAutoresizingMaskIntoConstraints = false
        mainStack.axis = .horizontal
        mainStack.spacing = 40.0
        mainStack.distribution = .equalSpacing
        mainStack.alignment = .center
        
        contentView.addSubview(mainStack)
        NSLayoutConstraint.activate([
            contentView.heightAnchor.constraint(greaterThanOrEqualToConstant: 77),
            mainStack.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 21),
            mainStack.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -21),
            mainStack.topAnchor.constraint(equalTo: topAnchor),
            mainStack.bottomAnchor.constraint(equalTo: bottomAnchor)
        ])
    }

}
