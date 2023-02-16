package com.example.ex3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.OnAntEventListener;

public class MainActivity extends AppCompatActivity implements OnAntEventListener {

    MotoConnection connection;
    Button pairingButton, startGameButton;

    boolean isPairing;

    TextView statusTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connection=MotoConnection.getInstance();
        connection.startMotoConnection(MainActivity.this);
        connection.saveRfFrequency(26);         // Check the back of your tiles for the RF
        connection.setDeviceId(2);              // Your group number
        connection.registerListener(MainActivity.this);

        statusTextView = findViewById(R.id.statusTextView);
        pairingButton = findViewById(R.id.pairingButton);
        startGameButton = findViewById(R.id.startGameButton);






        pairingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!isPairing)
                {
                    connection.pairTilesStart();
                    pairingButton.setText("Stop pairing!");
                }
                else
                {
                    connection.pairTilesStop();
                    pairingButton.setText("Start pairing!");
                }
                isPairing = !isPairing;
            }
        });

        startGameButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                connection.unregisterListener(MainActivity.this);
                Intent i = new Intent(MainActivity.this, GameActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onMessageReceived(byte[] bytes, long l)
    {

    }

    @Override
    public void onAntServiceConnected()
    {
        connection.setAllTilesToInit();
    }

    @Override
    public void onNumbersOfTilesConnected(final int i)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                statusTextView.setText(i +" connected tiles");
            }
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }
    @Override
    protected void onRestart()
    {
        super.onRestart();
        connection.registerListener(MainActivity.this);
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        connection.stopMotoConnection();
        connection.unregisterListener(MainActivity.this);
    }
}