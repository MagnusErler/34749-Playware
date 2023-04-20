package com.livelife.playwaremax;

import static com.livelife.motolibrary.AntData.EVENT_PRESS;
import static com.livelife.motolibrary.AntData.LED_COLOR_GREEN;
import static com.livelife.motolibrary.AntData.LED_COLOR_OFF;
import static com.livelife.motolibrary.AntData.LED_COLOR_RED;

import android.content.res.Resources;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

    MotoConnection connection = MotoConnection.getInstance();;
    int randomQuestionNr;
    int numberOfQuestions = 1000;
    ArrayList<Integer> answeredQuestionsNr= new ArrayList<>(numberOfQuestions);
    private TextToSpeech textToSpeechSystem;

    int player1_trueTile, player2_trueTile, player3_trueTile, player4_trueTile;
    int player1_falseTile, player2_falseTile, player3_falseTile, player4_falseTile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Back-button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        int setup_ga[] = getIntent().getIntArrayExtra("setup_data");
        Toast.makeText(GameActivity.this, "Setup_ga[0]: " + setup_ga[0] + " Setup_ga[1]: " + setup_ga[1] + " Setup_ga[2]: " + setup_ga[2], Toast.LENGTH_LONG).show();
        connection.registerListener(this);
        connection.setAllTilesToInit();


        do  {
            randomQuestionNr = getRandomNumber(numberOfQuestions);
        } while (answeredQuestionsNr.contains(randomQuestionNr));

        answeredQuestionsNr.add(randomQuestionNr);

        String[] separated = getQuestionFromCSV(randomQuestionNr).split(",");
        String Question = separated[0];
        boolean Answer = Boolean.parseBoolean(separated[1]);
        Toast.makeText(GameActivity.this, "Question: " + Question + ", Answer: " + Answer, Toast.LENGTH_LONG).show();
        textToSpeech(Question);

        player1_trueTile = connection.randomIdleTile();
        player2_trueTile = connection.randomIdleTile();
        player3_trueTile = connection.randomIdleTile();
        player4_trueTile = connection.randomIdleTile();
        player1_falseTile = connection.randomIdleTile();
        player2_falseTile = connection.randomIdleTile();
        player3_falseTile = connection.randomIdleTile();
        player4_falseTile = connection.randomIdleTile();

        int numberOfPlayers = 1;
        setupTilesPosition(numberOfPlayers);
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

    public void setupTilesPosition(int numberOfPlayers) {
        connection.setAllTilesIdle(LED_COLOR_OFF);

        switch (numberOfPlayers) {
            case 1:
                connection.setTileNumLeds(LED_COLOR_GREEN, player1_trueTile, 1);
                connection.setTileNumLeds(LED_COLOR_RED, player1_falseTile, 1);
                break;
            case 2:
                connection.setTileNumLeds(LED_COLOR_GREEN, player1_trueTile, 1);
                connection.setTileNumLeds(LED_COLOR_RED, player1_falseTile, 1);
                connection.setTileNumLeds(LED_COLOR_GREEN, player2_trueTile, 2);
                connection.setTileNumLeds(LED_COLOR_RED, player2_falseTile, 2);
                break;
            case 3:
                connection.setTileNumLeds(LED_COLOR_GREEN, player1_trueTile, 1);
                connection.setTileNumLeds(LED_COLOR_RED, player1_falseTile, 1);
                connection.setTileNumLeds(LED_COLOR_GREEN, player2_trueTile, 2);
                connection.setTileNumLeds(LED_COLOR_RED, player2_falseTile, 2);
                connection.setTileNumLeds(LED_COLOR_GREEN, player3_trueTile, 3);
                connection.setTileNumLeds(LED_COLOR_RED, player3_falseTile, 3);
                break;
            case 4:
                connection.setTileNumLeds(LED_COLOR_GREEN, player1_trueTile, 1);
                connection.setTileNumLeds(LED_COLOR_RED, player1_falseTile, 1);
                connection.setTileNumLeds(LED_COLOR_GREEN, player2_trueTile, 2);
                connection.setTileNumLeds(LED_COLOR_RED, player2_falseTile, 2);
                connection.setTileNumLeds(LED_COLOR_GREEN, player3_trueTile, 3);
                connection.setTileNumLeds(LED_COLOR_RED, player3_falseTile, 3);
                connection.setTileNumLeds(LED_COLOR_GREEN, player4_trueTile, 4);
                connection.setTileNumLeds(LED_COLOR_RED, player4_falseTile, 4);
                break;
            default:
                Log.d("tag", "ERROR: Wrong amount of players");
                break;
        }
    }

    @Override
    public void onMessageReceived(byte[] bytes, long l) {

        int command = AntData.getCommand(bytes);
        int tileId = AntData.getId(bytes);
        int color = AntData.getColorFromPress(bytes);

        if(command == EVENT_PRESS) {
            Log.d("tag", "tileID: " + tileId);
        }

    }

    @Override
    public void onAntServiceConnected() {

    }

    @Override
    public void onNumbersOfTilesConnected(int i) {

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
}