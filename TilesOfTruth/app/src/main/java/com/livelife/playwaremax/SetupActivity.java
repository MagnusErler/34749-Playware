package com.livelife.playwaremax;

import static com.livelife.motolibrary.AntData.EVENT_PRESS;
import static com.livelife.motolibrary.AntData.LED_COLOR_GREEN;
import static com.livelife.motolibrary.AntData.LED_COLOR_OFF;
import static com.livelife.motolibrary.AntData.LED_COLOR_RED;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.livelife.motolibrary.AntData;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.MotoSound;
import com.livelife.motolibrary.OnAntEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class SetupActivity extends AppCompatActivity implements OnAntEventListener {

    MotoConnection connection;
    MotoSound sound;

    Button simulateGetGameSessions,simulatePostGameSession, simulatePostGameChallenge, simulateGetGameChallenge;
    TextView connectedTextView;

    // ------ Added by us ------

    int numberOfPlayers = 1;

    int trueTile, falseTile;
    int player1_trueTile, player2_trueTile, player3_trueTile, player4_trueTile;
    int player1_falseTile, player2_falseTile, player3_falseTile, player4_falseTile;

    InputStream inputStream;
    private TextToSpeech textToSpeechSystem;
    ListView gameSessions_ListView;
    ArrayAdapter<String> gameSessions_ArrayAdapter;
    ArrayList<String> games_ArrayList = new ArrayList<>();
    ArrayList<String> listFromJson_ArrayList = new ArrayList<>();
    String groupID = "420";
    String myName = "AndersBjarklev";
    Button createChallenge_Btn;
    // -------------------------


    Boolean isPairing = false;
    Boolean isPlaying = false;

    int setupMode = 1;

    TextView apiOutput;
    String endpoint = "https://centerforplayware.com/api/index.php";

    SharedPreferences sharedPref;

    int selectedTile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // Back-button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        checkIfDeviceIsConnectedToInternet();

        sharedPref = this.getApplicationContext().getSharedPreferences("PLAYWARE_COURSE", Context.MODE_PRIVATE);

        /*gameSessions_ListView = findViewById(R.id.gameSessions_ListView);

        gameSessions_ArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, games_ArrayList);

        gameSessions_ListView.setAdapter(gameSessions_ArrayAdapter);*/

        connection = MotoConnection.getInstance();
        sound = MotoSound.getInstance();

        connection.startMotoConnection(this);
        connection.saveRfFrequency(66);
        connection.setDeviceId(2);
        connection.registerListener(this);

        /*createChallenge_Btn = findViewById(R.id.createChallenge_Btn);

        createChallenge_Btn.setOnClickListener(v -> createChallenge());

        apiOutput = findViewById(R.id.apiOutput);*/
        connectedTextView = findViewById(R.id.connectedTextView);

        Button pairingButton = findViewById(R.id.pairButton);
        RadioGroup playersRadioGroup = findViewById(R.id.playersRadioGroup);
        Button startGameButton = findViewById(R.id.startGameButton);

        pairingButton.setOnClickListener(v -> {
            switch(setupMode) {
                case 1:
                    //Starting pairing tiles -> tiles a spinning
                    connection.pairTilesStart();
                    textToSpeech("Press the tiles you want to use. Press next when you are done.");
                    pairingButton.setText("Next");
                    setupMode = 2;
                    break;
                case 2:
                    //Stopping pairing tiles -> tiles are OFF
                    connection.pairTilesStop();
                    setupTilesPosition(numberOfPlayers);
                    pairingButton.setText("Next");
                    setupMode = 3;
                    break;
                case 3:
                    textToSpeech("Setup complete. You are now ready to play. Press start game");
                    pairingButton.setText("Start Pairing");
                    setupMode = 1;
                    break;
                default:
                    pairingButton.setText("Error");
                    break;
            }

            /*if(isPlaying) {
                return;
            }
            if (isPairing) {
                connection.pairTilesStop();
                pairingButton.setText("Setup Tiles");
            } else {
                connection.pairTilesStart();
                pairingButton.setText("Next");
                setupTilesPosition();
            }
            isPairing = !isPairing;*/
        });




        playersRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
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
        });

        startGameButton.setOnClickListener(v -> {
            int setup[] = {1,1,1};
            Intent intent = new Intent(SetupActivity.this, GameActivity.class);
            intent.putExtra("setup_data",setup);
            startActivity(intent);

            /*if(!isPlaying) {
                startGameButton.setText("STOP GAME");
                isPlaying = true;
                connection.setAllTilesIdle(LED_COLOR_OFF);

            } else {
                startGameButton.setText("START GAME");
                isPlaying = false;
                connection.setAllTilesToInit();
            }*/
        });
    }

    // ------------------------------- //
    // For going back to previous activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    // ------------------------------- //
    // Text To Speech
    public void textToSpeech(String textToSay) {
        textToSpeechSystem = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeechSystem.speak(textToSay, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    // ------------------------------- //
    // Setup of the tiles
    public void setupTilesPosition(int numberOfPlayers) {
        connection.setAllTilesIdle(LED_COLOR_OFF);

        switch (numberOfPlayers) {
            case 1:
                player1_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player1_trueTile, 1);

                player1_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player1_falseTile, 1);
                break;
            case 2:
                player1_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player1_trueTile, 1);

                player1_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player1_falseTile, 1);

                player2_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player2_trueTile, 2);

                player2_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player2_falseTile, 2);

                break;
            case 3:
                player1_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player1_trueTile, 1);

                player1_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player1_falseTile, 1);

                player2_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player2_trueTile, 2);

                player2_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player2_falseTile, 2);

                player3_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player3_trueTile, 3);

                player3_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player3_falseTile, 3);

                break;
            case 4:
                player1_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player1_trueTile, 1);

                player1_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player1_falseTile, 1);

                player2_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player2_trueTile, 2);

                player2_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player2_falseTile, 2);

                player3_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player3_trueTile, 3);

                player3_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player3_falseTile, 3);

                player4_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player4_trueTile, 4);

                player4_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player4_falseTile, 4);

                break;
            default:
                Log.d("tag", "ERROR: Wrong amount of players");
                break;
        }

        textToSpeech("Place the two tiles 3 meters apart and stand between them. Press next when you are done.");
    }

    // ------------------------------- //
    // Checking Internet Connection
    public boolean checkIfDeviceIsConnectedToInternet() {

        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");

            if(!ipAddr.equals("")) {
                //coneccted
                Log.d("tag", "Connected");
                return true;
            }
        } catch (Exception e) {
            Toast.makeText(SetupActivity.this, "You are not connected to the internet!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // ------------------------------- //
    // Challenges
    private void createChallenge() {
        /*final Dialog dialog = new Dialog(this); // Context, this, etc.
        dialog.setContentView(R.layout.createchallenge_dialog);
        dialog.setTitle("Choose Challenge");
        dialog.show();

        findViewById(R.id.createChallenge_NormalMode_Btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("tag", "Normal Mode chosen");
            }
        });*/

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Challenge");

        // add a list
        String[] challenges = {"Normal mode", "Hard mode", "Normal Time mode", "Hard Time mode"};
        builder.setItems(challenges, (dialog, which) -> {
            switch (which) {
                case 0:
                    Log.d("tag", "Normal Mode chosen");
                    Toast.makeText(SetupActivity.this, "Normal Mode chosen", Toast.LENGTH_LONG).show();
                    postGameChallenge("1");
                    break;
                case 1:
                    Log.d("tag", "Hard Mode chosen");
                    Toast.makeText(SetupActivity.this, "Hard Mode chosen", Toast.LENGTH_LONG).show();
                    postGameChallenge("2");
                    break;
                case 2:
                    Log.d("tag", "Normal Time Mode chosen");
                    Toast.makeText(SetupActivity.this, "Normal Time Mode chosen", Toast.LENGTH_LONG).show();
                    postGameChallenge("3");
                    break;
                case 3:
                    Log.d("tag", "Hard Time Mode chosen");
                    Toast.makeText(SetupActivity.this, "Hard Time Mode chosen", Toast.LENGTH_LONG).show();
                    postGameChallenge("4");
                    break;
                default:
                    Log.d("tag", "ERROR: No Game mode chosen");
                    postGameChallenge("1");
                    break;
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void getGameChallenge() {
        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("GET");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method","getGameChallenge"); // The method name
        requestPackage.setParam("device_token",getDeviceToken()); // Your device token
        requestPackage.setParam("group_id",groupID); // Your group ID

        SetupActivity.Downloader downloader = new SetupActivity.Downloader(); //Instantiation of the Async task
        //that’s defined below

        downloader.execute(requestPackage);
    }

    // ------------------------------- //
    // Sessions
    private void postGameSession(String challengeId) {
        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("POST");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method","postGameSession"); // The method name
        requestPackage.setParam("device_token",getDeviceToken()); // Your device token
        requestPackage.setParam("group_id", groupID); // Your group ID
        requestPackage.setParam("game_id","1"); // The game ID (From the Game class > setGameId() function
        requestPackage.setParam("game_type_id","1"); // The game type ID (From the GameType class creation > first parameter)
        requestPackage.setParam("game_score","30"); // The game score
        requestPackage.setParam("game_time","30"); // The game elapsed time in seconds
        requestPackage.setParam("num_tiles","4"); // The number of tiles used
        if (challengeId != null) {
            requestPackage.setParam("gcid",challengeId);
        }


        SetupActivity.Downloader downloader = new SetupActivity.Downloader(); //Instantiation of the Async task

        downloader.execute(requestPackage);
    }
    private void getGameSessions() {
        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("GET");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method","getGameSessions"); // The method name
        requestPackage.setParam("device_token",getDeviceToken()); // Your device token
        requestPackage.setParam("group_id",groupID); // Your group ID

        SetupActivity.Downloader downloader = new SetupActivity.Downloader(); //Instantiation of the Async task
        //that’s defined below

        downloader.execute(requestPackage);
    }
    private void postGameChallenge(String challengeGameType) {
        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("POST");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method","postGameChallenge"); // The method name
        requestPackage.setParam("device_token",getDeviceToken()); // Your device token
        requestPackage.setParam("game_id", "1"); // The game ID (From the Game class > setGameId() function
        requestPackage.setParam("game_type_id", challengeGameType); // The game type ID (From the GameType class creation > first parameter)
        requestPackage.setParam("challenger_name",myName); // The challenger name
        requestPackage.setParam("group_id",groupID); // Your group ID

        SetupActivity.Downloader downloader = new SetupActivity.Downloader(); //Instantiation of the Async task
        //that’s defined below

        downloader.execute(requestPackage);
    }
    private void postGameChallengeAccept(int challengeID) {
        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("POST");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method","postGameChallengeAccept"); // The method name
        requestPackage.setParam("device_token",getDeviceToken()); // Your device token
        requestPackage.setParam("challenged_name",myName); // The name of the person accepting the challenge
        requestPackage.setParam("gcid", String.valueOf(challengeID)); // The game challenge id you want to accept

        SetupActivity.Downloader downloader = new SetupActivity.Downloader(); //Instantiation of the Async task
        //that’s defined below

        downloader.execute(requestPackage);
    }
    private String getDeviceToken() {
        // Get unique device_token from shared preferences
        // Remember that what is saved in sharedPref exists until you delete the app!
        String device_token = sharedPref.getString("device_token",null);

        if(device_token == null) { // If device_token was never saved and null create one
            device_token =  UUID.randomUUID().toString(); // Get a new device_token
            sharedPref.edit().putString("device_token",device_token).apply(); // save it to shared preferences so next time will be used
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

            if(!checkIfDeviceIsConnectedToInternet()) {
                return;
            }

            try {
                //We need to convert the string in result to a JSONObject
                JSONObject jsonObject = new JSONObject(result);

                String message = jsonObject.getString("message");
                Log.i("sessions",message);

                // Log the entire response if needed to check the data structure
                Log.i("sessions",jsonObject.toString());

                // Log response
                Log.i("sessions","response: "+jsonObject.getBoolean("response"));
                // Update UI
                apiOutput.setText(message);



                if(jsonObject.getString("method").equals("getGameSessions")) {

                    listFromJson_ArrayList.clear();
                    games_ArrayList.clear();

                    JSONArray sessions = jsonObject.getJSONArray("results");
                    for(int i = 0; i < sessions.length();i++) {
                        JSONObject session = sessions.getJSONObject(i);
                        Log.i("sessions",session.toString());

                        // get score example:
                        // String score = session.getString("game_score");

                        listFromJson_ArrayList.add("Game session ID:" + session.getString("sid") + " Score: " + session.getString("game_score") + " Group ID:" + session.getString("group_id") + " Number of tiles:" + session.getString("num_tiles"));
                    }

                    games_ArrayList.addAll(listFromJson_ArrayList);
                    gameSessions_ArrayAdapter.notifyDataSetChanged();
                }
                else if(jsonObject.getString("method").equals("getGameChallenge")) {

                    listFromJson_ArrayList.clear();
                    games_ArrayList.clear();

                    JSONArray challenges = jsonObject.getJSONArray("results");
                    for(int i = 0; i < challenges.length();i++) {
                        JSONObject challenge = challenges.getJSONObject(i);
                        Log.i("challenge:",challenge.toString());

                        // get score example:
                        // String score = session.getString("game_score");

                        listFromJson_ArrayList.add("ChallengeID: " + challenge.getString("gcid") + " Name: " + challenge.getString("challenger_name") + " GameID: " + challenge.getString("game_id") + " GameTypeID: " + challenge.getString("game_type_id") + " Status: " + challenge.getString("c_status"));
                    }

                    games_ArrayList.addAll(listFromJson_ArrayList);
                    gameSessions_ArrayAdapter.notifyDataSetChanged();
                }
                else if(jsonObject.getString("method").equals("postGameSession")) {

                    Log.i("sessions",message);

                    // Update UI


                }
                else if(jsonObject.getString("method").equals("postGameChallenge")) {

                    Log.i("challenge",message);

                    // Update UI


                }
                else if(jsonObject.getString("method").equals("postGameChallengeAccept")) {

                    Log.i("challengeAccept",message);

                    // Update UI


                }
                /*else if(jsonObject.getString("method").equals("getGameChallenge")) {

                    JSONArray challenges = jsonObject.getJSONArray("results");
                    for(int i = 0; i < challenges.length();i++) {
                        JSONObject challenge = challenges.getJSONObject(i);
                        Log.i("challenge",challenge.toString());
                        int status = challenge.getInt("c_status");
                        if(status == 4) {
                            Log.i("challenge",challenge.getJSONArray("summary").toString());
                        }
                    }


                    // Update UI
                }*/


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