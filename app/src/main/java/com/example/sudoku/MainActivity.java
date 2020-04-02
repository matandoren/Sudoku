package com.example.sudoku;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;


public class MainActivity extends AppCompatActivity implements AddPhoneDialog.AddPhoneDialogListener {
    private static MainActivity activeMainActivity;

    private static final long TIMEOUT = 10000; // 10 seconds

    public static final int MIN_HINTS = 17;
    public static final int BOARD_SIZE = 9;
    private Button helpButton;
    private Button playButton;
    private Button createButton;
    private String phoneNumber;
    private String name;
    private EditText numOfHintsEditText;
    private SudokuHint[] hints;
    private ProgressBar progressBar;
    private AlertDialog progressPopup;
    private TextView progressTextView;
    private CountDownTimer timer;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        activeMainActivity = this;
        helpButton = findViewById(R.id.help_button);
        playButton = findViewById(R.id.play_button);
        createButton = findViewById(R.id.create_board_button);
        numOfHintsEditText = findViewById(R.id.number_of_hints_editText);

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
                String temp;
                int numOfHints;

                EditText et = findViewById(R.id.name_editText);
                name = et.getText().toString();
                if (name.equals("")) {
                    Toast.makeText(MainActivity.this, R.string.name_is_mandatory_toast, Toast.LENGTH_LONG).show();
                    return;
                }

                if (!(temp = numOfHintsEditText.getText().toString()).equals("") && (numOfHints = Integer.parseInt(temp)) >= MIN_HINTS && numOfHints < BOARD_SIZE * BOARD_SIZE) {
                    playButton.setEnabled(false);
                    numOfHintsEditText.setEnabled(false);

                    final View view = getLayoutInflater().inflate(R.layout.progress_bar_layout ,null);
                    progressPopup = new AlertDialog.Builder(MainActivity.this).setView(view).create();
                    progressBar = view.findViewById(R.id.progressBar);
                    progressTextView = view.findViewById(R.id.progress_textView);
                    progressPopup.show();

                    new GenerateRandomBoardTask().execute(numOfHints);
                } else
                    Toast.makeText(MainActivity.this, getString(R.string.num_of_hints_range_part1) + " " + MIN_HINTS + " " + getString(R.string.num_of_hints_range_part2) + " " + (BOARD_SIZE * BOARD_SIZE - 1), Toast.LENGTH_LONG).show();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = findViewById(R.id.name_editText);
                name = et.getText().toString();
                if (name.equals("")) {
                    Toast.makeText(MainActivity.this, R.string.name_is_mandatory_toast, Toast.LENGTH_LONG).show();
                    return;
                }
                MainActivity.this.openSudokuActivity();
            }
        });
    }

    private void openSudokuActivity() {
        Intent intent = new Intent(this,CreatePuzzleActivity.class);
        intent.putExtra("NAME", name);
        startActivity(intent);
    }

    private void openPlayActivity(){
        Intent intent = new Intent(this,PlayActivity.class);
        intent.putExtra("PHONENUMBER",this.phoneNumber);
        intent.putExtra("HINTS", hints);
        intent.putExtra("NAME", name);

        startActivity(intent);
    }

    private void openAddPhoneDialog() {
        new AddPhoneDialog().show(getSupportFragmentManager(),"Add Phone");
    }

    @Override
    public void addPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        openPlayActivity();
    }

    private static class GenerateRandomBoardTask extends AsyncTask<Integer, Integer, SudokuHint[]> {

        @Override
        protected SudokuHint[] doInBackground(Integer... integers) {
            int numOfHints = integers[0];
            int row, col;
            Random random = new Random();
            int progress = 0;
            int iterations = 0;

            SudokuHint[] hints = new SudokuHint[numOfHints];
            SudokuEntry[][] board = new SudokuEntry[BOARD_SIZE][BOARD_SIZE];
            for (int i = 0; i < BOARD_SIZE; i++)
                for (int j = 0; j < BOARD_SIZE; j++)
                    board[i][j] = new SudokuEntry();

            for (int i = 0; i < numOfHints; i++) {
                SudokuSolver.IsSolvableResult result;
                do {
                    do { // find a random blank entry
                        row = random.nextInt(BOARD_SIZE);
                        col = random.nextInt(BOARD_SIZE);
                        iterations++;
                        publishProgress(progress, iterations);
                    } while (board[row][col].isHint);
                    board[row][col].isHint = true;

                    do { // find a random value for that entry so that the puzzle is solvable
                        board[row][col].value = random.nextInt(BOARD_SIZE) + 1;
                        iterations++;

                        activeMainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activeMainActivity.timer = new CountDownTimer(TIMEOUT, TIMEOUT) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                    }

                                    @Override
                                    public void onFinish() {
                                        SudokuSolver.stop();
                                    }
                                }.start();
                            }
                        });

                        publishProgress(progress, iterations);
                        result = SudokuSolver.isSolvable(board);

                        if (result != SudokuSolver.IsSolvableResult.CANCELLED)
                            activeMainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activeMainActivity.timer.cancel();
                                }
                            });

                    } while (result == SudokuSolver.IsSolvableResult.UNSOLVABLE);
                    if (result == SudokuSolver.IsSolvableResult.CANCELLED) {
                        board[row][col].isHint = false;
                        board[row][col].value = 0;
                    }
                } while (result == SudokuSolver.IsSolvableResult.CANCELLED);

                hints[i] = new SudokuHint();
                hints[i].row = row;
                hints[i].col = col;
                hints[i].value = board[row][col].value;
                progress += 100 / numOfHints;
                publishProgress(progress, iterations);
            }

            return hints;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            activeMainActivity.progressBar.setProgress(values[0]);
            activeMainActivity.progressTextView.setText("" + values[1]);
        }

        @Override
        protected void onPostExecute(SudokuHint[] sudokuHints) {
            activeMainActivity.hints = sudokuHints;
            activeMainActivity.progressPopup.dismiss();
            activeMainActivity.playButton.setEnabled(true);
            activeMainActivity.numOfHintsEditText.setEnabled(true);
            activeMainActivity.openAddPhoneDialog();
        }
    }
}
