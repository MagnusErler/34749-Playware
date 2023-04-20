package com.livelife.playwaremax;

import static com.livelife.motolibrary.AntData.EVENT_PRESS;
import static com.livelife.motolibrary.AntData.LED_COLOR_GREEN;
import static com.livelife.motolibrary.AntData.LED_COLOR_OFF;
import static com.livelife.motolibrary.AntData.LED_COLOR_RED;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    MotoConnection connection;
    MotoSound sound;

    Button pairingButton;
    Button startGameButton;
    Button simulateGetGameSessions,simulatePostGameSession, simulatePostGameChallenge, simulateGetGameChallenge;
    TextView connectedTextView;

    // ------ Added by us ------

    int numberOfPlayers = 1;

    int trueTile, falseTile;
    int player1_trueTile, player2_trueTile, player3_trueTile, player4_trueTile;
    int player1_falseTile, player2_falseTile, player3_falseTile, player4_falseTile;

    int numberOfQuestions = 1000;

    int randomQuestionNr;

    ArrayList<Integer> answeredQuestionsNr= new ArrayList<>(numberOfQuestions);

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
        setContentView(R.layout.activity_main);

        //checkIfDeviceIsConnectedToInternet();

        //sharedPref = this.getApplicationContext().getSharedPreferences("PLAYWARE_COURSE", Context.MODE_PRIVATE);

        //gameSessions_ListView = findViewById(R.id.gameSessions_ListView);

        //gameSessions_ArrayAdapter = new ArrayAdapter<String>(this,
        //        android.R.layout.simple_list_item_1, games_ArrayList);

        //gameSessions_ListView.setAdapter(gameSessions_ArrayAdapter);

        /*connection = MotoConnection.getInstance();
        sound = MotoSound.getInstance();

        connection.startMotoConnection(this);
        connection.saveRfFrequency(66);
        connection.setDeviceId(2);
        connection.registerListener(this);*/

        //createChallenge_Btn = findViewById(R.id.createChallenge_Btn);

        //createChallenge_Btn.setOnClickListener(v -> createChallenge());

        //apiOutput = findViewById(R.id.apiOutput);
        //connectedTextView = findViewById(R.id.connectedTextView);

        Button play_Btn = findViewById(R.id.playButton);
        play_Btn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SetupActivity.class)));

        /*startGameButton.setOnClickListener(v -> {
            if(!isPlaying) {
                startGameButton.setText("STOP GAME");
                isPlaying = true;
                connection.setAllTilesIdle(LED_COLOR_OFF);

            } else {
                startGameButton.setText("START GAME");
                isPlaying = false;
                connection.setAllTilesToInit();
            }

            do  {
                randomQuestionNr = getRandomNumber(numberOfQuestions);
            } while (answeredQuestionsNr.contains(randomQuestionNr));

            answeredQuestionsNr.add(randomQuestionNr);

            String[] separated = getQuestionFromCSV(randomQuestionNr).split(",");
            String Question = separated[0];
            boolean Answer = Boolean.parseBoolean(separated[1]);
            Toast.makeText(MainActivity.this, "Question: " + Question + ", Answer: " + Answer, Toast.LENGTH_LONG).show();
            textToSpeech(Question);

        });*/


    }

}