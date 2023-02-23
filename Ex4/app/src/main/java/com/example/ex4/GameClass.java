package com.example.ex4;

import static com.livelife.motolibrary.AntData.LED_COLOR_BLUE;
import static com.livelife.motolibrary.AntData.LED_COLOR_GREEN;
import static com.livelife.motolibrary.AntData.LED_COLOR_OFF;
import static com.livelife.motolibrary.AntData.LED_COLOR_RED;

import android.util.Log;

import com.livelife.motolibrary.AntData;
import com.livelife.motolibrary.Game;
import com.livelife.motolibrary.GameType;
import com.livelife.motolibrary.MotoConnection;

import java.util.ArrayList;
import java.util.Arrays;

public class GameClass extends Game {
    MotoConnection connection = MotoConnection.getInstance();

    GameType gt;

    int baseColor, specialColor;
    GameClass() {
        setName("Group2Game");
        gt = new GameType(1, GameType.GAME_TYPE_SCORE, 5, "Start game Score 5", 1);
        addGameType(gt);
    }

    public void gameLogic() {
        connection.setAllTilesIdle(LED_COLOR_OFF);

        //getGameTypes();

        Log.d("tag", "getGameTypes(): " + getGameTypes());

        ArrayList<Integer> connectedTiles_list = connection.connectedTiles;

        for (int i = 1; i <= connectedTiles_list.size(); i++) {
            connection.setTileColorCountdown(LED_COLOR_BLUE, i, 10);
        }

    }

    public void onGameStart() {
        super.onGameStart();
        gameLogic();
    }

    @Override
    public void onGameUpdate(byte[] message) {
        super.onGameUpdate(message);

        int event = AntData.getCommand(message);
        //int pressedTileColor = AntData.getColorFromPress(message);

        Log.d("tag", "event: " + event);

        switch(event) {
            case 22:
                Log.d("tag", "Tile pressed");
                /*if (pressedTileColor == specialColor) {
                    Log.d("tag", "Special tile pressed");
                    gameLogic(); // generate a new tile when the special one is pressed
                } else {
                    Log.d("tag", "Wrong tile pressed");
                    connection.setAllTilesBlink(4,LED_COLOR_RED);
                }*/
                break;
            case 28:
                Log.d("tag", "Timeup done");
                gameLost();
                break;
            case 36:
                Log.d("tag", "Tile released");
                incrementPlayerScore(1, 1);
                if (getPlayerScore()[1] >= gt.getGoal()) {
                    gameWon();
                }
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

}