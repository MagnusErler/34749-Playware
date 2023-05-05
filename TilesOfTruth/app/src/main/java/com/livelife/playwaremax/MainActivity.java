package com.livelife.playwaremax;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.OnAntEventListener;

public class MainActivity extends AppCompatActivity implements OnAntEventListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MotoConnection connection = MotoConnection.getInstance();

        connection.startMotoConnection(this);

        connection.saveRfFrequency(66);
        connection.setDeviceId(2);

        Log.d("tot", "Starting app...");

        Button play_Btn = findViewById(R.id.playButton);
        play_Btn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SetupActivity.class)));

        Button scoreboard_Btn = findViewById(R.id.scoreboardButton);
        scoreboard_Btn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ScoreboardActivity.class)));

        Button addQuestion_Btn = findViewById(R.id.addQuestionButton);
        addQuestion_Btn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddQuestionActivity.class)));
    }

    @Override
    public void onMessageReceived(byte[] bytes, long l) {

    }

    @Override
    public void onAntServiceConnected() {

    }

    @Override
    public void onNumbersOfTilesConnected(int i) {

    }
}