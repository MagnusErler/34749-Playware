package com.example.ex4;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.livelife.motolibrary.Game;
import com.livelife.motolibrary.GameType;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.OnAntEventListener;

import java.util.Arrays;

public class GameActivity extends AppCompatActivity implements OnAntEventListener {

    MotoConnection connection = MotoConnection.getInstance();
    GameClass game_object = new GameClass(); // Game object
    LinearLayout gt_container;

    int playerScore = 0;

    TextView playerScore_TextView;

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
                runOnUiThread(() -> playerScore_TextView.setText("PlayerScore: " + 0));

                game_object.selectedGameType = gt;
                game_object.startGame();
            });
            gt_container.addView(b);
        }



        game_object.setOnGameEventListener(new Game.OnGameEventListener() {
            @Override

            public void onGameTimerEvent(int i) {}

            @Override
            public void onGameScoreEvent(int i, int i1) {
                playerScore = game_object.getPlayerScore()[1];
                runOnUiThread(() -> playerScore_TextView.setText("PlayerScore: " + playerScore));
            }

            @Override
            public void onGameStopEvent() {}

            @Override
            public void onSetupMessage(String s) {}

            @Override
            public void onGameMessage(String s) {}

            @Override
            public void onSetupEnd() {}
        });
    }

    @Override
    public void onMessageReceived(byte[] bytes, long l)
    {
        game_object.addEvent(bytes);
    }

    @Override
    public void onAntServiceConnected() {}

    @Override
    public void onNumbersOfTilesConnected(final int i) {}
}