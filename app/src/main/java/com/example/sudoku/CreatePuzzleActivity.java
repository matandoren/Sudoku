package com.example.sudoku;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class CreatePuzzleActivity extends AppCompatActivity implements AddPhoneDialog.AddPhoneDialogListener {
    private static CreatePuzzleActivity activeCreatePuzzleActivity;

    public final int MIN_HINTS = 17;
    public final int BOARD_SIZE = 9; // the board has BOARD_SIZE * BOARD_SIZE entries
    private SudokuView sudokuView;
    private SudokuEntry[][] board;
    private boolean isBoardEditable;
    private String phoneNumber;
    private SudokuHint[] hints;
    private int numberOfHints;

    @SuppressLint({"SourceLockedOrientationActivity", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_puzzle);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        activeCreatePuzzleActivity = this;
        board = new SudokuEntry[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                board[i][j] = new SudokuEntry();

        sudokuView = findViewById(R.id.activity_create_sudoku_view);
        sudokuView.setBoard(board);

        Button submitButton = findViewById(R.id.activity_create_submit_puzzle_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberOfHints < MIN_HINTS)
                    Toast.makeText(CreatePuzzleActivity.this, getString(R.string.min_clues_allowed_toast) + " " + MIN_HINTS, Toast.LENGTH_LONG).show();
                else {
                    isBoardEditable = false;
                    new EvaluateBoardTask().execute(board);
                }
            }
        });

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
                            final View view = getLayoutInflater().inflate(R.layout.sudoku_entry_value_chooser_layout ,null);
                            new AlertDialog.Builder(CreatePuzzleActivity.this)
                                    .setView(view)
                                    .setNegativeButton(R.string.cancel, null)
                                    .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            RadioGroup rg = view.findViewById(R.id.radio_group);
                                            int id = rg.getCheckedRadioButtonId();
                                            if (id == R.id.radio_button_clear) {
                                                if (entry.value != 0)
                                                    numberOfHints--;
                                                entry.value = 0;
                                                entry.isHint = false;
                                            }
                                            else {
                                                if (entry.value == 0)
                                                    numberOfHints++;
                                                RadioButton rb = view.findViewById(id);
                                                entry.value = Integer.parseInt(rb.getText().toString());
                                                entry.isHint = true;
                                            }
                                            sudokuView.invalidate();
                                        }
                                    }).show();
                            return true;
                        }
                }
                return sudokuView.onTouchEvent(event);
            }
        });

        isBoardEditable = true;
    }

    private void openAddPhoneDialog() {
        new AddPhoneDialog().show(getSupportFragmentManager(),"Add Phone");
    }

    @Override
    public void addPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        openPlayActivity();
    }

    private void openPlayActivity(){
        Intent intent = new Intent(this,PlayActivity.class);
        intent.putExtra("PHONENUMBER",this.phoneNumber);
        intent.putExtra("HINTS", hints);
        intent.putExtra("NAME", getIntent().getStringExtra("NAME"));
        startActivity(intent);
    }


    private static class EvaluateBoardTask extends AsyncTask<SudokuEntry[][], Void, SudokuHint[]> {
        @Override
        protected SudokuHint[] doInBackground(SudokuEntry[][]... sudokuEntries) {
            SudokuEntry[][] board = sudokuEntries[0];

            if (!SudokuSolver.isSolvable(board))
                return null;

            SudokuHint[] hints = new SudokuHint[CreatePuzzleActivity.activeCreatePuzzleActivity.numberOfHints];
            for (int i = 0, k = 0; i < board.length; i++)
                for (int j = 0; j < board.length; j++)
                    if (board[i][j].isHint) {
                        hints[k] = new SudokuHint();
                        hints[k].row = i;
                        hints[k].col = j;
                        hints[k].value = board[i][j].value;
                        k++;
                    }

            return hints;
        }

        @Override
        protected void onPostExecute(SudokuHint[] hints) {
            if (hints == null) {
                Toast.makeText(CreatePuzzleActivity.activeCreatePuzzleActivity, R.string.unsolvable_puzzle_toast, Toast.LENGTH_LONG).show();
                CreatePuzzleActivity.activeCreatePuzzleActivity.isBoardEditable = true;
            } else {
                CreatePuzzleActivity.activeCreatePuzzleActivity.hints = hints;
                CreatePuzzleActivity.activeCreatePuzzleActivity.openAddPhoneDialog();
            }
        }
    }
}
