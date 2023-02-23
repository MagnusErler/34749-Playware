package com.example.ex5;

import static com.livelife.motolibrary.AntData.*;

import android.util.Log;

import com.livelife.motolibrary.AntData;
import com.livelife.motolibrary.Game;
import com.livelife.motolibrary.GameType;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.MotoSound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class GameClass extends Game {
    MotoConnection connection = MotoConnection.getInstance();

    GameType gt;
    ArrayList<Integer> colorList = new ArrayList<>();
    int specialColor, specialTile;
    GameClass() {
        setName("Group2Game");
        gt = new GameType(1, GameType.GAME_TYPE_SCORE, 5, "Start game Score 5", 1);
        addGameType(gt);
    }
    public void addColours(){
        colorList.add(LED_COLOR_BLUE);
        colorList.add(LED_COLOR_GREEN);
        colorList.add(LED_COLOR_INDIGO);
        colorList.add(LED_COLOR_ORANGE);
        colorList.add(LED_COLOR_RED);
        colorList.add(LED_COLOR_VIOLET);
        colorList.add(LED_COLOR_WHITE);
    }

    public void gameLogic() {
        connection.setAllTilesIdle(LED_COLOR_OFF);

        Collections.shuffle(colorList);

        int color1,color2,color3;

        specialColor = colorList.get(0);
        color1 = colorList.get(1);
        color2 = colorList.get(2);
        color3 = colorList.get(3);

        printColorFromNumber(specialColor);
        printColorFromNumber(color1);
        printColorFromNumber(color2);
        printColorFromNumber(color3);

        //Our special tile
        specialTile = connection.randomIdleTile();
        connection.setTileColor(specialColor, specialTile);

        //Set the other 3 tiles to different colors
        connection.setTileColor(color1, connection.randomIdleTile());

        connection.setTileColor(color2, connection.randomIdleTile());

        connection.setTileColor(color3, connection.randomIdleTile());
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
        //int pressedTileColor = AntData.getColorFromPress(message);

        Log.d("tag", "event: " + event);

        switch(event) {
            case EVENT_PRESS:
                Log.d("tag", "Tile pressed");
                // Correct tile block
                if (tileId == specialTile) // Check if the special tile has been pressed
                {
                    // Adding 10 points if the player presses a correct tile
                    incrementPlayerScore(10, 1);
                    // Player gets 500 ms less to hit the tile in the next round
                    this.getOnGameEventListener().onGameTimerEvent(-500);
                }
                else // Incorrect tile block
                {
                    // Subtracting 5 points if the player presses a wrong tile
                    incrementPlayerScore(-5,1);
                    // Player gets 1000 ms more to hit the tile in the next round
                    this.getOnGameEventListener().onGameTimerEvent(1000);
                }
                break;
            case CMD_COUNTDOWN_TIMEUP:
                Log.d("tag", "Timeup done");
                //gameLost();
                // No change to the score
                incrementPlayerScore(0,1);
                // No change to the timing
                this.getOnGameEventListener().onGameTimerEvent(0);
                break;
            case EVENT_RELEASE:
                Log.d("tag", "Tile released");
                /*
                incrementPlayerScore(1, 1);
                if (getPlayerScore()[1] >= gt.getGoal()) {
                    gameWon();
                }*/
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
                Log.d("tag", "LED_COLOR_OFF");
                androidColorCode = 0x666666;
                break;
            case 1:
                Log.d("tag", "LED_COLOR_RED");
                androidColorCode = 0xFF0000;
                break;
            case 2:
                Log.d("tag", "LED_COLOR_BLUE");
                androidColorCode = 0x0000FF;
                break;
            case 3:
                Log.d("tag", "LED_COLOR_GREEN");
                androidColorCode = 0x00FF00;
                break;
            case 4:
                Log.d("tag", "LED_COLOR_INDIGO");
                androidColorCode = 0x4b0082;
                break;
            case 5:
                Log.d("tag", "LED_COLOR_ORANGE");
                androidColorCode = 0xFFA500;
                break;
            case 6:
                Log.d("tag", "LED_COLOR_WHITE");
                androidColorCode = 0xFFFFFF;
                break;
            case 7:
                Log.d("tag", "LED_COLOR_VIOLET");
                androidColorCode = 0x7F00FF;
                break;
            default:
                Log.d("tag", "ERROR: Color not found");
                break;
        }
        return androidColorCode;
    }

}