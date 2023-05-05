package com.livelife.playwaremax;

import static com.livelife.motolibrary.AntData.LED_COLOR_GREEN;
import static com.livelife.motolibrary.AntData.LED_COLOR_OFF;
import static com.livelife.motolibrary.AntData.LED_COLOR_RED;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.MotoSound;
import com.livelife.motolibrary.OnAntEventListener;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
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
    int difficulty = 2;

    int player1_trueTile = 0, player2_trueTile = 0, player3_trueTile = 0, player4_trueTile = 0;
    int player1_falseTile = 0, player2_falseTile = 0, player3_falseTile = 0, player4_falseTile = 0;

    public static TextToSpeech textToSpeechSystem;
    // -------------------------

    int setupMode = 1;

    boolean challenge_accepted;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Log.d("tot", "Starting setup game...");

        setTitle("Setup Game");

        // Enable Back-button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        connection = MotoConnection.getInstance();
        sound = MotoSound.getInstance();

        //connection.startMotoConnection(this); TEMP comment
        
        //connection.saveRfFrequency(66); TEMP comment
        //connection.setDeviceId(2); TEMP comment
        //connection.registerListener(this);

        challenge_accepted = getIntent().getBooleanExtra("challenge_accepted", false);
        if (challenge_accepted) {
            difficulty = getIntent().getIntExtra("difficulty", 0);
        }

        // ------ Difficulty ------
        RadioButton easyDifficultyButton = findViewById(R.id.easyDifficultyButton);
        RadioButton normalDifficultyButton = findViewById(R.id.normalDifficultyButton);
        RadioButton hardDifficultyButton = findViewById(R.id.hardDifficultyButton);

        if (challenge_accepted) {

            if (difficulty == 1) {
                easyDifficultyButton.setChecked(true);
            } else if (difficulty == 3) {
                hardDifficultyButton.setChecked(true);
            } else {
                normalDifficultyButton.setChecked(true);
            }

            easyDifficultyButton.setClickable(false);
            normalDifficultyButton.setClickable(false);
            hardDifficultyButton.setClickable(false);

            //depending on difficulty gray out the other buttons
            if (difficulty == 1) {
                normalDifficultyButton.setBackground(getDrawable(R.drawable.radio_button_qrayed_out));
                hardDifficultyButton.setBackground(getDrawable(R.drawable.radio_button_qrayed_out));
            } else if (difficulty == 2) {
                easyDifficultyButton.setBackground(getDrawable(R.drawable.radio_button_qrayed_out));
                hardDifficultyButton.setBackground(getDrawable(R.drawable.radio_button_qrayed_out));
            } else {
                easyDifficultyButton.setBackground(getDrawable(R.drawable.radio_button_qrayed_out));
                normalDifficultyButton.setBackground(getDrawable(R.drawable.radio_button_qrayed_out));
            }

        } else {
            easyDifficultyButton.setOnClickListener(v -> {
                difficulty = 1;
            });

            normalDifficultyButton.setOnClickListener(v -> {
                difficulty = 2;
            });

            hardDifficultyButton.setOnClickListener(v -> {
                difficulty = 3;
            });
        }



        // ------ Text to Speech initialization ------
        textToSpeechSystem = new TextToSpeech(SetupActivity.this, status -> {
            if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeechSystem.setLanguage(Locale.ENGLISH);
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("error", "This Language is not supported");
                }
                textToSpeechSystem.setSpeechRate(1.5F);
                } else
                    Log.e("error", "Initialization Failed!");
        });

        // ------ Pairing Tiles ------
        Button pairingButton = findViewById(R.id.pairButton);
        pairingButton.setOnClickListener(v -> {
            switch(setupMode) {
                case 1:
                    //Starting pairing tiles -> tiles a spinning
                    connection.registerListener(this);
                    connection.pairTilesStart();
                    textToSpeechSystem.speak("Turn on and press " + numberOfPlayers*2 + "tiles you want to use", TextToSpeech.QUEUE_FLUSH, null,"ID");
                    pairingButton.setText("Next");
                    setupMode = 2;
                    break;
                case 2:
                    //Stopping pairing tiles -> tiles are OFF
                    connection.pairTilesStop();
                    textToSpeechSystem.speak("Place the " + numberOfPlayers*2 + " tiles 3 meters apart and stand between them", TextToSpeech.QUEUE_FLUSH, null,"ID");
                    pairingButton.setText("Next");
                    setupMode = 3;
                    break;
                case 3:
                    setupTilesPosition(numberOfPlayers);
                    textToSpeechSystem.speak("Setup complete", TextToSpeech.QUEUE_FLUSH, null,"ID");
                    connection.unregisterListener(this);
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

        if (challenge_accepted) {
            RadioButton onePlayersButton = findViewById(R.id.onePlayersButton);
            RadioButton twoPlayersButton = findViewById(R.id.twoPlayersButton);
            RadioButton threePlayersButton = findViewById(R.id.threePlayersButton);
            RadioButton fourPlayersButton = findViewById(R.id.fourPlayersButton);

            //Disbale buttons
            onePlayersButton.setClickable(false);
            twoPlayersButton.setClickable(false);
            threePlayersButton.setClickable(false);
            fourPlayersButton.setClickable(false);

            // grayout buttons
            //onePlayersButton.setBackground(getDrawable(R.drawable.radio_button_qrayed_out));
            twoPlayersButton.setBackground(getDrawable(R.drawable.radio_button_qrayed_out));
            threePlayersButton.setBackground(getDrawable(R.drawable.radio_button_qrayed_out));
            fourPlayersButton.setBackground(getDrawable(R.drawable.radio_button_qrayed_out));
        }

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

            String[] questionSets = getAllQuestionSets();

            // if only the default question set is available, start the game
            if (questionSets.length == 1) {
                Intent intent = new Intent(SetupActivity.this, GameActivity.class);
                Log.d("tot", "setup difficulty: " + difficulty);
                intent.putExtra("setup_data", new int[]{numberOfPlayers, difficulty});
                intent.putExtra("tile_ids", new int[]{player1_trueTile, player1_falseTile, player2_trueTile, player2_falseTile, player3_trueTile, player3_falseTile, player4_trueTile, player4_falseTile});
                intent.putExtra("question_set", "Default Question-set");
                startActivity(intent);
                return;
            }
            // else show a dialog to choose the question set

            AlertDialog.Builder chooseQuestionSet_builder = new AlertDialog.Builder(this);
            //gameOver_AlertDialog.setTitle("Player " + (maxScorePlayer+1) + " won this game with " + maxScore + " points");
            chooseQuestionSet_builder.setView(R.layout.dialog_choose_question_set);
            AlertDialog chooseQuestionSet_AlertDialog = chooseQuestionSet_builder.create();
            chooseQuestionSet_AlertDialog.setCancelable(false);
            chooseQuestionSet_AlertDialog.show();

            NumberPicker picker = chooseQuestionSet_AlertDialog.findViewById(R.id.choose_question_set_number_picker);

            picker.setDisplayedValues(questionSets);
            picker.setMinValue(0);
            picker.setMaxValue(questionSets.length - 1);

            Button enterButton = chooseQuestionSet_AlertDialog.findViewById(R.id.choose_question_set_enterButton);
            Button cancelButton = chooseQuestionSet_AlertDialog.findViewById(R.id.choose_question_set_cancelButton);

            enterButton.setOnClickListener(view -> {
                // FOR DEBUGGING
                /*if (numberOfPlayers > connectedTiles/2) {
                    Toast.makeText(this, "Not enough tiles connected", Toast.LENGTH_SHORT).show();
                    return;
                } else {*/
                Intent intent = new Intent(SetupActivity.this, GameActivity.class);
                Log.d("tot", "setup difficulty: " + difficulty);
                intent.putExtra("setup_data", new int[]{numberOfPlayers, difficulty});
                intent.putExtra("tile_ids", new int[]{player1_trueTile, player1_falseTile, player2_trueTile, player2_falseTile, player3_trueTile, player3_falseTile, player4_trueTile, player4_falseTile});
                intent.putExtra("question_set", questionSets[picker.getValue()]);
                startActivity(intent);
                chooseQuestionSet_AlertDialog.cancel();
                //}
            });

            cancelButton.setOnClickListener(view -> chooseQuestionSet_AlertDialog.cancel());
        });
    }

    // ------------------------------- //
    // For going back to previous activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            killTTS();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
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
        //questionSets[numQuestionSets] = String.valueOf(numQuestionSets - 1);
        return Arrays.copyOf(questionSets, numQuestionSets);
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
                Log.d("tag", "ERROR: Wrong number of players");
                break;
        }
    }

     private void killTTS() {
        textToSpeechSystem.stop();
        textToSpeechSystem.shutdown();
    }

    @Override
    public void onMessageReceived(byte[] bytes, long l) {
        /*int command = AntData.getCommand(bytes);
        int tileId = AntData.getId(bytes);
        int color = AntData.getColorFromPress(bytes);

        if(command == EVENT_PRESS) {
            Log.d("tag", "tileID: " + tileId);
        }*/
    }

    @Override
    public void onAntServiceConnected() {
        connection.setAllTilesToInit();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onNumbersOfTilesConnected(int i) {
        connectedTiles = i;
        TextView pairTextView = findViewById(R.id.pairTextView);
        pairTextView.setText("Tiles pairing (connected tiles: " + i + ")");
        Log.d("tag", "Number of connected tiles " + i);
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