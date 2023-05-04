package com.livelife.playwaremax;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class ScoreboardActivity extends AppCompatActivity {

    ListView gameSessions_ListView;
    ArrayAdapter<String> gameSessions_ArrayAdapter;
    ArrayList<String> games_ArrayList = new ArrayList<>();

    boolean showChallenges = false;

    String challenge_difficulty_text;
    String challenge_deviceTokenFromChallenger;

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
            //Toast.makeText(this, "games_ArrayList.get(position): " + games_ArrayList.get(position), Toast.LENGTH_SHORT).show();
            //games_ArrayList.get(position);

            // get text from listview at position "position" and split it into an array
            String[] gameSession = games_ArrayList.get(position).split(",");
            String name = gameSession[0];
            //get name after "Name: "
            name = name.substring(6);
            String score = gameSession[1];
            String difficulty = gameSession[2];
            String deviceToken = gameSession[3];
            //get deviceToken after "DeviceToken: "
            deviceToken = deviceToken.substring(14);

            Log.d("tot", "name: " + name + " deviceToken: " + deviceToken);

            showChallengeUser(name, deviceToken);
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

    void showChallengeUser(String nameOfUserToBeChallenged, String deviceTokenOfUserToBeChallenged) {
        AlertDialog.Builder challengeUser_builder = new AlertDialog.Builder(this);
        challengeUser_builder.setView(R.layout.dialog_challenge_user);
        AlertDialog challengeUser_AlertDialog = challengeUser_builder.create();
        challengeUser_AlertDialog.setCancelable(true);
        challengeUser_AlertDialog.setCanceledOnTouchOutside(true);
        challengeUser_AlertDialog.show();

        TextView challenge_player_title_TextView = challengeUser_AlertDialog.findViewById(R.id.challenge_player_title_TextView);
        challenge_player_title_TextView.setText("Challenge " + nameOfUserToBeChallenged);

        Button challenge_enter_btn = challengeUser_AlertDialog.findViewById(R.id.challenge_accept_btn);
        RadioGroup rg = challengeUser_AlertDialog.findViewById(R.id.challenge_difficultyRadioGroup);
        challenge_enter_btn.setOnClickListener(view -> {

            int checkedId = rg.getCheckedRadioButtonId();

            int difficulty = 0;
            switch(checkedId){
                case R.id.challenge_easy_btn:
                    difficulty = 1;
                    break;
                case R.id.challenge_normal_btn:
                    difficulty = 2;
                    break;
                case R.id.challenge_hard_btn:
                    difficulty = 3;
                    break;
            }

            postChallengeUser(difficulty, nameOfUserToBeChallenged, deviceTokenOfUserToBeChallenged);

            challengeUser_AlertDialog.cancel();
        });
    }

    void postChallengeUser(int difficulty, String nameOfUserToBeChallenged, String deviceTokenOfUserToBeChallenged) {

        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("POST");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method", "postGameSession");
        requestPackage.setParam("device_token", "Challenge," + difficulty + "," + nameOfUserToBeChallenged + "," + deviceTokenOfUserToBeChallenged);

        requestPackage.setParam("game_time","30");
        requestPackage.setParam("game_id", "1");
        requestPackage.setParam("group_id", "420");
        requestPackage.setParam("game_type_id", "1");
        requestPackage.setParam("game_score", "10");

        ScoreboardActivity.Downloader downloader = new ScoreboardActivity.Downloader();

        downloader.execute(requestPackage);
    }

    String[] getAllQuestionSets() {
        String[] questionSets = new String[Objects.requireNonNull(getFilesDir().listFiles()).length + 1];
        questionSets[0] = "Default Question-set";
        int numQuestionSets = 1;
        File[] files = getFilesDir().listFiles();
        assert files != null;
        for (File file : files) {
            if (file.getName().startsWith("question_")) {
                questionSets[numQuestionSets] = file.getName().replace("question_", "").replace(".csv", "");
                numQuestionSets++;
            }
        }
        return Arrays.copyOf(questionSets, numQuestionSets);
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

        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        if (id == R.id.getChallenges_MenuItem) {
            showIncomingChallenges();
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_get_challenges, menu);
        return true;
    }

    void showIncomingChallenges() {
        showChallenges = true;

        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("GET");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method", "getGameSessions"); // The method name
        requestPackage.setParam("device_token", getDeviceToken()); // Your device token
        requestPackage.setParam("group_id", "420"); // Your group ID

        Downloader downloader = new Downloader();

        downloader.execute(requestPackage);
    }

    private void getGameWinner() {
        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("GET");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method", "getGameSessions"); // The method name
        //requestPackage.setParam("device_token", getDeviceToken()); // Your device token
        requestPackage.setParam("group_id", "420"); // Your group ID

        Downloader downloader = new Downloader();

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
                        // Winner og a game
                        if (parts[0].equals("Winner1")) {
                            String gameWinner_Name = parts[1];
                            String gameWinner_Score = parts[2];
                            int gameWinner_Difficulty = Integer.parseInt(parts[3]);
                            String deviceToken = parts[4];

                            String difficulty_text;
                            if (gameWinner_Difficulty == 1){
                                difficulty_text = "Easy";
                            } else if (gameWinner_Difficulty == 2){
                                difficulty_text = "Normal";
                            } else if (gameWinner_Difficulty == 3){
                                difficulty_text = "Hard";
                            } else {
                                difficulty_text = "Easy";
                            }

                            if (sortByDifficulty == gameWinner_Difficulty) {
                                games_ArrayList.add("Name: " + gameWinner_Name + ", Score: " + gameWinner_Score + ", Difficulty: " + difficulty_text + ", DeviceToken: " + deviceToken);
                            }

                            if (sortByDifficulty == 0) {
                                games_ArrayList.add("Name: " + gameWinner_Name + ", Score: " + gameWinner_Score + ", Difficulty: " + difficulty_text + ", DeviceToken: " + deviceToken);
                            }
                        }

                        // Posting a challenge
                        if (showChallenges) {
                            showChallenges = false;
                            if (parts[0].equals("Challenge")) {

                                int callenge_difficulty = Integer.parseInt(parts[1]);
                                String challenge_name = parts[2];
                                challenge_deviceTokenFromChallenger = parts[3];

                                Log.d("tot", "parts[1]: " + parts[1] + " parts[2]: " + parts[2] + " parts[3]: " + challenge_deviceTokenFromChallenger);
                                
                                if (Objects.equals(challenge_deviceTokenFromChallenger, getDeviceToken())) {
                                    Log.d("tot", "deviceToken are the same");
                                    Toast.makeText(ScoreboardActivity.this, "deviceToken are the same", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (callenge_difficulty == 1) {
                                    challenge_difficulty_text = "Easy";
                                } else if (callenge_difficulty == 2) {
                                    challenge_difficulty_text = "Normal";
                                } else if (callenge_difficulty == 3) {
                                    challenge_difficulty_text = "Hard";
                                } else {
                                    challenge_difficulty_text = "Easy";
                                }

                                AlertDialog.Builder challengeUser_builder = new AlertDialog.Builder(ScoreboardActivity.this);
                                challengeUser_builder.setView(R.layout.dialog_accepting_challenges);
                                AlertDialog acceptChallengeUser_AlertDialog = challengeUser_builder.create();
                                acceptChallengeUser_AlertDialog.setCancelable(true);
                                acceptChallengeUser_AlertDialog.setCanceledOnTouchOutside(true);
                                acceptChallengeUser_AlertDialog.show();

                                TextView accepting_challenge_TextView = acceptChallengeUser_AlertDialog.findViewById(R.id.accepting_challenge_TextView);
                                accepting_challenge_TextView.setText("You have been challenged by " + challenge_name + " on " + challenge_difficulty_text + " difficulty!");

                                Button accepting_challenge_cancelButton = acceptChallengeUser_AlertDialog.findViewById(R.id.accepting_challenge_cancelButton);
                                Button accepting_challenge_enterButton = acceptChallengeUser_AlertDialog.findViewById(R.id.accepting_challenge_enterButton);
                                accepting_challenge_cancelButton.setOnClickListener(view -> {
                                    acceptChallengeUser_AlertDialog.cancel();
                                });

                                accepting_challenge_enterButton.setOnClickListener(view -> {
                                    Intent intent = new Intent(ScoreboardActivity.this, SetupActivity.class);
                                    intent.putExtra("difficulty", callenge_difficulty);
                                    intent.putExtra("challenge_accepted", true);
                                    startActivity(intent);

                                    acceptChallengeUser_AlertDialog.cancel();
                                });
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