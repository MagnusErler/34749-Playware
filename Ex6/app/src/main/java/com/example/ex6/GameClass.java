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
    int tileColor1, tileColor2, tileColor3;
    int tileID1, tileID2, tileID3;
    int specialColor, specialTile;

    //double timePerRound = 5;

    View targetColor;

    Context applicationContext;

    MotoConnection connection = MotoConnection.getInstance();

    GameType gt;
    ArrayList<Integer> colorList = new ArrayList<>();
    GameClass() {
        setName("Group2Game");
        gt = new GameType(1, GameType.GAME_TYPE_TIME, 9999, "Start game Score 30", 1);
        addGameType(gt);
        addColorsToList();
    }
    public void addColorsToList(){
        colorList.add(LED_COLOR_BLUE);
        colorList.add(LED_COLOR_GREEN);
        colorList.add(LED_COLOR_INDIGO);
        colorList.add(LED_COLOR_ORANGE);
        colorList.add(LED_COLOR_RED);
        //colorList.add(LED_COLOR_VIOLET);
        //colorList.add(LED_COLOR_WHITE);
    }

    public void gameLogic() {
        connection.setAllTilesIdle(LED_COLOR_OFF);

        Collections.shuffle(colorList);

        specialColor = colorList.get(0);
        tileColor1 = colorList.get(1);
        tileColor2 = colorList.get(2);
        tileColor3 = colorList.get(3);

        printColorFromNumber(specialColor);
        printColorFromNumber(tileColor1);
        printColorFromNumber(tileColor2);
        printColorFromNumber(tileColor3);

        //Our special tile
        specialTile = connection.randomIdleTile();
        connection.setTileColor(specialColor, specialTile);

        /*do {
            tileID1 = connection.randomIdleTile();
            tileID2 = connection.randomIdleTile();
            tileID3 = connection.randomIdleTile();
        } while(tileID1 == tileID2 || tileID1 == tileID3 || tileID2 == tileID3);

        Log.d("tag", "tileID1: " + tileID1);
        Log.d("tag", "tileID2: " + tileID2);
        Log.d("tag", "tileID3: " + tileID3);*/

        //Set the other 3 tiles to different colors
        connection.setTileColor(tileColor1, connection.randomIdleTile());
        connection.setTileColor(tileColor2, connection.randomIdleTile());
        connection.setTileColor(tileColor3, connection.randomIdleTile());

        //connection.printMessage("Convert Java String".getBytes());


        //targetColor.setBackgroundColor(getColor(specialColor));

        incrementPlayerScore(0,1);
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



        switch(event) {
            case EVENT_PRESS:
                Log.d("tag", "Tile pressed");
                // Correct tile block
                if (tileId == specialTile) // Check if the special tile has been pressed
                {
                    Log.d("tag", "Correct tile pressed");
                    // Adding 10 points if the player presses a correct tile
                    incrementPlayerScore(10, 1);
                    // Player gets 500 ms less to hit the tile in the next round
                    this.getOnGameEventListener().onGameTimerEvent(1000);
                }
                else {
                    Log.d("tag", "Wrong tile pressed");
                    // Subtracting 5 points if the player presses a wrong tile
                    incrementPlayerScore(-5,1);
                    // Player gets 1000 ms more to hit the tile in the next round
                    this.getOnGameEventListener().onGameTimerEvent(2000);
                }

                /*if (getPlayerScore()[1] <= 0) {
                    gameLost();
                    break;
                }*/

                /*if (getPlayerScore()[1] >= gt.getGoal()) {
                    gameWon();
                    break;
                }*/

                gameLogic();
                break;
            case CMD_COUNTDOWN_TIMEUP:
                Log.d("tag", "Timeup done");
                gameLost();
                // No change to the score
                incrementPlayerScore(0,1);
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
    int getColor(int colorValue) {
        int androidColorCode = 0;
        switch (colorValue) {
            case 0:
                //Log.d("tag", "LED_COLOR_OFF");
                androidColorCode = 0xFFFFFFF;
                break;
            case 1:
                //Log.d("tag", "LED_COLOR_RED");
                androidColorCode = 0xFFFF0000;
                break;
            case 2:
                //Log.d("tag", "LED_COLOR_BLUE");
                androidColorCode = 0xFF0000FF;
                break;
            case 3:
                //Log.d("tag", "LED_COLOR_GREEN");
                androidColorCode = 0xFF00FF00;
                break;
            case 4:
                //Log.d("tag", "LED_COLOR_INDIGO");
                androidColorCode = 0xFF4B0082;
                break;
            case 5:
                //Log.d("tag", "LED_COLOR_ORANGE");
                androidColorCode = 0xFFFFA500;
                break;
            case 6:
                //Log.d("tag", "LED_COLOR_WHITE");
                androidColorCode = 0xFFFFFFFF;
                break;
            case 7:
                //Log.d("tag", "LED_COLOR_VIOLET");
                androidColorCode = 0xFF7F00FF;
                break;
            default:
                Log.d("tag", "ERROR: Color not found");
                break;
        }
        return androidColorCode;
    }

    public void test(View targetColor_temp) {
        targetColor = targetColor_temp;
    }
}