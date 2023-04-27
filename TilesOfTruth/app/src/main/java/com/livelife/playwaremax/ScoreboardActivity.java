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

        apiOutput = findViewById(R.id.apiOutput);

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
        if (item.getItemId() == android.R.id.home) {
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
        requestPackage.setParam("method", "getGameSessions"); // The method name
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
                // Update UI
                apiOutput.setText(message);

                if(jsonObject.getString("method").equals("getGameSessions")) {

                    listFromJson_ArrayList.clear();
                    games_ArrayList.clear();

                    JSONArray sessions = jsonObject.getJSONArray("results");
                    for(int i = 0; i < sessions.length();i++) {
                        JSONObject session = sessions.getJSONObject(i);

                        //Split string into two varibale seperated by ","
                        String[] parts = session.getString("device_token").split(",");
                        if (parts[0].equals("ToT")) {
                            String gameWinner_Name = parts[1];
                            String gameWinner_Score = parts[2];
                            String gameWinner_Difficulty = parts[3];

                            games_ArrayList.add("Name: " + gameWinner_Name + ", Score: " + gameWinner_Score + ", Difficulty: " + gameWinner_Difficulty);
                        }
                    }
                    gameSessions_ArrayAdapter.notifyDataSetChanged();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}