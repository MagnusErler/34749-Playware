package com.example.ex7;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.livelife.motolibrary.AntData;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.MotoSound;
import com.livelife.motolibrary.OnAntEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static com.livelife.motolibrary.AntData.EVENT_PRESS;
import static com.livelife.motolibrary.AntData.LED_COLOR_OFF;
import static com.livelife.motolibrary.AntData.LED_COLOR_RED;

public class MainActivity extends AppCompatActivity implements OnAntEventListener {

    MotoConnection connection;
    MotoSound sound;

    Button pairingButton;
    Button startGameButton;
    Button simulateGetGameSessions,simulatePostGameSession;
    TextView connectedTextView;


    Boolean isPairing = false;
    Boolean isPlaying = false;

    TextView apiOutput;
    String endpoint = "https://centerforplayware.com/api/index.php";

    int selectedTile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connection = MotoConnection.getInstance();
        sound = MotoSound.getInstance();

        connection.startMotoConnection(this);
        connection.saveRfFrequency(66);
        connection.setDeviceId(2);
        connection.registerListener(this);


        apiOutput = findViewById(R.id.apiOutput);
        connectedTextView = findViewById(R.id.connectedTextView);
        pairingButton = findViewById(R.id.pairingButton);
        pairingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isPlaying) {
                    return;
                }

                Log.i("ButtonStuff","You clicked the button!");
                if(isPairing) {
                    connection.pairTilesStop();
                    pairingButton.setText("START PAIRING");
                } else {
                    connection.pairTilesStart();
                    pairingButton.setText("STOP PAIRING");
                }
                isPairing = !isPairing;
            }
        });

        startGameButton = findViewById(R.id.startGameButton);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPlaying) {
                    startGameButton.setText("STOP GAME");
                    isPlaying = true;
                    connection.setAllTilesIdle(LED_COLOR_OFF);
                    selectedTile = connection.randomIdleTile();
                    connection.setTileColor(LED_COLOR_RED,selectedTile);
                } else {
                    startGameButton.setText("START GAME");
                    isPlaying = false;
                    connection.setAllTilesToInit();
                }
            }
        });

        simulateGetGameSessions = findViewById(R.id.simulateGetGameSessions);
        simulateGetGameSessions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGameSessions();
            }
        });

        simulatePostGameSession = findViewById(R.id.simulatePostGameSession);
        simulatePostGameSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                postGameSession();


            }
        });
    }

    private void postGameSession() {
        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("POST");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method","postGameSession"); // The method name
        requestPackage.setParam("group_id","99"); // Your group ID
        requestPackage.setParam("game_id","1"); // The game ID (From the Game class > setGameId() function
        requestPackage.setParam("game_type_id","1"); // The game type ID (From the GameType class creation > first parameter)
        requestPackage.setParam("game_score","30"); // The game score
        requestPackage.setParam("game_time","60"); // The game elapsed time in seconds
        requestPackage.setParam("num_tiles","4"); // The number of tiles used

        Downloader downloader = new Downloader(); //Instantiation of the Async task
        //that’s defined below

        downloader.execute(requestPackage);
    }
    private void getGameSessions() {
        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("GET");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method","getGameSessions");
        requestPackage.setParam("group_id","99");

        Downloader downloader = new Downloader(); //Instantiation of the Async task
        //that’s defined below

        downloader.execute(requestPackage);
    }

    private class Downloader extends AsyncTask<RemoteHttpRequest, String, String> {
        @Override
        protected String doInBackground(RemoteHttpRequest... params) {
            return HttpManager.getData(params[0]);
        }

        //The String that is returned in the doInBackground() method is sent to the
        // onPostExecute() method below. The String should contain JSON data.
        @Override
        protected void onPostExecute(String result) {
            try {
                //We need to convert the string in result to a JSONObject
                JSONObject jsonObject = new JSONObject(result);
                String message = jsonObject.getString("message");

                // Update UI
                apiOutput.setText(message);
                Log.i("sessions",message);


                if(jsonObject.getString("method") == "getGameSessions") {

                    JSONArray sessions = jsonObject.getJSONArray("results");
                    for(int i = 0; i < sessions.length();i++) {
                        JSONObject session = sessions.getJSONObject(i);
                        Log.i("sessions",session.toString());

                        // get score example:
                        // String score = session.getString("game_score");

                    }

                }
                else if(jsonObject.getString("method") == "postGameSession") {

                    Log.i("sessions",message);

                    // Update UI


                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMessageReceived(byte[] bytes, long l) {

        int command = AntData.getCommand(bytes);
        int tileId = AntData.getId(bytes);
        int color = AntData.getColorFromPress(bytes);

        if(command == EVENT_PRESS) {
            if(tileId == selectedTile) {
                sound.playMatched();
                int randTile = connection.randomIdleTile();
                connection.setAllTilesIdle(LED_COLOR_OFF);
                connection.setTileColor(LED_COLOR_RED,randTile);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Update UI
                    }
                });

            }
        }

    }

    @Override
    public void onAntServiceConnected() {
        connection.setAllTilesToInit();

    }

    @Override
    public void onNumbersOfTilesConnected(int i) {
        connectedTextView.setText("Tiles connected: "+i);
    }

    @Override
    protected void onPause() {
        super.onPause();
        connection.stopMotoConnection();
        connection.unregisterListener(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        connection.startMotoConnection(this);
        connection.registerListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connection.stopMotoConnection();
        connection.unregisterListener(this);
    }
}


/*
package com.example.ex7;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.OnAntEventListener;

public class MainActivity extends AppCompatActivity implements OnAntEventListener {

    MotoConnection connection;
    Button pairingButton, startGameButton;

    boolean isPairing;

    TextView numberOfConnectedTiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connection=MotoConnection.getInstance();
        connection.startMotoConnection(MainActivity.this);
        connection.saveRfFrequency(46);         // Check the back of your tiles for the RF
        connection.setDeviceId(2);              // Your group number
        connection.registerListener(MainActivity.this);

        numberOfConnectedTiles = findViewById(R.id.numberOfConnectedTiles);
        pairingButton = findViewById(R.id.pairingButton);
        startGameButton = findViewById(R.id.startGameButton);


        pairingButton.setOnClickListener(view -> {
            if(!isPairing) {
                connection.pairTilesStart();
                pairingButton.setText("Stop pairing!");
            }
            else {
                connection.pairTilesStop();
                pairingButton.setText("Start pairing!");
            }
            isPairing = !isPairing;
        });

        startGameButton.setOnClickListener(view -> {
            connection.unregisterListener(MainActivity.this);
            Intent i = new Intent(MainActivity.this, GameActivity.class);
            startActivity(i);
        });
    }

    @Override
    public void onMessageReceived(byte[] bytes, long l) {}

    @Override
    public void onAntServiceConnected()
    {
        connection.setAllTilesToInit();
    }

    @Override
    public void onNumbersOfTilesConnected(final int i) {
        runOnUiThread(() -> numberOfConnectedTiles.setText(i +" connected tiles"));
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        connection.registerListener(MainActivity.this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        connection.stopMotoConnection();
        connection.unregisterListener(MainActivity.this);
    }

}

*/