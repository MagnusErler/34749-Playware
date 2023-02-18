package com.example.ex3;

import static com.livelife.motolibrary.AntData.LED_COLOR_BLUE;
import static com.livelife.motolibrary.AntData.LED_COLOR_GREEN;
import static com.livelife.motolibrary.AntData.LED_COLOR_OFF;
import static com.livelife.motolibrary.AntData.LED_COLOR_ORANGE;
import static com.livelife.motolibrary.AntData.LED_COLOR_RED;

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
        connection.setAllTilesIdle(LED_COLOR_OFF);

        ArrayList<Integer> colorList = new ArrayList<Integer>();
        colorList.add(LED_COLOR_BLUE);
        colorList.add(LED_COLOR_ORANGE);
        colorList.add(LED_COLOR_GREEN);
        colorList.add(LED_COLOR_RED);
        Collections.shuffle(colorList);
        baseColor = colorList.get(0);
        specialColor = colorList.get(1);

        Log.d("tag", "baseColor: " + baseColor);
        Log.d("tag", "specialColor: " + specialColor);

        connection.setAllTilesColor(baseColor); // set all tiles to the random base color

        int randomTile = connection.randomIdleTile();
        connection.setTileColor(specialColor, randomTile); // set the random tile to the special color
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

        if(event == 22) { // tile press event
            if(pressedTileColor == specialColor) {
                gameLogic(); // generate a new tile when the special one is pressed
            }
        }
    }

    // Some animation on the tiles once the game is over
    @Override
    public void onGameEnd()
    {
        super.onGameEnd();

        connection.setAllTilesBlink(4,LED_COLOR_RED);
    }

}
