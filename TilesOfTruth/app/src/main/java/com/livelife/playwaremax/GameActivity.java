package com.livelife.playwaremax;

import static com.livelife.motolibrary.AntData.EVENT_PRESS;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.livelife.motolibrary.AntData;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.OnAntEventListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class GameActivity extends AppCompatActivity implements OnAntEventListener {

    MotoConnection connection = MotoConnection.getInstance();

    int randomQuestionNr;
    int numberOfQuestions = 0;
    ArrayList<Integer> answeredQuestionsNr;

    //Setup info
    int numberOfPlayers = 1;
    int difficulty = 2;
    int player1_trueTile, player2_trueTile, player3_trueTile, player4_trueTile;
    int player1_falseTile, player2_falseTile, player3_falseTile, player4_falseTile;

    // ------------------------------- //
    // Game logic/Score
    boolean gameOver = false;
    boolean newRound = false;
    int numberOfPlayersPressed = 0;
    boolean answer_bool;
    ArrayList<Integer> playerPressedList = new ArrayList<>();
    int answer_int = 0;
    int timeLeft_Round; //milliseconds
    int timeLeft_Game;  //milliseconds
    int roundScore = 0;
    int[] playerScores = {0, 0, 0, 0};
    int[] defaultArray = {0, 0}; //For calling gameLogic() when no press is detected
    CountDownTimer timerRound;
    CountDownTimer timerGame;
    // ------------------------------- //
    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_MUSIC, 40);
    boolean cancelTimerRound = false;
    boolean cancelTimerGame = false;
    //Database
    //------------------------//
    // Adaptivity stuff
    int baseRoundTimeEasy = 10000;
    int baseRoundTimeNormal = 7500;
    int baseRoundTimeHard = 5000;
    int baseGameTime = 10000;       //30000

    String questionSet = "Default Question-set";

    double adaptivityFactor = 1.0;
    String endpoint = "https://centerforplayware.com/api/index.php";
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        connection.registerListener(this);

        sharedPref = this.getApplicationContext().getSharedPreferences("PLAYWARE_COURSE", Context.MODE_PRIVATE);

        // Enable Back-button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        // Data from SetupActivity
        questionSet = getIntent().getStringExtra("question_set");

        // Get number of questions in question set
        InputStream inputStream;
        if (questionSet.equals("Default Question-set")) {
            inputStream = getResources().openRawResource(R.raw.default_questions);
        } else {
            try {
                inputStream = openFileInput("question_" + questionSet + ".csv");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        try {
            while (reader.readLine() != null) {
                numberOfQuestions++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        answeredQuestionsNr = new ArrayList<>(numberOfQuestions);

        int[] setup = getIntent().getIntArrayExtra("setup_data");
        numberOfPlayers = setup[0];
        difficulty = setup[1];

        int[] tileIDs = getIntent().getIntArrayExtra("tile_ids");
        //Toast.makeText(GameActivity.this, "Number of players: " + setup[0] + " Difficulty: " + difficulty, Toast.LENGTH_LONG).show();

        player1_trueTile = tileIDs[0];
        player1_falseTile = tileIDs[1];
        player2_trueTile = tileIDs[2];
        player2_falseTile = tileIDs[3];
        player3_trueTile = tileIDs[4];
        player3_falseTile = tileIDs[5];
        player4_trueTile = tileIDs[6];
        player4_falseTile = tileIDs[7];

        Log.d("tot", "Game configuration:");
        Log.d("tot", "player1_trueTile: " + player1_trueTile + " player1_falseTile: " + player1_falseTile + " player2_trueTile: " + player2_trueTile + " player2_falseTile: " + player2_falseTile + " player3_trueTile: " + player3_trueTile + " player3_falseTile: " + player3_falseTile + " player4_trueTile: " + player4_trueTile + " player4_falseTile: " + player4_falseTile);
        Log.d("tot", "numberOfPlayers: " + numberOfPlayers);
        Log.d("tot", "difficulty: " + difficulty);

        startTimer_Game(baseGameTime);
        try {
            gameLogic(defaultArray, false, true);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressLint("SetTextI18n")
    void gameLogic(int[] playerPressed, Boolean timeOut, Boolean firstRound) throws FileNotFoundException {

        if (answeredQuestionsNr.size() >= numberOfQuestions) {
            gameOver = true;
            gameOver();
        }

        //Get Score UI elements
        // TextViews
        TextView player1ScoreTextView = findViewById(R.id.player1scoreTextView);
        TextView player2ScoreTextView = findViewById(R.id.player2scoreTextView);
        TextView player3ScoreTextView = findViewById(R.id.player3scoreTextView);
        TextView player4ScoreTextView = findViewById(R.id.player4scoreTextView);
        TextView questionTextView = findViewById(R.id.questionTextView);

        if(firstRound) newRound = true;
        if(timeOut) newRound = true;

        // A player has pressed a tile :O
        if(playerPressed[0] != 0 && !playerPressedList.contains(playerPressed[0])) {
            numberOfPlayersPressed++;
            playerPressedList.add(playerPressed[0]);
            if (playerPressed[1] == answer_int) {
                playerScores[playerPressed[0]-1]++; //increment the player that pressed the correct tile
                roundScore++; //Increment total score of all players for this round
            }
            //Update score in UI
            runOnUiThread(() -> {
                player1ScoreTextView.setText(playerScores[0]+"");
                player2ScoreTextView.setText(playerScores[1]+"");
                player3ScoreTextView.setText(playerScores[2]+"");
                player4ScoreTextView.setText(playerScores[3]+"");
            });
        }
        if(numberOfPlayersPressed >= numberOfPlayers) newRound = true;

        if (newRound && !gameOver) {
            playerPressedList.clear();
            numberOfPlayersPressed = 0;
            newRound = false;
            // Get a new question
            do  {
                randomQuestionNr = getRandomNumber(numberOfQuestions);
            } while (answeredQuestionsNr.contains(randomQuestionNr));
            answeredQuestionsNr.add(randomQuestionNr);

            Log.d("tot", "size of answeredQuestionsNr: " + answeredQuestionsNr.size());

            Log.d("tot", "randomQuestionNr: " + randomQuestionNr);
            Log.d("tot", "numberOfQuestions: " + numberOfQuestions);

            // Question update
            String[] QuestionAnswer = getQuestionFromQuestionSet(randomQuestionNr).split(",");
            Log.d("tot", "QuestionAnswer: " + Arrays.toString(QuestionAnswer));
            String Question = QuestionAnswer[0];
            answer_bool = Boolean.parseBoolean(QuestionAnswer[1]);
            answer_int = answer_bool ? 1 : 0;

            runOnUiThread(() -> questionTextView.setText(Question));

            //Toast.makeText(GameActivity.this, "Question: " + Question + ", Answer: " + answer_int, Toast.LENGTH_LONG).show();

            SetupActivity.textToSpeechSystem.speak(Question, TextToSpeech.QUEUE_FLUSH, null, "ID");
            if (timerRound != null) timerRound.cancel(); //Cancel previous timer

            startTimer_Round(getAdaptiveRoundTime(roundScore));
            roundScore = 0;

        }
    }

    int getAdaptiveRoundTime(int prevRoundScore) {
        int roundTime = 10000;

        if (prevRoundScore/numberOfPlayers == 1 && adaptivityFactor > 0.5) { // corrections added
            // for preventing the roundTimer from going to zero and breaking GameActivity
            adaptivityFactor -= 0.1;
        }
        else if (prevRoundScore/numberOfPlayers != 1) {
            adaptivityFactor += 0.1;
        }
        //Easy mode
        if (difficulty == 1) {
            roundTime = (int) (adaptivityFactor * baseRoundTimeEasy);
        }
        else if (difficulty == 2) {
            roundTime = (int) (adaptivityFactor * baseRoundTimeNormal);
        }
        else if (difficulty == 3) {
            roundTime = (int) (adaptivityFactor * baseRoundTimeHard);
        }

        return roundTime;
    }

    void startTimer_Round(int time) {
        TextView timerRound_TextView = findViewById(R.id.roundTimeTextView);
        TextView timerGame_TextView = findViewById(R.id.gameTimeTextView);

        timerRound = new CountDownTimer(time, 1000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                if(cancelTimerRound) { cancel(); }

                timeLeft_Round = (int) (millisUntilFinished / 1000);

                runOnUiThread(() -> {
                    timerRound_TextView.setText("Round: " + timeLeft_Round + "s");
                    timerGame_TextView.setText("Game: " + timeLeft_Game + "s");
                });

                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            }

            @SuppressLint("SetTextI18n")
            public void onFinish() {
                runOnUiThread(() -> timerRound_TextView.setText("Round Over"));

                try {
                    gameLogic(defaultArray, true,false);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

        };
        timerRound.start();
    }

    void startTimer_Game(int time) {
        TextView timer = findViewById(R.id.gameTimeTextView);
        timerGame = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                if(cancelTimerGame) { cancel(); }
                timeLeft_Game = (int) (millisUntilFinished / 1000);
            }

            @SuppressLint("SetTextI18n")
            public void onFinish() {
                timer.setText("Game Over");
                gameOver();
            }

        };
        timerGame.start();
    }

    void gameOver() {
        Log.d("tot", "Game over");
        connection.unregisterListener(this);
        //Find the highest number of playerScores and the player with that score
        int maxScore = 0;
        int maxScorePlayer = 0;
        for (int i = 0; i < playerScores.length; i++) {
            if (playerScores[i] > maxScore) {
                maxScore = playerScores[i];
                maxScorePlayer = i;
            }
        }
        gameOver = true;
        stopTimer();
        SetupActivity.textToSpeechSystem.speak("Game over", TextToSpeech.QUEUE_FLUSH, null, "ID");

        AlertDialog.Builder gameOver_builder = new AlertDialog.Builder(this);
        //gameOver_AlertDialog.setTitle("Player " + (maxScorePlayer+1) + " won this game with " + maxScore + " points");
        gameOver_builder.setView(R.layout.dialog_gameover);
        AlertDialog gameOver_AlertDialog = gameOver_builder.create();
        gameOver_AlertDialog.setCancelable(false);
        gameOver_AlertDialog.show();

        EditText winnerEditText = gameOver_AlertDialog.findViewById(R.id.winnerEditText);
        Button enterButton = gameOver_AlertDialog.findViewById(R.id.enterButton);
        Button cancelButton = gameOver_AlertDialog.findViewById(R.id.cancelButton);
        TextView dialogHighscoreTextView = gameOver_AlertDialog.findViewById(R.id.highscoreTextView);
        TextView dialogPlayer1ScoreTextView = gameOver_AlertDialog.findViewById(R.id.player1scoreTextView);
        TextView dialogPlayer2ScoreTextView = gameOver_AlertDialog.findViewById(R.id.player2scoreTextView);
        TextView dialogPlayer3ScoreTextView = gameOver_AlertDialog.findViewById(R.id.player3scoreTextView);
        TextView dialogPlayer4ScoreTextView = gameOver_AlertDialog.findViewById(R.id.player4scoreTextView);
        dialogHighscoreTextView.setText("This game's highscore is: " + maxScore);
        dialogPlayer1ScoreTextView.setText(playerScores[0]+"");
        dialogPlayer2ScoreTextView.setText(playerScores[1]+"");
        dialogPlayer3ScoreTextView.setText(playerScores[2]+"");
        dialogPlayer4ScoreTextView.setText(playerScores[3]+"");


        int finalMaxScore = maxScore;
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkIfDeviceIsConnectedToInternet()) {
                    postGameWinner(winnerEditText.getText().toString(), finalMaxScore);
                }
                gameOver_AlertDialog.cancel();
                finish(); //Go back to previous activity
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameOver_AlertDialog.cancel();
                finish(); //Go back to previous activity
            }
        });

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

    // ------------------------------- //
    // For going back to previous activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            stopTimer();
            SetupActivity.textToSpeechSystem.stop();
            connection.unregisterListener(this);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    void stopTimer() {
        cancelTimerRound = true;
        cancelTimerGame = true;
        timerRound.cancel();
        timerGame.cancel();
        toneG.stopTone();
        //toneG.release();
    }

    @Override
    public void onMessageReceived(byte[] bytes, long l) {

        int command = AntData.getCommand(bytes);
        int tileId = AntData.getId(bytes);
        int[] pressArray = {0,0};
        //int color = AntData.getColorFromPress(bytes);

        if(command == EVENT_PRESS) {
            Log.d("tile_press", "tileID: " + tileId);

            if (tileId == player1_trueTile) {
                Log.d("tile_press", "Player 1 True Tile pressed");
                pressArray[0] = 1;
                pressArray[1] = 1;
            } else if (tileId == player1_falseTile) {
                Log.d("tile_press", "Player 1 False Tile pressed");
                pressArray[0] = 1;
                pressArray[1] = 0;
            } else if (tileId == player2_trueTile) {
                Log.d("tile_press", "Player 2 True Tile pressed");
                pressArray[0] = 2;
                pressArray[1] = 1;
            } else if (tileId == player2_falseTile) {
                Log.d("tile_press", "Player 2 False Tile pressed");
                pressArray[0] = 2;
                pressArray[1] = 0;
            } else if (tileId == player3_trueTile) {
                Log.d("tile_press", "Player 3 True Tile pressed");
                pressArray[0] = 3;
                pressArray[1] = 1;
            } else if (tileId == player3_falseTile) {
                Log.d("tile_press", "Player 3 False Tile pressed");
                pressArray[0] = 3;
                pressArray[1] = 0;
            } else if (tileId == player4_trueTile) {
                Log.d("tile_press", "Player 4 True Tile pressed");
                pressArray[0] = 4;
                pressArray[1] = 1;
            } else if (tileId == player4_falseTile) {
                Log.d("tile_press", "Player 4 False Tile pressed");
                pressArray[0] = 4;
                pressArray[1] = 0;
            } else {
                Log.d("tile_press", "ERROR: Tile not found");
            }
            try {
                gameLogic(pressArray,false,false);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ------------------------------- //
    public int getRandomNumber(int max) {
        return new Random().nextInt((max - 1) + 1) + 1;
    }

    // ------------------------------- //
    public String getQuestionFromQuestionSet(int lineNr) {
        try {
            InputStream inputStream;

            Log.d("tag", "questionSet: " + questionSet);

            if (questionSet.equals("Default Question-set")) {
                inputStream = getResources().openRawResource(R.raw.default_questions);
            } else {
                inputStream = openFileInput("question_" + questionSet + ".csv");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            Log.d("tag", "reader: " + reader);
            Log.d("tag", "lineNr: " + lineNr);

            String question = "";
            // Skip lines until the lineNr is reached and return the question
            for(int i = 0; i < lineNr; ++i)
                question = reader.readLine();
            reader.close();
            Log.d("tag", "question: " + question);
            return question;
        } catch (Resources.NotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onAntServiceConnected() {
        connection.setAllTilesToInit();
    }

    @Override
    public void onNumbersOfTilesConnected(int i) {}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopTimer();
        SetupActivity.textToSpeechSystem.stop();
        connection.unregisterListener(this);
        finish();
    }

    private void postGameWinner(String gameWinner, int score) {
        Log.d("tot", "maxScore: " + score);
        Log.d("tot", "gameWinner: " + gameWinner);

        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("POST");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method", "postGameSession");
        requestPackage.setParam("device_token", "Winner2," + gameWinner + "," + score + "," + difficulty + "," + getDeviceToken());
        requestPackage.setParam("game_time","30");
        requestPackage.setParam("game_id", "1");
        requestPackage.setParam("group_id", "420");
        requestPackage.setParam("game_type_id", "1");
        requestPackage.setParam("game_score", "10");

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

    private static class Downloader extends AsyncTask<RemoteHttpRequest, String, String> {
        @Override
        protected String doInBackground(RemoteHttpRequest... params) {
            return HttpManager.getData(params[0]);
        }
    }

}