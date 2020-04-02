package com.example.sudoku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class HighScoresActivity extends AppCompatActivity {
    Button sendMailButton;
    HighScoreRecord latestHighScore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        sendMailButton = findViewById(R.id.send_mail_button);
        Intent intent = getIntent();
        latestHighScore = new HighScoreRecord();
        latestHighScore.rank = 1;
        latestHighScore.hints = intent.getIntExtra("NUM_OF_HINTS", -1);
        latestHighScore.name = intent.getStringExtra("NAME");
        latestHighScore.time = intent.getLongExtra("TIME", -1);

        sendMailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMail();
            }
        });

        List<HighScoreRecord> highScoreList = null;
        File file = new File(getFilesDir(), getString(R.string.filename));
        if (!file.exists()) {
            highScoreList = new LinkedList<>();
            highScoreList.add(latestHighScore);
        } else {
            ObjectInputStream is = null;
            try {
                is = new ObjectInputStream(new FileInputStream(file));
                highScoreList = (LinkedList<HighScoreRecord>) is.readObject();
            } catch (IOException | ClassNotFoundException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                try {
                    if (is != null)
                        is.close();
                } catch (IOException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            if (highScoreList != null) {
                highScoreList.add(latestHighScore);
                Collections.sort(highScoreList);

                int i = 1;
                for (HighScoreRecord record : highScoreList)
                    record.rank = i++;
            }
        }
        if (file.exists())
            file.delete();
        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
            os.writeObject(highScoreList);
            os.close();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }


        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        HighScoresRecyclerAdapter adapter = new HighScoresRecyclerAdapter(this, highScoreList);
        recyclerView.setAdapter(adapter);
    }

    private void sendMail(){
        long time = latestHighScore.time / 1000; // converts the time from milliseconds to seconds
        int seconds = (int)(time % 60);
        time /= 60; // converts the time from seconds to minutes
        int minutes = (int)(time % 60);
        time /= 60; // converts the time from minutes to hours
        String totalTime = String.format("%02d:%02d:%02d", time, minutes, seconds);

        String subject = "Hey Lookg at my score";
        String message = "Ive used that name: " + latestHighScore.name
                + "\nIve got this Rank right now: " + latestHighScore.rank
                + "\nAmount of hints: " + latestHighScore.hints
                + "\nAnd the time it took me in Total is: " + totalTime;
        String[] emails = {getString(R.string.email)};

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, emails);
        intent.putExtra(Intent.EXTRA_SUBJECT,subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, "Choose an email client"));
    }
}
