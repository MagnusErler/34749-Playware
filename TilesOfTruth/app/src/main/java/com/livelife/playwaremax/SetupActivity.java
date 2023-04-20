package com.example.tilestest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        RadioGroup playersRadioGroup = (RadioGroup) findViewById(R.id.playersRadioGroup);
        playersRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                ImageView tilesPositioning = findViewById(R.id.positioningImageView);
                switch(checkedId) {
                    case R.id.twoPlayersButton:
                        tilesPositioning.setImageResource(R.drawable.two_players);
                        break;
                    case R.id.threePlayersButton:
                        tilesPositioning.setImageResource(R.drawable.three_players);
                        break;
                    case R.id.fourPlayersButton:
                        tilesPositioning.setImageResource(R.drawable.four_players);
                        break;
                    default:
                        tilesPositioning.setImageResource(R.drawable.one_players);
                }



            }
        });
    }


}