package com.livelife.playwaremax;

import static com.livelife.motolibrary.AntData.EVENT_PRESS;
import static com.livelife.motolibrary.AntData.LED_COLOR_GREEN;
import static com.livelife.motolibrary.AntData.LED_COLOR_OFF;
import static com.livelife.motolibrary.AntData.LED_COLOR_RED;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.livelife.motolibrary.AntData;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.MotoSound;
import com.livelife.motolibrary.OnAntEventListener;

import java.net.InetAddress;
import java.util.Objects;

public class SetupActivity extends AppCompatActivity implements OnAntEventListener {

    // ------ Moto ------
    MotoConnection connection;
    MotoSound sound;

    // ------ TextView ------
    TextView numberOfPlayersTextView;

    int connectedTiles = 0;

    RadioGroup playersRadioGroup;

    ImageView positioningImageView;

    int numberOfPlayers = 1;
    int difficulty = 1;

    int player1_trueTile = 0, player2_trueTile = 0, player3_trueTile = 0, player4_trueTile = 0;
    int player1_falseTile = 0, player2_falseTile = 0, player3_falseTile = 0, player4_falseTile = 0;

    private TextToSpeech textToSpeechSystem;
    // -------------------------

    int setupMode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        //Clear previous connection


        setTitle("Setup Game");

        // Enable Back-button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        checkIfDeviceIsConnectedToInternet();

        connection = MotoConnection.getInstance();
        sound = MotoSound.getInstance();

        connection.stopMotoConnection();
        connection.startMotoConnection(this);
        
        connection.saveRfFrequency(66);
        connection.setDeviceId(2);
        connection.registerListener(this);

        // ------ Difficulty ------
        Button easyDifficultyButton = findViewById(R.id.easyDifficultyButton);
        Button normalDifficultyButton = findViewById(R.id.normalDifficultyButton);
        Button hardDifficultyButton = findViewById(R.id.hardDifficultyButton);

        easyDifficultyButton.setOnClickListener(v -> {
            difficulty = 1;
        });

        normalDifficultyButton.setOnClickListener(v -> {
            difficulty = 2;
        });

        hardDifficultyButton.setOnClickListener(v -> {
            difficulty = 3;
        });

        // ------ Pairing Tiles ------
        Button pairingButton = findViewById(R.id.pairButton);
        pairingButton.setOnClickListener(v -> {
            switch(setupMode) {
                case 1:
                    //Starting pairing tiles -> tiles a spinning
                    connection.pairTilesStart();
                    textToSpeech("Press " + numberOfPlayers*2 + "tiles you want to use");
                    pairingButton.setText("Next");
                    setupMode = 2;
                    break;
                case 2:
                    //Stopping pairing tiles -> tiles are OFF
                    connection.pairTilesStop();
                    //playersRadioGroup.setVisibility(View.VISIBLE);
                    //numberOfPlayersTextView.setVisibility(View.VISIBLE);
                    //tilesPositioningTextView.setVisibility(View.VISIBLE);
                    //positioningImageView.setVisibility(View.VISIBLE);
                    setupTilesPosition(numberOfPlayers);
                    textToSpeech("Place the " + numberOfPlayers*2 + " tiles 3 meters apart and stand between them");
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

        });

        // ------ Number of players ------
        numberOfPlayersTextView = findViewById(R.id.numberOfPlayersTextView);
        playersRadioGroup = findViewById(R.id.playersRadioGroup);
        positioningImageView = findViewById(R.id.positioningImageView);

        playersRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch(checkedId) {
                case R.id.twoPlayersButton:
                    positioningImageView.setImageResource(R.drawable.two_players);
                    numberOfPlayers = 2;
                    break;
                case R.id.threePlayersButton:
                    positioningImageView.setImageResource(R.drawable.three_players);
                    numberOfPlayers = 3;
                    break;
                case R.id.fourPlayersButton:
                    positioningImageView.setImageResource(R.drawable.four_players);
                    numberOfPlayers = 4;
                    break;
                default:
                    positioningImageView.setImageResource(R.drawable.one_players);
                    numberOfPlayers = 1;
                    break;
            }
            setupTilesPosition(numberOfPlayers);
        });

        Button startGameButton = findViewById(R.id.startGameButton);
        startGameButton.setOnClickListener(v -> {

            if (numberOfPlayers > connectedTiles/2) {
                Toast.makeText(this, "Not enough tiles connected", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(SetupActivity.this, GameActivity.class);
            intent.putExtra("setup_data", new int[]{numberOfPlayers, difficulty});
            intent.putExtra("tile_ids", new int[]{player1_trueTile, player1_falseTile, player2_trueTile, player2_falseTile, player3_trueTile, player3_falseTile, player4_trueTile, player4_falseTile});
            startActivity(intent);
        });
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

    // ------------------------------- //
    // Text To Speech
    public void textToSpeech(String textToSay) {
        textToSpeechSystem = new TextToSpeech(this, status -> {
            //if (status == TextToSpeech.SUCCESS) {
            textToSpeechSystem.speak(textToSay, TextToSpeech.QUEUE_FLUSH, null,"ID");
            //}
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
    }

    // ------------------------------- //
    // Checking Internet Connection
    public boolean checkIfDeviceIsConnectedToInternet() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");

            if(!ipAddr.equals("")) {
                //connected
                Log.d("tag", "Connected");
                return true;
            }
        } catch (Exception e) {
            Toast.makeText(SetupActivity.this, "You are not connected to the internet!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
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
        connection.setAllTilesToInit();
    }

    @Override
    public void onNumbersOfTilesConnected(int i) {
        connectedTiles = i;
        TextView pairTextView = findViewById(R.id.pairTextView);
        pairTextView.setText("Tiles pairing (connected tiles: " + i + ")");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //connection.stopMotoConnection();
        connection.unregisterListener(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //connection.startMotoConnection(this);
        //connection.registerListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //connection.stopMotoConnection();
        connection.unregisterListener(this);
    }

}