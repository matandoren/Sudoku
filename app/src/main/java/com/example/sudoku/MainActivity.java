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
                MainActivity.this.openSudokuActivity();
            }
        });
    }

    private void openSudokuActivity() {
        Intent intent = new Intent(this,CreatePuzzleActivity.class);
        startActivity(intent);
    }

    public void openPlayActivity(){
        Intent intent = new Intent(this,PlayActivity.class);
        intent.putExtra("PHONENUMBER",this.phoneNumber);


        /******************* MOCK UP **********************************/
        SudokuHint[] hints = new SudokuHint[3];
        hints[0] = new SudokuHint();
        hints[1] = new SudokuHint();
        hints[2] = new SudokuHint();

        hints[0].row = 0;
        hints[0].col = 3;
        hints[0].value = 9;

        hints[1].row = 6;
        hints[1].col = 1;
        hints[1].value = 2;

        hints[2].row = 1;
        hints[2].value = 9;
        hints[2].col = 7;

        intent.putExtra("HINTS", hints);
        /*************************************************************/


        startActivity(intent);
    }

    private void openAddPhoneDialog() {
        AddPhoneDialog addDriver = new AddPhoneDialog();
        addDriver.show(getSupportFragmentManager(),"Add Phone");
    }

    @Override
    public void addPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        openPlayActivity();
    }
}
