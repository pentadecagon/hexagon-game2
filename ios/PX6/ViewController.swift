//
//  ViewController.swift
//  PX6
//
//  Created by Thomas Kunert on 29.07.14.
//  Copyright (c) 2014 Thomas Kunert. All rights reserved.
//

import UIKit

var viewController: ViewController!
var boardSize: Int = 2
var boardShape: BOARD_GEOMETRY = .rect
var phonePlayerId: Int = 0

class ViewController: UIViewController {
    
    func startSettings(){
        self.view = settingsView
    }
    @IBAction func phoneGoes(_ sender: UISegmentedControl) {
        phonePlayerId = sender.selectedSegmentIndex
    }

    @IBAction func shapeValueChanged(_ sender: UISegmentedControl) {
        if sender.selectedSegmentIndex == 0 {
            boardShape = .hex
        } else {
            boardShape = .rect
        }
    }
    @IBAction func boardSizeChanged(_ sender: UISegmentedControl) {
        boardSize = sender.selectedSegmentIndex + 1
    }

    @IBAction func startButtonPressed() {
        startGame1()
    }
    func startGame1(){
        let applicationFrame : CGRect = UIScreen.main.applicationFrame;
        let contentView = HexView(frame: applicationFrame)
        self.view = contentView
    }
    
    override func loadView(){
        viewController = self
        let applicationFrame : CGRect = UIScreen.main.applicationFrame;
        Bundle.main.loadNibNamed( "SettingsView", owner:self, options:nil  )

        settingsView!.frame = applicationFrame
        startGame1()
    }
    
    @IBOutlet var settingsView: UIView! = nil
        
    override func viewDidLoad() {
        super.viewDidLoad()
        setNeedsStatusBarAppearanceUpdate()
        // Do any additional setup after loading the view, typically from a nib.
    }
    
    override var preferredStatusBarStyle : UIStatusBarStyle {
        return UIStatusBarStyle.lightContent;
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


}

