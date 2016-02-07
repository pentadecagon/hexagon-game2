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
var boardShape: BOARD_GEOMETRY = .RECT
var phonePlayerId: Int = 0

class ViewController: UIViewController {
    
    func startSettings(){
        self.view = settingsView
    }
    @IBAction func phoneGoes(sender: UISegmentedControl) {
        phonePlayerId = sender.selectedSegmentIndex
    }

    @IBAction func shapeValueChanged(sender: UISegmentedControl) {
        if sender.selectedSegmentIndex == 0 {
            boardShape = .HEX
        } else {
            boardShape = .RECT
        }
    }
    @IBAction func boardSizeChanged(sender: UISegmentedControl) {
        boardSize = sender.selectedSegmentIndex + 1
    }

    @IBAction func startButtonPressed() {
        startGame1()
    }
    func startGame1(){
        let applicationFrame : CGRect = UIScreen.mainScreen().applicationFrame;
        let contentView = HexView(frame: applicationFrame)
        self.view = contentView
    }
    
    override func loadView(){
        viewController = self
        let applicationFrame : CGRect = UIScreen.mainScreen().applicationFrame;
        NSBundle.mainBundle().loadNibNamed( "SettingsView", owner:self, options:nil  )

        settingsView!.frame = applicationFrame
        startGame1()
    }
    
    @IBOutlet var settingsView: UIView! = nil
        
    override func viewDidLoad() {
        super.viewDidLoad()
        setNeedsStatusBarAppearanceUpdate()
        // Do any additional setup after loading the view, typically from a nib.
    }
    
    override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return UIStatusBarStyle.LightContent;
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


}

