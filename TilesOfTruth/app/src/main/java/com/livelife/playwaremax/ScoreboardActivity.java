package com.livelife.playwaremax;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ScoreboardActivity extends AppCompatActivity {

    ListView gameSessions_ListView;
    ArrayAdapter<String> gameSessions_ArrayAdapter;
    ArrayList<String> games_ArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        setTitle("Scoreboard");

        // Enable Back-button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        gameSessions_ListView = findViewById(R.id.gameSessions_ListView);

        gameSessions_ArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, games_ArrayList);

        gameSessions_ListView.setAdapter(gameSessions_ArrayAdapter);

        gameSessions_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
                String content = (String) adapterView.getItemAtPosition(position);
                List<String> content_ArrayList = new ArrayList<>(Arrays.asList(content.split(" ")));
            }
        });
        // for testing purposes only
        for(int i = 0; i < 20; i++) {
            games_ArrayList.add("Game session ID: " + i + " Score: " + i + " Group ID:" + i + " Number of tiles:" + i);
        }

        games_ArrayList.addAll(games_ArrayList);
        gameSessions_ArrayAdapter.notifyDataSetChanged();
    }

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
}