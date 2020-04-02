package com.example.sudoku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


public class PlayActivity extends AppCompatActivity {

    public final int BOARD_SIZE = 9; // the board has BOARD_SIZE * BOARD_SIZE entries
    private final int REQUEST_CALL = 1;
    private String friendsNumber = "0505947773";
    private Chronometer chronometer;
    private ImageButton phoneButton;
    private Button submitSolutionButton;
    private SudokuView sudokuView;
    private int numOfHints;
    private SudokuEntry[][] board;
    private boolean isBoardEditable;


    @SuppressLint({"SourceLockedOrientationActivity", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        submitSolutionButton = findViewById(R.id.activity_play_submit_solution_button);
        phoneButton = findViewById(R.id.activity_play_phone_a_friend_button);
        chronometer = findViewById(R.id.activity_play_chronometer);
        friendsNumber = getIntent().getStringExtra("PHONENUMBER");
        if (friendsNumber.equals("")) {
            phoneButton.setEnabled(false);
            phoneButton.setImageResource(R.drawable.ic_phone_garyedout);
        }

        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneAFriend();
            }
        });

        submitSolutionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBoardEditable = false;
                if (SudokuSolver.isSolved(board)) {
                    long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
                    chronometer.stop();
                    Intent intent = new Intent(PlayActivity.this, HighScoresActivity.class);
                    intent.putExtra("NUM_OF_HINTS", numOfHints);
                    intent.putExtra("NAME", getIntent().getStringExtra("NAME"));
                    intent.putExtra("TIME", elapsedMillis);
                    startActivity(intent);
                    finish();
                } else {
                    isBoardEditable = true;
                    Toast.makeText(PlayActivity.this, R.string.puzzle_not_solved_toast, Toast.LENGTH_LONG).show();
                }
            }
        });

        SudokuHint[] hints = (SudokuHint[]) getIntent().getSerializableExtra("HINTS");
        numOfHints = hints.length;
        board = new SudokuEntry[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                board[i][j] = new SudokuEntry();
        for (int i = 0; i < hints.length; i++) {
            board[hints[i].row][hints[i].col].value = hints[i].value;
            board[hints[i].row][hints[i].col].isHint = true;
        }

        sudokuView = findViewById(R.id.activity_play_sudoku_view);
        sudokuView.setBoard(board);

        sudokuView.setOnTouchListener(new View.OnTouchListener() {
            private final int CLICK_ACTION_THRESHOLD = 50;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        if ((endX - startX <= CLICK_ACTION_THRESHOLD && endX - startX >= -CLICK_ACTION_THRESHOLD && endY - startY <= CLICK_ACTION_THRESHOLD && endY - startY >= -CLICK_ACTION_THRESHOLD) && isBoardEditable) {
                            final SudokuEntry entry = sudokuView.getEntryAt((endX + startX) / 2.f, (endY + startY) / 2.f);
                            if (!entry.isHint) {
                                final View view = getLayoutInflater().inflate(R.layout.sudoku_entry_value_chooser_layout ,null);
                                new AlertDialog.Builder(PlayActivity.this)
                                        .setView(view)
                                        .setNegativeButton(R.string.cancel, null)
                                        .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                RadioGroup rg = view.findViewById(R.id.radio_group);
                                                int id = rg.getCheckedRadioButtonId();
                                                if (id == R.id.radio_button_clear)
                                                    entry.value = 0;
                                                else {
                                                    RadioButton rb = view.findViewById(id);
                                                    entry.value = Integer.parseInt(rb.getText().toString());
                                                }
                                                sudokuView.invalidate();
                                            }
                                        }).show();
                            }
                            return true;
                        }
                }
                return sudokuView.onTouchEvent(event);
            }
        });

        isBoardEditable = true;
    }

    private void phoneAFriend(){
        if(ContextCompat.checkSelfPermission(PlayActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(PlayActivity.this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        }
        else{
            String dial = "tel:" + friendsNumber;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CALL){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                phoneAFriend();
            else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
