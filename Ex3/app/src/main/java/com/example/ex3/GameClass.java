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
import java.util.Random;

public class GameClass extends Game {

    ArrayList<Integer> colourList = new ArrayList<Integer>();

    MotoConnection connection = MotoConnection.getInstance();

    GameClass()
    {
        setName("Colour Race");

        GameType gt = new GameType(1, GameType.GAME_TYPE_TIME, 3000, "Start game",1);
        addGameType(gt);
    }


    public void onGameStart() {

        super.onGameStart();

        connection.setAllTilesIdle(LED_COLOR_OFF);

        colourList.add(LED_COLOR_BLUE);   //0
        colourList.add(LED_COLOR_ORANGE);   //1
        colourList.add(LED_COLOR_GREEN);    //2
        colourList.add(LED_COLOR_RED);   //3

        int randomTile = connection.randomIdleTile();
        //Log.d("tag", "randomTile: " + randomTile);

        Random rand = new Random();
        int randomColour_base = rand.nextInt(colourList.size());
        //Log.d("tag", "randomColour_base: " + randomColour_base);

        connection.setAllTilesColor(colourList.get(randomColour_base));

        int randomColour_special = rand.nextInt(colourList.size());

        while (randomColour_base == randomColour_special) {
            randomColour_special = rand.nextInt(colourList.size());
        }

        //Log.d("tag", "randomColour_special: " + randomColour_special);

        connection.setTileColor(colourList.get(randomColour_special), randomTile);
    }

    // Put game logic here
    @Override
    public void onGameUpdate(byte[] message)
    {
        super.onGameUpdate(message);

        int event = AntData.getCommand(message);
        int pressedTileColor= AntData.getColorFromPress(message);

        //Tile pressed
        if (event == 22) {
            if(colourList.get(randomColour_special))

        }

        Log.d("tag", "event: " + event);
        Log.d("tag", "colour: " + pressedTileColor);

    }


    // Some animation on the tiles once the game is over
    @Override
    public void onGameEnd()
    {
        super.onGameEnd();

        connection.setAllTilesBlink(4,LED_COLOR_RED);
    }

}
