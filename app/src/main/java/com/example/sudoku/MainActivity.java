package com.example.sudoku;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity implements AddPhoneDialog.AddPhoneDialogListener {
    private Button helpButton;
    private Button playButton;
    private Button createButton;
    private String phoneNumber;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        helpButton = findViewById(R.id.help_button);
        playButton = findViewById(R.id.play_button);
        createButton = findViewById(R.id.create_board_button);

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent wikipediaWebPage = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.sudoku_wikipedia)));
                startActivity(wikipediaWebPage);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddPhoneDialog();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.openSudokuActiv();
            }
        });
    }

    private void openSudokuActiv() {
        Intent intent = new Intent(this,CreatePuzzleActivity.class);
        startActivity(intent);
    }

    public void openPlayActiviy(){
        Intent intent = new Intent(this,PlayActivity.class);
        intent.putExtra("PHONENUMBER",this.phoneNumber);
        startActivity(intent);
    }

    private void openAddPhoneDialog() {
        AddPhoneDialog addDriver = new AddPhoneDialog();
        addDriver.show(getSupportFragmentManager(),"Add Phone");
    }

    @Override
    public void addPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        openPlayActiviy();
    }
}
