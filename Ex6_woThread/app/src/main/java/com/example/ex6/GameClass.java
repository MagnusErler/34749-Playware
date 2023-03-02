package com.example.ex6;

import static com.livelife.motolibrary.AntData.*;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
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
import java.util.logging.LogRecord;

public class GameClass extends Game {
    int specialColor, specialTile;

<<<<<<< HEAD:Ex6_woThread/app/src/main/java/com/example/ex6/GameClass.java
    int correctTilesPressed = 0, wrongTilesPressed = 0;

    //double timePerRound = 5;

    Handler handler = new Handler();

    int speedPerRound = 2000;

=======
>>>>>>> 7ba8ce46e728b6f56ed225479a1c0523308bf1e9:Ex6/app/src/main/java/com/example/ex6/GameClass.java
    View targetColor;

    Context applicationContext;

    MotoConnection connection = MotoConnection.getInstance();

    GameType gt;

    GameClass() {
<<<<<<< HEAD:Ex6_woThread/app/src/main/java/com/example/ex6/GameClass.java
        setName("Group2Game");
        gt = new GameType(1, GameType.GAME_TYPE_TIME, 20, "Start game Score 20", 1);
=======
        setName("Colour Race");
        gt = new GameType(1, GameType.GAME_TYPE_TIME, 30, "Start game Score 30", 1);
>>>>>>> 7ba8ce46e728b6f56ed225479a1c0523308bf1e9:Ex6/app/src/main/java/com/example/ex6/GameClass.java
        addGameType(gt);
    }

    public void gameLogic() {
        connection.setAllTilesIdle(LED_COLOR_OFF);
<<<<<<< HEAD:Ex6_woThread/app/src/main/java/com/example/ex6/GameClass.java

        //Collections.shuffle(colorList);

        /*specialColor = colorList.get(0);
        tileColor1 = colorList.get(1);
        tileColor2 = colorList.get(2);
        tileColor3 = colorList.get(3);

        printColorFromNumber(specialColor);
        printColorFromNumber(tileColor1);
        printColorFromNumber(tileColor2);
        printColorFromNumber(tileColor3);*/



=======
>>>>>>> 7ba8ce46e728b6f56ed225479a1c0523308bf1e9:Ex6/app/src/main/java/com/example/ex6/GameClass.java
        //Our special tile
        specialColor = LED_COLOR_GREEN;
        specialTile = connection.randomIdleTile();

        connection.setAllTilesColor(LED_COLOR_OFF);
<<<<<<< HEAD:Ex6_woThread/app/src/main/java/com/example/ex6/GameClass.java
        connection.setTileColor(LED_COLOR_GREEN, specialTile);

        //Set the other 3 tiles to different colors
        /*connection.setTileColor(tileColor1, connection.randomIdleTile());
        connection.setTileColor(tileColor2, connection.randomIdleTile());
        connection.setTileColor(tileColor3, connection.randomIdleTile());*/

        incrementPlayerScore(0,1);
=======

        connection.setTileColor(specialColor, specialTile);
>>>>>>> 7ba8ce46e728b6f56ed225479a1c0523308bf1e9:Ex6/app/src/main/java/com/example/ex6/GameClass.java
    }

    public void onGameStart() {
        super.onGameStart();

        correctTilesPressed = 0;
        wrongTilesPressed = 0;
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
                    correctTilesPressed++;
                    Log.d("tag", "Correct tile pressed");
                    // Adding 10 points if the player presses a correct tile
                    incrementPlayerScore(10, 1);
                    // Player gets 500 ms less to hit the tile in the next round
<<<<<<< HEAD:Ex6_woThread/app/src/main/java/com/example/ex6/GameClass.java
                    //this.getOnGameEventListener().onGameTimerEvent(1000);

                    connection.setTileColor(LED_COLOR_OFF, specialTile);

                    speedPerRound = speedPerRound - 100;
                    final Runnable time = this::gameLogic;
                    handler.postDelayed(time, speedPerRound);

=======
                    this.getOnGameEventListener().onGameTimerEvent(-500);
>>>>>>> 7ba8ce46e728b6f56ed225479a1c0523308bf1e9:Ex6/app/src/main/java/com/example/ex6/GameClass.java
                }
                else {
                    wrongTilesPressed++;
                    Log.d("tag", "Wrong tile pressed");
                    // Subtracting 5 points if the player presses a wrong tile
                    incrementPlayerScore(-5, 1);
                    // Player gets 1000 ms more to hit the tile in the next round
<<<<<<< HEAD:Ex6_woThread/app/src/main/java/com/example/ex6/GameClass.java
                    //this.getOnGameEventListener().onGameTimerEvent(2000);

                    speedPerRound = speedPerRound + 100;
                    final Runnable time = this::gameLogic;
                    handler.postDelayed(time, speedPerRound);
                }

                /*if (getPlayerScore()[1] <= 0) {
                    gameLost();
                    break;
                }*/

                /*if (getPlayerScore()[1] >= gt.getGoal()) {
                    gameWon();
                    break;
                }*/

                if (speedPerRound <= 0) {
                    gameWon();
                }

=======
                    this.getOnGameEventListener().onGameTimerEvent(500);
                }

>>>>>>> 7ba8ce46e728b6f56ed225479a1c0523308bf1e9:Ex6/app/src/main/java/com/example/ex6/GameClass.java
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
<<<<<<< HEAD:Ex6_woThread/app/src/main/java/com/example/ex6/GameClass.java
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
=======
>>>>>>> 7ba8ce46e728b6f56ed225479a1c0523308bf1e9:Ex6/app/src/main/java/com/example/ex6/GameClass.java
}