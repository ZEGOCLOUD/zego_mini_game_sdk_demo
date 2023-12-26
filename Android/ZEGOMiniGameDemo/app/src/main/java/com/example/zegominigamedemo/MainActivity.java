package com.example.zegominigamedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.zego.minigameengine.IZegoGameEngineHandler;
import im.zego.minigameengine.ZegoGameEngineError;
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
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoEngineProfile;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LudoGameActivity.class);
                startActivity(intent);
            }
        });
    }
}