//
//  LudoGameViewController.swift
//  ZegoMiniGameSdkDemo
//
//  Created by Larry on 2023/12/27.
//

import UIKit
import ZegoExpressEngine
import ZegoMiniGameEngine

class LudoGameViewController: UIViewController {
    
    let appID: UInt32 = Your app ID
    let token: String = "Your Token"

    override func viewDidLoad() {
        super.viewDidLoad()

        let userID = "111"
        let userName = "userName" + userID
        let userAvatar = "http://e.hiphotos.baidu.com/image/pic/item/a1ec08fa513d2697e542494057fbb2fb4316d81e.jpg"

        let roomID = "Room_111"
        let gameID = "Ludo"
        
        createEngine()
        setRTCEngineHandler()
        loginRTCRoom(roomID: roomID, userID: userID)

        initMiniGameSDK(userID: userID, userName: userName, avatar: userAvatar)
        setGameEngineHandler()
        loadGame(roomID: roomID, gameID: gameID, userID: userID)
        
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        ZegoExpressEngine.shared().stopPublishingStream()
        ZegoExpressEngine.shared().logoutRoom()
        ZegoExpressEngine.destroy(nil)
        
        unloadGame();
        uninitGame();
        
    }

    
    @IBAction func pressMicButton(_ sender: UIButton) {
//        sender.setSelected(!sender.isSelected, animated: true)
        sender.isSelected = !sender.isSelected
        ZegoExpressEngine.shared().muteMicrophone(sender.isSelected)
    }
    
    
    private func createEngine() {
        let profile = ZegoEngineProfile()
        profile.appID = appID
        profile.scenario = .default
        ZegoExpressEngine.createEngine(with: profile, eventHandler: self)
        
    }
    
    private func loginRTCRoom(roomID: String, userID: String) {
        // The value of `userID` is generated locally and must be globally unique.
            let user = ZegoUser(userID: userID)
            // The value of `roomID` is generated locally and must be globally unique.
            // Users must log in to the same room to call each other.
            let roomConfig = ZegoRoomConfig()
        roomConfig.token = token
            // onRoomUserUpdate callback can be received when "isUserStatusNotify" parameter value is "true".
            roomConfig.isUserStatusNotify = true
            // log in to a room
            ZegoExpressEngine.shared().loginRoom(roomID, user: user, config: roomConfig) { errorCode, extendedData in
                if errorCode == 0 {
                    self.startPublish(roomID: roomID, userID: userID)
                } else {
                    // Login room failed
                }
            }
        
    }
    
    private func startPublish(roomID: String, userID: String) {
        let streamID = roomID + "_" + userID + "_main";
        ZegoExpressEngine.shared().enableCamera(false);
        ZegoExpressEngine.shared().muteMicrophone(false);
        ZegoExpressEngine.shared().startPublishingStream(streamID)
        
    }
    
    private func initMiniGameSDK(userID: String, userName: String, avatar: String) {
        let userInfo = ZegoGameUserInfo()
        userInfo.userID = userID
        userInfo.userName = userName
        userInfo.avatar = avatar
        
        ZegoMiniGameEngine.shared().`init`(Int(appID), token: token, userInfo: userInfo) { errorCode, object in
        }
        ZegoMiniGameEngine.shared().setGameLanguage(.EN)
        
    }
    
    private func loadGame(roomID: String, gameID: String, userID: String) {
        ZegoMiniGameEngine.shared().setGameContainer(self.view)
        let gameConfig = [
            "roomID": roomID,
            "minGameCoin": 0,
        ] as [String : Any]
        
        ZegoMiniGameEngine.shared().loadGame(gameID, gameMode: .hostsGame, gameConfig: gameConfig) { errorCode in
            print("loadGame: \(errorCode)")
            if(errorCode == 0) {
                self.startGame(userID: userID)
            }
        }
        
    }
    
    private func startGame(userID: String) {
        let players = [userID]
        let gamePlayCount = 4
        var userSeatInfos: [ZegoUserSeatInfo] = []
        for index in 0..<players.count {
            let userID = players[index]
            let seat = ZegoUserSeatInfo(userID: userID, seatIndex: Int32(index))
            userSeatInfos.append(seat)
        }
        
        var robotSeatInfos: [ZegoRobotSeatInfo] = []
        if (players.count < gamePlayCount) {
            
            for index in players.count..<gamePlayCount {
                let avatar = "http://e.hiphotos.baidu.com/image/pic/item/a1ec08fa513d2697e542494057fbb2fb4316d81e.jpg"
                let robot = ZegoRobotSeatInfo(robotName: "Robot", seatIndex: Int32(index), robotAvatar: avatar, robotLevel: 4, robotBalance: 0)
                robotSeatInfos.append(robot)
            }
        }
        let gameConfig = ZegoStartGameConfig()
        ZegoMiniGameEngine.shared().startGame(gameConfig, userSeatInfoList: userSeatInfos, robotSeatInfoList: robotSeatInfos) { errorCode in
            print("startGame: \(errorCode)")
        }
        
        
        
    }
    
    private func unloadGame() {
        ZegoMiniGameEngine.shared().unloadGame(false)
    }
    
    private func uninitGame() {
        ZegoMiniGameEngine.shared().unInit()
    }
    
    private func setRTCEngineHandler() {
        
    }
    
    private func setGameEngineHandler() {
        ZegoMiniGameEngine.shared().setGameEngineHandler(self)
        
    }
}

extension LudoGameViewController : ZegoEventHandler {

    // Callback for updates on the status of the streams in the room.
    func onRoomStreamUpdate(_ updateType: ZegoUpdateType, streamList: [ZegoStream], extendedData: [AnyHashable : Any]?, roomID: String) {
        // If users want to play the streams published by other users in the room, call the startPlayingStream method with the corresponding streamID obtained from the `streamList` parameter where ZegoUpdateType == ZegoUpdateTypeAdd.
        if updateType == .add {
            for stream in streamList {
                ZegoExpressEngine.shared().startPlayingStream(stream.streamID)
            }
        } else {
            for stream in streamList {
                ZegoExpressEngine.shared().stopPlayingStream(stream.streamID)
            }
        }
    }

    // Callback for updates on the current user's room connection status.
    func onRoomStateUpdate(_ state: ZegoRoomState, errorCode: Int32, extendedData: [AnyHashable : Any]?, roomID: String) {
     }

    // Callback for updates on the status of other users in the room.
    // Users can only receive callbacks when the isUserStatusNotify property of ZegoRoomConfig is set to `true` when logging in to the room (loginRoom).
    func onRoomUserUpdate(_ updateType: ZegoUpdateType, userList: [ZegoUser], roomID: String) {
    }

}

extension LudoGameViewController : ZegoGameEngineHandler {
    func onGameStateUpdate(_ gameState: ZegoGameState) {
        print("onGameStateUpdate: \(gameState)")
        
    }
    
    func onGameResult(_ result: String) {
        print("onGameResult: \(result)")
        
    }
    
    func onPlayerStateUpdate(_ playerState: ZegoGamePlayerState) {
        print("onPlayerStateUpdate: \(playerState)")
        
    }
}
