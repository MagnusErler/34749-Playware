package com.livelife.playwaremax;

import static com.livelife.motolibrary.AntData.EVENT_PRESS;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
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
    boolean gameOver = false;
    boolean newRound = false;
    int numberOfPlayersPressed = 0;
    boolean answer_bool;
    ArrayList<Integer> playerPressedList = new ArrayList<>();
    int answer_int = 0;
    int timeLeft_Round; //milliseconds
    int timeLeft_Game;  //milliseconds
    int[] playerScores = {0, 0, 0, 0};
    int[] defaultArray = {0, 0}; //For calling gameLogic() when no press is detected
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

        //gameOver();

        startTimer_Game(30000);
        gameLogic(defaultArray, false, true);
    }
    void gameLogic(int[] playerPressed, Boolean timeOut,Boolean firstRound) {

        if(firstRound) newRound = true;
        if(timeOut) newRound = true;

        // A player has pressed a tile :O
        if(playerPressed[0] != 0 && !playerPressedList.contains(playerPressed[0])) {
            numberOfPlayersPressed++;
            playerPressedList.add(playerPressed[0]);
            if (playerPressed[1] == answer_int) {
                playerScores[playerPressed[0]]++; //increment the player that pressed the correct tile
            }
        }
        if(numberOfPlayersPressed == numberOfPlayers) newRound = true;

        if (newRound && !gameOver) {
            playerPressedList.clear();
            newRound = false;
            // Get a new question
            do  {
                randomQuestionNr = getRandomNumber(numberOfQuestions);
            } while (answeredQuestionsNr.contains(randomQuestionNr));
            answeredQuestionsNr.add(randomQuestionNr);

            // Question update
            String[] QuestionAnswer = getQuestionFromCSV(randomQuestionNr).split(",");
            String Question = QuestionAnswer[0];
            answer_bool = Boolean.parseBoolean(QuestionAnswer[1]);
            answer_int = answer_bool ? 1 : 0;
            TextView questionTextView = findViewById(R.id.questionTextView);
            questionTextView.setText(Question);
            //Toast.makeText(GameActivity.this, "Question: " + Question + ", Answer: " + Answer, Toast.LENGTH_LONG).show();
            textToSpeech(Question);

            startTimer_Round(10000);
        }
    }

    void startTimer_Round(int time) {
        TextView timer = findViewById(R.id.roundTimeTextView);
        CountDownTimer timerRound = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeft_Round = (int) (millisUntilFinished / 1000);
                timer.setText("Round: " + timeLeft_Round + ", Game: " + timeLeft_Game);
                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 40);
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            }

            public void onFinish() {
                timer.setText("Round Over");
                gameLogic(defaultArray, true,false);
            }

        };
        timerRound.start();
    }

    void startTimer_Game(int time) {
        TextView timer = findViewById(R.id.gameTimeTextView);
        CountDownTimer timerGame = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeft_Game = (int) (millisUntilFinished / 1000);
            }

            public void onFinish() {
                timer.setText("Game Over");
                gameOver();
            }

        };
        timerGame.start();
    }

    void gameOver() {
        gameOver = true;
        AlertDialog.Builder gameOver_AlertDialog = new AlertDialog.Builder(this);
        gameOver_AlertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        gameOver_AlertDialog.setTitle("Player: XX" + " won this round!");
        gameOver_AlertDialog.setMessage("Please fill in you name for the score board");

        final EditText input = new EditText(GameActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        gameOver_AlertDialog.setView(input);
        String gameOver_WinnerOfGame = input.getText().toString();

        gameOver_AlertDialog.setPositiveButton("Enter", (dialogInterface, i) -> {
                    //set what would happen when positive button is clicked
                    finish();
                });
        gameOver_AlertDialog.setNegativeButton("No", (dialogInterface, i) -> {
                    //set what should happen when negative button is clicked
                    Toast.makeText(getApplicationContext(), "Nothing Happened", Toast.LENGTH_LONG).show();
                });
        gameOver_AlertDialog.show();
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
            gameLogic(pressArray,false,false);
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