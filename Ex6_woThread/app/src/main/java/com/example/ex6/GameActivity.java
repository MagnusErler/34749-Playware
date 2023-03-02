package com.example.ex6;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.livelife.motolibrary.Game;
import com.livelife.motolibrary.GameType;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.OnAntEventListener;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity implements OnAntEventListener {

    MotoConnection connection = MotoConnection.getInstance();
    GameClass game_object = new GameClass(); // Game object
    LinearLayout gt_container;

    int playerScore = 0;
    int specialColor;

    int timePerRound_int = 10;

    TextView playerScore_TextView;
    TextView timePerRound;
    View targetColor;

    //Stop the game when we exit activity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        game_object.stopGame();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        connection.registerListener(this);
        connection.setAllTilesToInit();

        gt_container = findViewById(R.id.game_type_container);
        playerScore_TextView = findViewById(R.id.playerScore_TextView);

        for (final GameType gt : game_object.getGameTypes()) {

            Button b = new Button(this);
            b.setText(gt.getName());
            b.setOnClickListener(v -> {
                game_object.clearPlayersScore();
                runOnUiThread(() -> playerScore_TextView.setText("Score: " + 0));
                //runOnUiThread(() -> timePerRound.setText("Time: " + timePerRound_int));

                game_object.selectedGameType = gt;
                game_object.startGame();
            });
            gt_container.addView(b);
        }

        game_object.setOnGameEventListener(new Game.OnGameEventListener() {
            @Override
            public void onGameTimerEvent(int i) {
            }

            @Override
            public void onGameScoreEvent(int i, int i1) {
                playerScore = game_object.getPlayerScore()[1];
                runOnUiThread(() -> playerScore_TextView.setText("Score: " + playerScore));
                specialColor = game_object.specialColor;
            }

            @Override
            public void onGameStopEvent() {
                Log.d("tag", "onGameStopEvent");
                game_object.gameWon();
            }

            @Override
            public void onSetupMessage(String s) {}

            @Override
            public void onGameMessage(String s) {}

            @Override
            public void onSetupEnd() {}
        });


    }

    @Override
    public void onMessageReceived(byte[] bytes, long l) {
        //Log.d("tag", "Byte: " + new String(bytes, StandardCharsets.UTF_8));
        game_object.addEvent(bytes);
    }

    @Override
    public void onAntServiceConnected() {}

    @Override
    public void onNumbersOfTilesConnected(final int i) {}
}