package com.livelife.playwaremax;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class AddQuestionActivity extends AppCompatActivity {

    ListView addQuestion_ListView;
    ArrayList<String> addQuestion_ArrayList = new ArrayList<>();
    ArrayAdapter<String> addQuestion_ArrayAdapter;

    boolean insideDefaultQuestionSet = false;
    boolean menuIsVisible = true;

    boolean deleteQuestionSet = false;

    String questionSet = "Nothing";

    MenuItem editQuestionSet_MenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        setTitle("Question sets");

        invalidateOptionsMenu();

        // Enable Back-button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Default Question set
        addQuestion_ListView = findViewById(R.id.addQuestion_ListView);
        addQuestion_ArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, addQuestion_ArrayList);
        addQuestion_ListView.setAdapter(addQuestion_ArrayAdapter);
        addQuestion_ListView.setOnItemClickListener((parent, view, position, id) -> {

            if (deleteQuestionSet) {
                if (!Objects.equals(addQuestion_ArrayList.get(position), "Default Question-set")) {

                    questionSet = addQuestion_ArrayList.get(position).substring(8);

                    Log.d("tot", "questionSet1: " + questionSet);

                    // takeing the text after "Custom: " and before ".csv"
                    int startIndex = questionSet.indexOf(":") + 2;
                    questionSet = questionSet.substring(startIndex);

                    Log.d("tot", "questionSet2: " + questionSet);

                    AlertDialog.Builder addQuestionSet_builder = new AlertDialog.Builder(this);
                    addQuestionSet_builder.setView(R.layout.dialog_delete_question_set);
                    AlertDialog deleteQuestionSet_AlertDialog = addQuestionSet_builder.create();
                    deleteQuestionSet_AlertDialog.setCancelable(false);
                    deleteQuestionSet_AlertDialog.show();

                    Button yes_Btn = deleteQuestionSet_AlertDialog.findViewById(R.id.delete_question_set_yes_btn);
                    Button no_Btn = deleteQuestionSet_AlertDialog.findViewById(R.id.delete_question_set_no_btn);

                    yes_Btn.setOnClickListener(v -> {

                        // delete the question set
                        deleteQuestionSetFile(questionSet);

                        displayAllQuestionSets(deleteQuestionSet);
                        deleteQuestionSet_AlertDialog.cancel();
                    });

                    no_Btn.setOnClickListener(v -> {
                        deleteQuestionSet_AlertDialog.cancel();
                    });




                } else {
                    Toast.makeText(getApplicationContext(), "You cannot delete the default question set", Toast.LENGTH_SHORT).show();
                }
            } else {
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
                } else {
                    if (!insideDefaultQuestionSet) {
                        editQuestion(position);
                    } else {
                        Toast.makeText(getApplicationContext(), "You cannot edit the default question set", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        displayAllQuestionSets(false);

        // if addQuestion_btn is clicked
        findViewById(R.id.addQuestion_btn).setOnClickListener(v -> {
            if (menuIsVisible) {
                menuIsVisible = true;
                showDialog_newQuestionSet();
            } else {
                showDialog_newQuestion(null, false, -1);
            }
        });

        //deleteQuestionSetFile("hey");
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
        questionSets[numQuestionSets] = String.valueOf(numQuestionSets - 1);
        return Arrays.copyOf(questionSets, numQuestionSets);
    }

    // ------------------------------- //
    // For going back to previous activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!menuIsVisible) {
                menuIsVisible = true;
                displayAllQuestionSets(false);
                editQuestionSet_MenuItem.setVisible(true);
                findViewById(R.id.addQuestion_btn).setVisibility(View.VISIBLE);
            } else {
                finish();
            }
            return true;
        }

        if (id == R.id.editQuestionSet_MenuItem) {
            if (deleteQuestionSet) {
                deleteQuestionSet = false;
                displayAllQuestionSets(deleteQuestionSet);
            } else {
                deleteQuestionSet = true;
                displayAllQuestionSets(deleteQuestionSet);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!menuIsVisible) {
            displayAllQuestionSets(false);
            editQuestionSet_MenuItem.setVisible(true);
        } else {
            finish();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {


        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_question_set, menu);
        editQuestionSet_MenuItem = menu.findItem(R.id.editQuestionSet_MenuItem);

        editQuestionSet_MenuItem.setVisible(true);

        return true;
    }

    void showDialog_newQuestionSet() {
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
                displayAllQuestionSets(false);
            }
        });

        cancelButton.setOnClickListener(view -> addQuestionSet_AlertDialog.cancel());
    }

    void showDialog_newQuestion(String question, boolean answer, int position) {
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
        boolean editQuestion = false;
        if (question != null) {
            addQuestionEditText.setText(question);
            if (answer) {
                rg.check(R.id.true_btn);
            } else {
                rg.check(R.id.false_btn);
            }
            alertDialog_Title = "Edit question";
            editQuestion = true;
        } else {
            alertDialog_Title = "Add question";
            position = -1;
        }

        int finalPosition = position;
        title.setText(alertDialog_Title);
        boolean finalEditQuestion = editQuestion;
        enterButton.setOnClickListener(view -> {
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

            writeQuestionToCSV(addQuestionEditText.getText().toString(), Boolean.parseBoolean((String) radioButton.getText()), finalPosition, finalEditQuestion);
            addQuestion_AlertDialog.cancel();
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addQuestion_AlertDialog.cancel();
            }
        });

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

    void displayAllQuestionSets(boolean delete) {
        addQuestion_ArrayList.clear();

        addQuestion_ArrayList.add("Default Question-set");

        File[] files = getFilesDir().listFiles();
        assert files != null;
        for (File file : files) {
            if (file.getName().startsWith("question_")) {
                if (delete) {
                    addQuestion_ArrayList.add("Delete \t\t - \t\t Custom: " + file.getName().replace("question_", "").replace(".csv", ""));
                } else {
                    addQuestion_ArrayList.add("Custom: " + file.getName().replace("question_", "").replace(".csv", ""));
                }
            }
        }

        addQuestion_ArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, addQuestion_ArrayList);
        addQuestion_ListView.setAdapter(addQuestion_ArrayAdapter);
        /*
        if (delete) {
            addQuestion_ListView.setBackgroundColor(Color.RED);
        } else {
            addQuestion_ListView.setBackgroundColor(Color.WHITE);
        }
         */

    }

    void displayAllQuestionsFromDefaultQuestionSet() {

        //Disable the delete button (editQuestionSet_MenuItem)
        editQuestionSet_MenuItem.setVisible(false);


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

    void writeQuestionToCSV(String newQuestion, boolean newAnswer, int position, boolean editQuestion) {

        File file = new File(getApplicationContext().getFilesDir(), "question_" + questionSet + ".csv");

        // if editQuestion = true, edit the question at position "position" in the csv-file.
        // otherwise write a new question to the csv-file
        if (editQuestion) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                String input = "";

                int i = 0;
                while ((line = reader.readLine()) != null) {
                    if (i == position) {
                        input += newQuestion + "," + newAnswer + "\n";
                    } else {
                        input += line + "\n";
                    }
                    i++;
                }
                reader.close();

                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(input.getBytes());
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                FileOutputStream outputStream = new FileOutputStream(file, true);
                outputStream.write((newQuestion + "," + newAnswer + "\n").getBytes());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        displayAllQuestionsFromQuestionSet();
    }

    void editQuestion(int position) {

        // get question from csv-file at position "position"
        String question = addQuestion_ArrayList.get(position).split(" - ")[0];
        boolean answer = Boolean.parseBoolean(addQuestion_ArrayList.get(position).split(" - ")[1]);

        showDialog_newQuestion(question, answer, position);
    }

    void deleteQuestionSetFile(String name) {
        File file = new File(getApplicationContext().getFilesDir(), "question_" + name + ".csv");
        if (file.delete()) {
            Log.d("Delete File", "File deleted successfully");
        } else {
            Log.d("Delete File", "Failed to delete file");
        }
    }
}