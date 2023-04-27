package com.livelife.playwaremax;

import static com.livelife.motolibrary.AntData.EVENT_PRESS;
import static com.livelife.motolibrary.AntData.LED_COLOR_GREEN;
import static com.livelife.motolibrary.AntData.LED_COLOR_RED;

import android.content.res.Resources;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.livelife.motolibrary.AntData;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.OnAntEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements OnAntEventListener {

    MotoConnection connection = MotoConnection.getInstance();

    int randomQuestionNr;
    int numberOfQuestions = 1000;
    ArrayList<Integer> answeredQuestionsNr = new ArrayList<>(numberOfQuestions);
    private TextToSpeech textToSpeechSystem;

    //Setup info
    int numberOfPlayers = 1;
    int difficulty = 1;
    int player1_trueTile, player2_trueTile, player3_trueTile, player4_trueTile;
    int player1_falseTile, player2_falseTile, player3_falseTile, player4_falseTile;



    // ------------------------------- //
    // Game logic/Score
    int timeLeft_Round; //milliseconds
    int timeLeft_Game;  //milliseconds
    boolean Answer;
    int player1_score = 0; int player2_score = 0; int player3_score = 0; int player4_score = 0;
    int[] defaultArray = {0, 0}; //For calling gamelogic() when no press is detected
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Enable Back-button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Data from SetupActivity
        int[] setup = getIntent().getIntArrayExtra("setup_data");
        numberOfPlayers = setup[0];
        difficulty = setup[1];

        int[] tileIDs = getIntent().getIntArrayExtra("tile_ids");
        Toast.makeText(GameActivity.this, "Number of players: " + setup[0] + " Difficulty: " + setup[1], Toast.LENGTH_LONG).show();

        player1_trueTile = tileIDs[0];
        player1_falseTile = tileIDs[1];
        player2_trueTile = tileIDs[2];
        player2_falseTile = tileIDs[3];
        player3_trueTile = tileIDs[4];
        player3_falseTile = tileIDs[5];
        player4_trueTile = tileIDs[6];
        player4_falseTile = tileIDs[7];

        gameLogic(defaultArray);
    }
    void gameLogic(int[] playerPressed) {

        do  {
            randomQuestionNr = getRandomNumber(numberOfQuestions);
        } while (answeredQuestionsNr.contains(randomQuestionNr));
        answeredQuestionsNr.add(randomQuestionNr);

        // Question update
        String[] QuestionAnswer = getQuestionFromCSV(randomQuestionNr).split(",");
        String Question = QuestionAnswer[0];
        boolean Answer = Boolean.parseBoolean(QuestionAnswer[1]);
        TextView questionTextView = findViewById(R.id.questionTextView);
        questionTextView.setText(Question);
        //Toast.makeText(GameActivity.this, "Question: " + Question + ", Answer: " + Answer, Toast.LENGTH_LONG).show();
        textToSpeech(Question);

        startTimer_Round();
        startTimer_Game();

    }

    void startTimer_Round() {
        TextView timer = findViewById(R.id.timer);
        new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeft_Round = (int) (millisUntilFinished / 1000);
                timer.setText("Round: " + timeLeft_Round + ", Game: " + timeLeft_Game);
                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 40);
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            }

            public void onFinish() {
                timer.setText("Round Over");
            }

        }.start();
    }

    void startTimer_Game() {
        TextView timer = findViewById(R.id.timer);
        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeft_Game = (int) (millisUntilFinished / 1000);
            }

            public void onFinish() {
                timer.setText("Game Over");
            }

        }.start();
    }

    // ------------------------------- //
    // For going back to previous activity
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

    @Override
    public void onMessageReceived(byte[] bytes, long l) {

        int command = AntData.getCommand(bytes);
        int tileId = AntData.getId(bytes);
        int[] pressArray = {0,0};
        //int color = AntData.getColorFromPress(bytes);

        if(command == EVENT_PRESS) {
            Log.d("tag", "tileID: " + tileId);

            if (tileId == player1_trueTile) {
                Log.d("tag", "Player 1 True Tile pressed");
                pressArray[0] = 1;
                pressArray[1] = 1;
            } else if (tileId == player1_falseTile) {
                Log.d("tag", "Player 1 False Tile pressed");
                pressArray[0] = 1;
                pressArray[1] = 0;
            } else if (tileId == player2_trueTile) {
                Log.d("tag", "Player 2 True Tile pressed");
                pressArray[0] = 2;
                pressArray[1] = 1;
            } else if (tileId == player2_falseTile) {
                Log.d("tag", "Player 2 False Tile pressed");
                pressArray[0] = 2;
                pressArray[1] = 0;
            } else if (tileId == player3_trueTile) {
                Log.d("tag", "Player 3 True Tile pressed");
                pressArray[0] = 3;
                pressArray[1] = 1;
            } else if (tileId == player3_falseTile) {
                Log.d("tag", "Player 3 False Tile pressed");
                pressArray[0] = 3;
                pressArray[1] = 0;
            } else if (tileId == player4_trueTile) {
                Log.d("tag", "Player 4 True Tile pressed");
                pressArray[0] = 4;
                pressArray[1] = 1;
            } else if (tileId == player4_falseTile) {
                Log.d("tag", "Player 4 False Tile pressed");
                pressArray[0] = 4;
                pressArray[1] = 0;
            } else {
                Log.d("tag", "ERROR: Tile not found");
            }
            gameLogic(pressArray);
        }
    }

    // ------------------------------- //
    public int getRandomNumber(int max) {
        return new Random().nextInt(max) + 1;
    }

    // ------------------------------- //
    public String getQuestionFromCSV(int lineNr) {
        try {
            InputStream is = getResources().openRawResource(R.raw.questions);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            for(int i = 0; i < lineNr-1; ++i)
                reader.readLine();
            return reader.readLine();

        } catch (Resources.NotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ------------------------------- //
    // Text to Speech
    public void textToSpeech(String textToSay) {
        textToSpeechSystem = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeechSystem.speak(textToSay, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    @Override
    public void onAntServiceConnected() {
        connection.setAllTilesToInit();
    }

   @Override
    public void onNumbersOfTilesConnected(int i) {
        //TextView connectedTextView = findViewById(R.id.connectedTextView);
        //connectedTextView.setText("Tiles connected: "+i);
    }
}