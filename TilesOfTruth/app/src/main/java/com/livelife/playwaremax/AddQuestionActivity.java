package com.livelife.playwaremax;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

public class AddQuestionActivity extends AppCompatActivity {

    ListView addQuestion_ListView;
    ArrayList<String> addQuestion_ArrayList = new ArrayList<>();
    ArrayAdapter<String> addQuestion_ArrayAdapter;

    boolean insideDefaultQuestionSet = false;
    boolean menuIsVisible = true;

    String questionSet = "Nothing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        setTitle("Add Question-set");

        // Enable Back-button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Default Question set
        addQuestion_ListView = findViewById(R.id.addQuestion_ListView);
        addQuestion_ArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, addQuestion_ArrayList);
        addQuestion_ListView.setAdapter(addQuestion_ArrayAdapter);
        addQuestion_ListView.setOnItemClickListener((parent, view, position, id) -> {

            if (Objects.equals(addQuestion_ArrayList.get(position), "Default Question-set")) {
                questionSet = "Default Question-set";
                insideDefaultQuestionSet = true;
                menuIsVisible = false;

                findViewById(R.id.addQuestion_btn).setVisibility(View.GONE);
                displayAllQuestionsFromDefaultQuestionSet();
            }
            // if the name of the question set starts with "Custom: "
            else if (addQuestion_ArrayList.get(position).startsWith("Custom: ")) {
                // remove "Custom: " from the name of the question set
                questionSet = addQuestion_ArrayList.get(position).substring(8);
                insideDefaultQuestionSet = false;
                menuIsVisible = false;

                findViewById(R.id.addQuestion_btn).setVisibility(View.VISIBLE);
                displayAllQuestionsFromQuestionSet();
            }

            else {
                if (!insideDefaultQuestionSet) {
                    editQuestion(position);
                }
            }
        });


        displayAllQuestionSets();

        // if addQuestion_btn is clicked
        findViewById(R.id.addQuestion_btn).setOnClickListener(v -> {
            if (menuIsVisible) {
                showAlertDialog_newQuestionSet();
            } else {
                showAlertDialog_newQuestion(null, false, -1);
            }
        });

        //deleteCSVFile("name");
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    void showAlertDialog_newQuestionSet() {
        AlertDialog.Builder gameOver_AlertDialog = new AlertDialog.Builder(this);
        gameOver_AlertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        gameOver_AlertDialog.setTitle("Create a new question set");
        //gameOver_AlertDialog.setMessage("Please fill in your name for the scoreboard");

        final EditText input = new EditText(AddQuestionActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        gameOver_AlertDialog.setView(input);

        gameOver_AlertDialog.setPositiveButton("OK", (dialogInterface, i) -> {
            // write a csv-file with the name of the new question-set
            String fileName = "question_" + input.getText().toString() + ".csv";
            try {
                FileOutputStream fileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            dialogInterface.cancel();
            displayAllQuestionSets();
        });
        gameOver_AlertDialog.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        gameOver_AlertDialog.setCancelable(false);
        gameOver_AlertDialog.show();
    }

    void showAlertDialog_newQuestion(String question, boolean answer, int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(AddQuestionActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.alertdialog_add_question_set, null);

        EditText newQuestion = dialogView.findViewById(R.id.newQuestion_EditText);
        RadioGroup rg = dialogView.findViewById(R.id.radioPersonGroup);

        String alertDialog_Title;
        if (question != null) {
            newQuestion.setText(question);
            if (answer) {
                rg.check(R.id.true_btn);
            } else {
                rg.check(R.id.false_btn);
            }
            alertDialog_Title = "Edit question";
        } else {
            alertDialog_Title = "Add question";
            position = -1;
        }

        int finalPosition = position;
        builder.setView(dialogView)
                .setTitle(alertDialog_Title)
                .setCancelable(true)
                .setPositiveButton("Ok", (dialog, id) -> {

                    // New question
                    if( TextUtils.isEmpty(newQuestion.getText())) {
                        Toast.makeText(getApplicationContext(), "A question is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // New answer
                    int selectedId = rg.getCheckedRadioButtonId();
                    if(selectedId == -1) {
                        Toast.makeText(getApplicationContext(), "An answer is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    RadioButton radioButton = dialogView.findViewById(selectedId);

                    writeQuestionToCSV(newQuestion.getText().toString(), Boolean.parseBoolean((String) radioButton.getText()), finalPosition);
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    void displayAllQuestionsFromQuestionSet() {
        //Display the questions and answers from the question-set
        addQuestion_ArrayList.clear();

        try {
            InputStream inputStream = openFileInput("question_" + questionSet + ".csv");

            Log.d("tot", "inputStream: " + inputStream);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {

                Log.d("tot", "line: " + line);

                String question = line.split(",")[0];
                String answer = line.split(",")[1];

                addQuestion_ArrayList.add(question + " - " + answer);
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        addQuestion_ArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, addQuestion_ArrayList);
        addQuestion_ListView.setAdapter(addQuestion_ArrayAdapter);
    }

    void displayAllQuestionSets() {
        addQuestion_ArrayList.clear();

        addQuestion_ArrayList.add("Default Question-set");

        File[] files = getFilesDir().listFiles();
        assert files != null;
        for (File file : files) {
            if (file.getName().startsWith("question_")) {
                addQuestion_ArrayList.add("Custom: " + file.getName().replace("question_", "").replace(".csv", ""));
            }
        }

        addQuestion_ArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, addQuestion_ArrayList);
        addQuestion_ListView.setAdapter(addQuestion_ArrayAdapter);
    }

    void displayAllQuestionsFromDefaultQuestionSet() {
        addQuestion_ArrayList.clear();

        try {
            InputStream inputStream = getResources().openRawResource(R.raw.default_questions);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {

                String question = line.split(",")[0];
                String answer = line.split(",")[1];

                addQuestion_ArrayList.add(question + " - " + answer);
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        addQuestion_ArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, addQuestion_ArrayList);
        addQuestion_ListView.setAdapter(addQuestion_ArrayAdapter);
    }

    void writeQuestionToCSV(String newQuestion, boolean newAnswer, int position) {

        File file = new File(getApplicationContext().getFilesDir(), "question_" + questionSet + ".csv");

        try {
            FileWriter csvWriter = new FileWriter(file, true);

            csvWriter.write(newQuestion + "," + newAnswer + "\n");

            // close the FileWriter object
            csvWriter.close();

        } catch (IOException e) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        displayAllQuestionsFromQuestionSet();
    }

    void editQuestion(int position) {

        // get question from csv-file at position "position"
        String question = addQuestion_ArrayList.get(position).split(" - ")[0];
        boolean answer = Boolean.parseBoolean(addQuestion_ArrayList.get(position).split(" - ")[1]);

        showAlertDialog_newQuestion(question, answer, position);
    }

    void deleteCSVFile(String name) {
        File file = new File(getApplicationContext().getFilesDir(), "question_" + name + ".csv");
        if (file.delete()) {
            Log.d("Delete File", "File deleted successfully");
        } else {
            Log.d("Delete File", "Failed to delete file");
        }
    }
}