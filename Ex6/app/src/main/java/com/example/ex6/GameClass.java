package com.example.ex6;

import static com.livelife.motolibrary.AntData.*;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.livelife.motolibrary.AntData;
import com.livelife.motolibrary.Game;
import com.livelife.motolibrary.GameType;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.MotoSound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class GameClass extends Game {
    int specialColor, specialTile;

    View targetColor;

    Context applicationContext;

    MotoConnection connection = MotoConnection.getInstance();

    GameType gt;

    GameClass() {
        setName("Colour Race");
        gt = new GameType(1, GameType.GAME_TYPE_TIME, 30, "Start game Score 30", 1);
        addGameType(gt);
    }

    public void gameLogic() {
        connection.setAllTilesIdle(LED_COLOR_OFF);
        //Our special tile
        specialColor = LED_COLOR_GREEN;
        specialTile = connection.randomIdleTile();

        connection.setAllTilesColor(LED_COLOR_OFF);

        connection.setTileColor(specialColor, specialTile);
    }

    public void onGameStart() {
        super.onGameStart();
        gameLogic();
    }

    @Override
    public void onGameUpdate(byte[] message) {
        super.onGameUpdate(message);

        int tileId = AntData.getId(message);
        int event = AntData.getCommand(message);
        int pressedTileColor = AntData.getColorFromPress(message);


        switch (event) {
            case EVENT_PRESS:
                Log.d("tag", "Tile pressed");
                // Correct tile block
                if (pressedTileColor != LED_COLOR_OFF) // Check if the special tile has been pressed
                {
                    Log.d("tag", "Correct tile pressed");
                    // Adding 10 points if the player presses a correct tile
                    incrementPlayerScore(10, 1);
                    // Player gets 500 ms less to hit the tile in the next round
                    this.getOnGameEventListener().onGameTimerEvent(-500);
                }
                else {
                    Log.d("tag", "Wrong tile pressed");
                    // Subtracting 5 points if the player presses a wrong tile
                    incrementPlayerScore(-5, 1);
                    // Player gets 1000 ms more to hit the tile in the next round
                    this.getOnGameEventListener().onGameTimerEvent(500);
                }

                //gameLogic();
                break;
            case CMD_COUNTDOWN_TIMEUP:
                Log.d("tag", "Timeup done");
                gameLost();
                // No change to the score
                incrementPlayerScore(0, 1);
                // No change to the timing
                this.getOnGameEventListener().onGameTimerEvent(0);
                break;
            case EVENT_RELEASE:
                Log.d("tag", "Tile released");
                break;
            default:
                Log.d("tag", "ERROR: event not found");
                break;
        }
    }

    public void gameWon() {
        connection.setAllTilesBlink(4, LED_COLOR_GREEN);
        stopGame();
    }

    public void gameLost() {
        connection.setAllTilesBlink(4, LED_COLOR_RED);
        stopGame();
    }

    // Some animation on the tiles once the game is over
    @Override
    public void onGameEnd() {
        super.onGameEnd();
    }

    void printColorFromNumber(int colorValue) {
        switch (colorValue) {
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