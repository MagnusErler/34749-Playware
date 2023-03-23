package com.livelife.playwaremax;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.livelife.motolibrary.AntData;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.MotoSound;
import com.livelife.motolibrary.OnAntEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.ArrayAdapter;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import static com.livelife.motolibrary.AntData.EVENT_PRESS;
import static com.livelife.motolibrary.AntData.LED_COLOR_OFF;
import static com.livelife.motolibrary.AntData.LED_COLOR_RED;

public class MainActivity extends AppCompatActivity implements OnAntEventListener {

    MotoConnection connection;
    MotoSound sound;
    // For game session layout
    ArrayAdapter<String> adapter;
    ListView listview;
    ArrayList<String> arrayGames = new ArrayList<>();
    ArrayList<String> listFromJson = new ArrayList<>();
    Button pairingButton;
    Button startGameButton;
    Button simulateGetGameSessions,simulatePostGameSession;
    TextView connectedTextView;

    Boolean isPairing = false;
    Boolean isPlaying = false;
    SharedPreferences sharedPref;
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

        sharedPref = getSharedPreferences("Prefs", MODE_PRIVATE);
        // UI of active sessions
        listview = findViewById(R.id.gameSessions);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, arrayGames);
      /*  {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView textView=(TextView) view.findViewById(android.R.id.text1);

                textView.setTextColor(Color.WHITE);

                return view;}
        };*/
        listview.setAdapter(adapter);

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
        requestPackage.setParam("group_id","420"); // Your group ID
        requestPackage.setParam("game_id","1"); // The game ID (From the Game class > setGameId() function
        requestPackage.setParam("game_type_id","1"); // The game type ID (From the GameType class creation > first parameter)
        requestPackage.setParam("game_score","30"); // The game score
        requestPackage.setParam("game_time","60"); // The game elapsed time in seconds
        requestPackage.setParam("num_tiles","4"); // The number of tiles used
        requestPackage.setParam("device_token",getDeviceToken());

        Downloader downloader = new Downloader(); //Instantiation of the Async task
        //that’s defined below

        downloader.execute(requestPackage);
    }
    private void getGameSessions() {
        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("GET");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method","getGameSessions");
        requestPackage.setParam("device_token",getDeviceToken());
        requestPackage.setParam("group_id","420");

        Downloader downloader = new Downloader(); //Instantiation of the Async task
        //that’s defined below

        downloader.execute(requestPackage);
    }

    private String getDeviceToken() {
        //Get Device Token
        String device_token = sharedPref.getString("device_token",null);
        if (device_token == null) {
            device_token = UUID.randomUUID().toString();
            sharedPref.edit().putString("device_token",device_token).apply();
        }
        return device_token;
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

                if(jsonObject.getString("method").equals("getGameSessions")) {
                    JSONArray sessions = jsonObject.getJSONArray("results");

                    for(int i = 0; i < sessions.length();i++) {
                        JSONObject session = sessions.getJSONObject(i);
                        Log.i("sessions",session.toString());

                        StringBuilder sb = new StringBuilder();
                        sb.append("Game session ID:").append(session.getString("sid")).append(" Score: ").append(session.getString("game_score")).append(" Group ID:").append(session.getString("group_id")).append(" Number of tiles:").append(session.getString("num_tiles"));
                        listFromJson.add(sb.toString());
                        // get score example:
                        //String score = session.getString("game_score");
                        //Log.i("game_score",score);
                        //arrayGames.add(score);
                    }

                    arrayGames.addAll(listFromJson);
                    adapter.notifyDataSetChanged();
                }
                else if(jsonObject.getString("method").equals("postGameSession") ) {

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
