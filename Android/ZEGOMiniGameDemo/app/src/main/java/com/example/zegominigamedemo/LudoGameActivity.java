package com.example.zegominigamedemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.zego.minigameengine.IZegoCommonCallback;
import im.zego.minigameengine.IZegoGameEngineHandler;
import im.zego.minigameengine.ZegoGameComponent;
import im.zego.minigameengine.ZegoGameEngineError;
import im.zego.minigameengine.ZegoGameInfo;
import im.zego.minigameengine.ZegoGameLanguage;
import im.zego.minigameengine.ZegoGameLoadState;
import im.zego.minigameengine.ZegoGameMode;
import im.zego.minigameengine.ZegoGamePlayerState;
import im.zego.minigameengine.ZegoGameRobotConfig;
import im.zego.minigameengine.ZegoGameState;
import im.zego.minigameengine.ZegoGameTaxType;
import im.zego.minigameengine.ZegoGameUserInfo;
import im.zego.minigameengine.ZegoMiniGameEngine;
import im.zego.minigameengine.ZegoRobotSeatInfo;
import im.zego.minigameengine.ZegoStartGameConfig;
import im.zego.minigameengine.ZegoUserSeatInfo;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class LudoGameActivity extends AppCompatActivity {
    ZegoMiniGameEngine miniGameEngine;
    long appID = Your App ID;
    String token = "Your Token";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ludo_game);
        FrameLayout containerView = findViewById(R.id.containerView);
        getPermission();

        String userID = "111";
        String userName = "userName" + userID;
        String userAvatar = "http://e.hiphotos.baidu.com/image/pic/item/a1ec08fa513d2697e542494057fbb2fb4316d81e.jpg";

        String roomID = "Room_111";
        String gameID = "Ludo";

        addButtonEvent(userID);

        createEngine();
        setRTCEngineHandler();
        loginRTCRoom(roomID, userID);

        initMiniGameSDK(userID, userName, userAvatar);
        setGameEngineHandler();
        loadGame(roomID, gameID, containerView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unloadGame();
        uninitGame();

        ZegoExpressEngine.getEngine().stopPublishingStream();
        ZegoExpressEngine.getEngine().logoutRoom();
        ZegoExpressEngine.destroyEngine(null);
    }

    private  void addButtonEvent(String userID) {
        Button mic = findViewById(R.id.mic);
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mic.setSelected(!mic.isSelected());
                ZegoExpressEngine.getEngine().muteMicrophone(mic.isSelected());
            }
        });
    }

    private void getPermission() {
        String[] permissionNeeded = {
                "android.permission.RECORD_AUDIO"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissionNeeded, 101);
            }
        }

    }


    private  void createEngine() {
        ZegoEngineProfile zegoEngineProfile = new ZegoEngineProfile();
        zegoEngineProfile.appID = appID;
        zegoEngineProfile.application = getApplication();
        zegoEngineProfile.scenario = ZegoScenario.DEFAULT;
        ZegoExpressEngine.createEngine(zegoEngineProfile, null);
    }

    private  void loginRTCRoom(String roomID, String userID) {
        ZegoRoomConfig roomConfig = new ZegoRoomConfig();
        roomConfig.token = token;
        ZegoExpressEngine.getEngine().loginRoom(roomID, new ZegoUser(userID), roomConfig, (int error, JSONObject extendedData) -> {
            if (error == 0) {
                startPublish(roomID, userID);
            }
        });
    }

    private  void startPublish(String roomID, String userID) {
        ZegoExpressEngine.getEngine().enableCamera(false);
        ZegoExpressEngine.getEngine().muteMicrophone(false);
        ZegoExpressEngine.getEngine().startPublishingStream(roomID + "_" + userID + "_main");
    }



    private void initMiniGameSDK(String userID, String userName, String userAvatar) {
        miniGameEngine = ZegoMiniGameEngine.getInstance();
        ZegoGameUserInfo userInfo = new ZegoGameUserInfo(userID, userName, userAvatar);
        miniGameEngine.init(appID, token, userInfo, (errorCode, object) -> {});
        miniGameEngine.setGameLanguage(ZegoGameLanguage.en);
    }

    private  void loadGame(String roomID, String gameID, ViewGroup gameContainer) {
        miniGameEngine.setGameContainer(gameContainer);

        HashMap<String, Object> gameConfig = new HashMap<>();
        gameConfig.put(ZegoMiniGameEngine.GAME_CONFIG_ROOM_ID, roomID); // Game room ID
        gameConfig.put(ZegoMiniGameEngine.MIN_GAME_COIN, 0);        // mini game coin
        miniGameEngine.loadGame(gameID,
                ZegoGameMode.ZegoGameModeHostsGame, gameConfig, (errorCode, object) -> {
                    Log.d("TAG", " loadGame Error code: " + errorCode);
                    if (ZegoGameEngineError.SUCCESS == errorCode) {
                        // Load the game successful
                        startGame();
                    }
                });
    }

    private void startGame() {
        // game
        int gamePlayerNum = 4; // player number

        // player
        List<String> selectUserIDList = new ArrayList<>();
        selectUserIDList.add("111");
//        selectUserIDList.add("222");
        List<ZegoUserSeatInfo> userList = new ArrayList<>();
        int seatIndex = 0;
        for (String user: selectUserIDList) {
            userList.add(new ZegoUserSeatInfo(user, seatIndex++));
        }

        // robot
        List<ZegoRobotSeatInfo> robotList = null;
        if (selectUserIDList.size() < gamePlayerNum) {
            robotList = new ArrayList<>();
            for (int i = seatIndex; i < gamePlayerNum; i++) {
                String userName = "hahaha";
                String userAvatar = "{http://e.hiphotos.baidu.com/image/pic/item/a1ec08fa513d2697e542494057fbb2fb4316d81e.jpg}";
                ZegoRobotSeatInfo robotSeatInfo = new ZegoRobotSeatInfo(userName, i, userAvatar, 0, 0);
                robotList.add(robotSeatInfo);
            }
        }

        ZegoStartGameConfig gameConfig = new ZegoStartGameConfig();
        miniGameEngine.startGame(gameConfig, userList, robotList, ((errorCode, object) -> {
            Log.d("TAG", " startGame Error code: " + errorCode);
        }));
    }

    private void unloadGame() {
        miniGameEngine.unloadGame(false);
    }

    private  void uninitGame() {
        miniGameEngine.unInit();
    }

    private void setRTCEngineHandler() {
        ZegoExpressEngine.getEngine().setEventHandler(new IZegoEventHandler() {
            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);

                Log.d("Tag", "onRoomStreamUpdate");
                if (updateType == ZegoUpdateType.ADD) {
                    for (ZegoStream stream: streamList) {
                        ZegoExpressEngine.getEngine().startPlayingStream(stream.streamID);
                    }
                } else {
                    for (ZegoStream stream: streamList) {
                        ZegoExpressEngine.getEngine().stopPlayingStream(stream.streamID);
                    }
                }
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                Log.d("Tag", "onPublisherStateUpdate" + state);
            }
        });
    }

    private  void setGameEngineHandler() {
        miniGameEngine.setGameEngineHandler(new IZegoGameEngineHandler() {
            @Override
            public void onTokenWillExpire() {
            }

            @Override
            public void onGameLoadStateUpdate(ZegoGameLoadState gameLoadState) {
            }

            @Override
            public void onGameStateUpdate(ZegoGameState gameState) {
                Log.d("Tag", "onGameStateUpdate:" + gameState);
            }

            @Override
            public void onPlayerStateUpdate(ZegoGamePlayerState playerState) {
                Log.d("Tag", "onPlayerStateUpdate:" + playerState);
            }

            @Override
            public void onUnloaded(String gameID) {
            }

            @Override
            public void onChargeRequire(String gameID) {
            }

            @Override
            public void onGameSoundPlay(String name, boolean isPlay, String url, boolean isLoop, int volume) {
            }

            @Override
            public void onGameSoundVolumeChange(String name, int volume) {
            }

            @Override
            public void onGameError(int i, String s) {
            }

            @Override
            public void onActionEventUpdate(int actionID, String data) {
            }

            @Override
            public void onGameResult(String result) {
                Log.d("Tag", "onGameResult:" + result);
            }

            @Override
            public ZegoGameRobotConfig onRobotConfigRequire(String gameID) {
                return null;
            }

            @Override
            public void onMicStateChange(boolean b) {
            }

            @Override
            public void onSpeakerStateChange(List<String> list, boolean b) {
            }
        });
    }
}