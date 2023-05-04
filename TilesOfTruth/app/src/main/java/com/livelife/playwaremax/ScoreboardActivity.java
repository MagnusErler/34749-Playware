package com.livelife.playwaremax;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class ScoreboardActivity extends AppCompatActivity {

    ListView gameSessions_ListView;
    ArrayAdapter<String> gameSessions_ArrayAdapter;
    ArrayList<String> games_ArrayList = new ArrayList<>();

    //Database
    String endpoint = "https://centerforplayware.com/api/index.php";
    SharedPreferences sharedPref;
    ArrayList<String> listFromJson_ArrayList = new ArrayList<>();
    int sortByDifficulty = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        setTitle("Scoreboard");

        sharedPref = this.getApplicationContext().getSharedPreferences("PLAYWARE_COURSE", Context.MODE_PRIVATE);

        // Enable Back-button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        gameSessions_ListView = findViewById(R.id.gameSessions_ListView);
        gameSessions_ArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, games_ArrayList);
        gameSessions_ListView.setAdapter(gameSessions_ArrayAdapter);
        gameSessions_ListView.setOnItemClickListener((adapterView, arg1, position, arg3) -> {
            Toast.makeText(this, "games_ArrayList.get(position): " + games_ArrayList.get(position), Toast.LENGTH_SHORT).show();
            //games_ArrayList.get(position);
            showChallengeUser();
        });

        if (!checkIfDeviceIsConnectedToInternet()) {
            return;
        }

        getGameWinner();

        RadioGroup playersRadioGroup = findViewById(R.id.difficultyRadioGroup);
        playersRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch(checkedId) {
                case R.id.allScoreboardButton:
                    sortByDifficulty = 0;
                    break;
                case R.id.easyScoreboardButton:
                    sortByDifficulty = 1;
                    break;
                case R.id.normalScoreboardButton:
                    sortByDifficulty = 2;
                    break;
                case R.id.hardScoreboardButton:
                    sortByDifficulty = 3;
                    break;
                default:
                    break;
            }
            getGameWinner();
        });
    }

    void showChallengeUser() {
        AlertDialog.Builder addQuestion_builder = new AlertDialog.Builder(this);
        addQuestion_builder.setView(R.layout.dialog_challenge_user);
        AlertDialog addQuestion_AlertDialog = addQuestion_builder.create();
        addQuestion_AlertDialog.setCancelable(false);
        addQuestion_AlertDialog.show();

        RadioGroup rg = addQuestion_AlertDialog.findViewById(R.id.challenge_RadioGroup);

        int selectedId = rg.getCheckedRadioButtonId();
        Toast.makeText(getApplicationContext(), "selectedId: " + selectedId, Toast.LENGTH_SHORT).show();

    }

    // ------------------------------- //
    // Checking Internet Connection
    public boolean checkIfDeviceIsConnectedToInternet() {
        try {
            String command = "ping -c 1 google.com";
            boolean value = (Runtime.getRuntime().exec(command).waitFor() == 0);
            if (!value) {
                Toast.makeText(getApplicationContext(), "You are not connected to the internet!", Toast.LENGTH_LONG).show();
            }
            return value;
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "You are not connected to the internet!", Toast.LENGTH_LONG).show();
            return false;
        }
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

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);

                if(jsonObject.getString("method").equals("getGameSessions")) {

                    listFromJson_ArrayList.clear();
                    games_ArrayList.clear();

                    JSONArray sessions = jsonObject.getJSONArray("results");
                    for(int i = 0; i < sessions.length();i++) {
                        JSONObject session = sessions.getJSONObject(i);

                        //Split string into two varibale seperated by ","
                        String[] parts = session.getString("device_token").split(",");
                        if (parts[0].equals("ToT2")) {
                            String gameWinner_Name = parts[1];
                            String gameWinner_Score = parts[2];
                            int gameWinner_Difficulty = Integer.parseInt(parts[3]);

                            String difficulty_text;
                            if (gameWinner_Difficulty == 1){
                                difficulty_text = "Easy";
                            } else if (gameWinner_Difficulty == 2){
                                difficulty_text = "Medium";
                            } else if (gameWinner_Difficulty == 3){
                                difficulty_text = "Hard";
                            } else {
                                difficulty_text = "Easy";
                            }

                            if (sortByDifficulty == gameWinner_Difficulty) {
                                games_ArrayList.add("Name: " + gameWinner_Name + ", Score: " + gameWinner_Score + ", Difficulty: " + difficulty_text);
                            }

                            if (sortByDifficulty == 0) {
                                games_ArrayList.add("Name: " + gameWinner_Name + ", Score: " + gameWinner_Score + ", Difficulty: " + difficulty_text);
                            }
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