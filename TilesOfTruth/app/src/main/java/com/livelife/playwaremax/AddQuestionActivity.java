package com.livelife.playwaremax;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddQuestionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        setTitle("Add Question");

        // Enable Back-button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        displayAllQuestionsFromCSV();

        // if addQuestion_btn is clicked
        findViewById(R.id.addQuestion_btn).setOnClickListener(v -> {
            showAlertDialog();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    void displayAllQuestionsFromCSV() {

        TextView questions_TextView = findViewById(R.id.questions_TextView);

        try {
            // Open the CSV file as an InputStream
            InputStream inputStream = getResources().openRawResource(R.raw.questions);

            // Use a BufferedReader to read the lines of the file
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            // Loop through each line of the file
            while ((line = reader.readLine()) != null) {
                // Display the line in the TextView
                questions_TextView.append(line + "\n");
            }

            // Close the BufferedReader
            reader.close();

        } catch (IOException e) {
            // Handle the exception
            e.printStackTrace();
        }

    }

    void showAlertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(AddQuestionActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.alertdialog_add_question, null);
        builder.setTitle("Please add a question (example: Tigers can fly)")
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton("Ok", (dialog, id) -> {

                    // New question
                    EditText newQuestion = dialogView.findViewById(R.id.newQuestion_EditText);

                    // New answer
                    RadioGroup rg = dialogView.findViewById(R.id.radioPersonGroup);
                    int selectedId = rg.getCheckedRadioButtonId();
                    RadioButton radioButton = dialogView.findViewById(selectedId);

                    Toast.makeText(getApplicationContext(), newQuestion.getText(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), radioButton.getText(), Toast.LENGTH_SHORT).show();

                    writeQuestionToSCV(newQuestion.getText().toString(), Boolean.parseBoolean((String) radioButton.getText()));
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    void writeQuestionToSCV(String newQuestion, boolean newAnswer) {

        Log.d("AddQuestionActivity", "writeQuestionToSCV: " + newQuestion);

        try {
            // Open the CSV file in the resources directory as an InputStream
            InputStream inputStream = getResources().openRawResource(R.raw.questions);

            // Create a new file in the app's private storage directory to write the contents of the CSV file to
            FileOutputStream outputStream = openFileOutput("questions.csv", Context.MODE_PRIVATE);

            // Copy the contents of the CSV file to the new file using a byte buffer
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Close the InputStream and the OutputStream
            inputStream.close();
            outputStream.close();

        } catch (IOException e) {
            // Handle the exception
            e.printStackTrace();
        }


        Log.d("tag", "here");

        try {
            // Open the CSV file as an InputStream
            InputStream inputStream = getResources().openRawResource(R.raw.questions);

            // Use a BufferedReader to read the lines of the file
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            List<String> lines = new ArrayList<>();

            // Loop through each line of the file and add it to the List
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

            // Modify the contents of the List as needed (e.g., change the first line)
            lines.set(0, newQuestion);

            // Close the BufferedReader
            reader.close();

            // Open the CSV file as a FileOutputStream
            FileOutputStream outputStream = openFileOutput("questions.csv", Context.MODE_PRIVATE);

            // Use a PrintWriter to write the modified lines back to the file
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream));
            for (String modifiedLine : lines) {
                writer.println(modifiedLine);
            }

            // Close the PrintWriter
            writer.close();

            displayAllQuestionsFromCSV();

        } catch (IOException e) {
            // Handle the exception
            e.printStackTrace();
        }
    }
}