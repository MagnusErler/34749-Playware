package com.livelife.playwaremax;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button play_Btn = findViewById(R.id.playButton);
        play_Btn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SetupActivity.class)));

        Button scoreboard_Btn = findViewById(R.id.scoreboardButton);
        scoreboard_Btn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ScoreboardActivity.class)));
    }
}