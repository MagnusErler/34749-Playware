package com.livelife.playwaremax;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ScoreboardActivity extends AppCompatActivity {

    ListView gameSessions_ListView;
    ArrayAdapter<String> gameSessions_ArrayAdapter;
    ArrayList<String> games_ArrayList = new ArrayList<>();

    //Database
    TextView apiOutput;
    String endpoint = "https://centerforplayware.com/api/index.php";
    SharedPreferences sharedPref;
    ArrayList<String> listFromJson_ArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        setTitle("Scoreboard");

        sharedPref = this.getApplicationContext().getSharedPreferences("PLAYWARE_COURSE", Context.MODE_PRIVATE);

        // Enable Back-button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        gameSessions_ListView = findViewById(R.id.gameSessions_ListView);

        gameSessions_ArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, games_ArrayList);

        gameSessions_ListView.setAdapter(gameSessions_ArrayAdapter);

        gameSessions_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
                String content = (String) adapterView.getItemAtPosition(position);
                List<String> content_ArrayList = new ArrayList<>(Arrays.asList(content.split(" ")));
            }
        });
        /*// for testing purposes only
        for(int i = 0; i < 20; i++) {
            games_ArrayList.add("Game session ID: " + i + " Score: " + i + " Group ID:" + i + " Number of tiles:" + i);
        }

        games_ArrayList.addAll(games_ArrayList);
        gameSessions_ArrayAdapter.notifyDataSetChanged();*/

        getGameWinner();
    }

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


    private void getGameWinner() {
        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("GET");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method", "getGameWinner"); // The method name
        requestPackage.setParam("device_token", getDeviceToken()); // Your device token
        requestPackage.setParam("group_id", "420"); // Your group ID

        Downloader downloader = new Downloader(); //Instantiation of the Async task
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




                if(jsonObject.getString("method").equals("getGameWinner")) {

                    listFromJson_ArrayList.clear();
                    games_ArrayList.clear();

                    JSONArray sessions = jsonObject.getJSONArray("results");
                    for(int i = 0; i < sessions.length();i++) {
                        JSONObject session = sessions.getJSONObject(i);
                        Log.i("sessions", session.toString());

                        // get score example:
                        // String score = session.getString("game_score");

                        //listFromJson_ArrayList.add("Game session ID:" + session.getString("sid") + " Score: " + session.getString("game_score") + " Group ID:" + session.getString("group_id") + " Number of tiles:" + session.getString("num_tiles"));
                        games_ArrayList.add("Game session ID: " + i + " Score: " + i + " Group ID:" + i + " Number of tiles:" + i);
                    }

                    games_ArrayList.addAll(games_ArrayList);
                    gameSessions_ArrayAdapter.notifyDataSetChanged();
                }

                /*if(jsonObject.getString("method").equals("getGameSessions")) {

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
}