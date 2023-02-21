package com.example.ex4;

import static com.livelife.motolibrary.AntData.CMD_COUNTDOWN_TIMEUP;
import static com.livelife.motolibrary.AntData.LED_COLOR_BLUE;
import static com.livelife.motolibrary.AntData.LED_COLOR_GREEN;
import static com.livelife.motolibrary.AntData.LED_COLOR_INDIGO;
import static com.livelife.motolibrary.AntData.LED_COLOR_OFF;
import static com.livelife.motolibrary.AntData.LED_COLOR_ORANGE;
import static com.livelife.motolibrary.AntData.LED_COLOR_RED;
import static com.livelife.motolibrary.AntData.LED_COLOR_VIOLET;
import static com.livelife.motolibrary.AntData.LED_COLOR_WHITE;

import android.util.Log;

import com.livelife.motolibrary.AntData;
import com.livelife.motolibrary.Game;
import com.livelife.motolibrary.GameType;
import com.livelife.motolibrary.MotoConnection;

import java.util.ArrayList;
import java.util.Collections;

public class GameClass extends Game {
    MotoConnection connection = MotoConnection.getInstance();

    int baseColor, specialColor;
    GameClass() {
        setName("Group2Game");
        GameType gt = new GameType(1, GameType.GAME_TYPE_TIME, 9999, "Start game", 1);
        addGameType(gt);
    }

    public void gameLogic() {
        connection.setAllTilesIdle(LED_COLOR_RED);

        //getGameTypes();

        Log.d("tag", "getGameTypes(): " + getGameTypes());



        connection.setTileColorCountdown(LED_COLOR_BLUE, connection.randomIdleTile(), 40);
    }

    public void onGameStart() {
        super.onGameStart();
        gameLogic();
    }

    @Override
    public void onGameUpdate(byte[] message) {
        super.onGameUpdate(message);

        int event = AntData.getCommand(message);
        int pressedTileColor = AntData.getColorFromPress(message);

        Log.d("tag", "CMD_COUNTDOWN_TIMEUP: " + CMD_COUNTDOWN_TIMEUP);

        if(event == 22) { // tile press event
            Log.d("tag", "Tile pressed");
            /*if (pressedTileColor == specialColor) {
                Log.d("tag", "Special tile pressed");
                gameLogic(); // generate a new tile when the special one is pressed
            } else {
                Log.d("tag", "Wrong tile pressed");
                connection.setAllTilesBlink(4,LED_COLOR_RED);
            }*/
        }
    }

    // Some animation on the tiles once the game is over
    @Override
    public void onGameEnd() {
        super.onGameEnd();

        connection.setAllTilesBlink(4,LED_COLOR_RED);
    }

    void printColorFromNumber(int colorValue) {
        switch(colorValue) {
            case 0:
                Log.d("tag", "LED_COLOR_OFF");
                break;
            case 1:
                Log.d("tag", "LED_COLOR_RED");
                break;
            case 2:
                Log.d("tag", "LED_COLOR_BLUE");
                break;
            case 3:
                Log.d("tag", "LED_COLOR_GREEN");
                break;
            case 4:
                Log.d("tag", "LED_COLOR_INDIGO");
                break;
            case 5:
                Log.d("tag", "LED_COLOR_ORANGE");
                break;
            case 6:
                Log.d("tag", "LED_COLOR_WHITE");
                break;
            case 7:
                Log.d("tag", "LED_COLOR_VIOLET");
                break;
            default:
                Log.d("tag", "ERROR: Color not found");
                break;
        }
    }

}