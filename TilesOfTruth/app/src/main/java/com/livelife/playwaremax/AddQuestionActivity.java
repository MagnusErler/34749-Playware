package com.livelife.playwaremax;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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

        setTitle("Question sets");

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
                menuIsVisible = true;
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
            if (!menuIsVisible) {
                menuIsVisible = true;
                displayAllQuestionSets();
            } else {
                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!menuIsVisible) {
            displayAllQuestionSets();
        } else {
            finish();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    void showAlertDialog_newQuestionSet() {
        AlertDialog.Builder addQuestionSet_builder = new AlertDialog.Builder(this);
        addQuestionSet_builder.setView(R.layout.dialog_add_question_set);
        AlertDialog addQuestionSet_AlertDialog = addQuestionSet_builder.create();
        addQuestionSet_AlertDialog.setCancelable(false);
        addQuestionSet_AlertDialog.show();

        EditText addQuestionSetEditText = addQuestionSet_AlertDialog.findViewById(R.id.addQuestionSetEditText);
        Button enterButton = addQuestionSet_AlertDialog.findViewById(R.id.enterButton);
        Button cancelButton = addQuestionSet_AlertDialog.findViewById(R.id.cancelButton);

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // write a csv-file with the name of the new question-set
                String fileName = "question_" + addQuestionSetEditText.getText().toString() + ".csv";
                try {
                    FileOutputStream fileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                addQuestionSet_AlertDialog.cancel();
                displayAllQuestionSets();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addQuestionSet_AlertDialog.cancel();
            }
        });
    }

    void showAlertDialog_newQuestion(String question, boolean answer, int position) {
        AlertDialog.Builder addQuestion_builder = new AlertDialog.Builder(this);
        addQuestion_builder.setView(R.layout.dialog_add_question);
        AlertDialog addQuestion_AlertDialog = addQuestion_builder.create();
        addQuestion_AlertDialog.setCancelable(false);
        addQuestion_AlertDialog.show();

        TextView title = addQuestion_AlertDialog.findViewById(R.id.addQuestionTextView);
        EditText addQuestionEditText = addQuestion_AlertDialog.findViewById(R.id.addQuestionSetEditText);
        RadioGroup rg = addQuestion_AlertDialog.findViewById(R.id.questionRadioGroup);
        Button enterButton = addQuestion_AlertDialog.findViewById(R.id.enterButton);
        Button cancelButton = addQuestion_AlertDialog.findViewById(R.id.cancelButton);

        String alertDialog_Title;
        if (question != null) {
            addQuestionEditText.setText(question);
            if (answer) {
                rg.check(R.id.true_btn);
            } else {
                rg.check(R.id.false_btn);
            }
            alertDialog_Title = "Edit question";
        } else {
            alertDialog_Title = "Add question and the correct answer";
            position = -1;
        }

        int finalPosition = position;
        title.setText(alertDialog_Title);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // New question
                if( TextUtils.isEmpty(addQuestionEditText.getText())) {
                    Toast.makeText(getApplicationContext(), "A question is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // New answer
                int selectedId = rg.getCheckedRadioButtonId();
                if(selectedId == -1) {
                    Toast.makeText(getApplicationContext(), "An answer is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                RadioButton radioButton = addQuestion_AlertDialog.findViewById(selectedId);

                writeQuestionToCSV(addQuestionEditText.getText().toString(), Boolean.parseBoolean((String) radioButton.getText()), finalPosition);
                addQuestion_AlertDialog.cancel();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addQuestion_AlertDialog.cancel();
            }
        });

        //AlertDialog alert = builder.create();
        //alert.show();
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