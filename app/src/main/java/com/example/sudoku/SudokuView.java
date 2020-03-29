package com.example.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

public class SudokuView extends View {
    public final int BOARD_SIZE = 9; // the board has BOARD_SIZE * BOARD_SIZE entries
    public final float MIN_SCALE_FACTOR = 1.f;
    public final float MAX_SCALE_FACTOR = 2.5f;
    public final float FONT_SIZE = 35.f;
    private ScaleGestureDetector sd;
    private float scaleFactor = 1.f;
    private GestureDetector gd;
    private float offsetX = 0.f;
    private float offsetY = 0.f;
    private float pivotX = 0.0f; // the center of the scaling in the x axis
    private float pivotY = 0.0f; // the center of the scaling in the Y axis
    private float top = 0.0f; // the top of the scaled image
    private float left = 0.0f; // the left of the scaled image
    private int viewWidth;
    private int viewHeight;
    private boolean isMeasuresKnown = false;
    private Paint hintBackground;
    private Paint textColor;
    private Paint gridColor;
    private SudokuEntry[][] board;



    public SudokuView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        board = new SudokuEntry[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                board[i][j] = new SudokuEntry();

        sd = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float previousScaleFactor = scaleFactor;
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(MIN_SCALE_FACTOR, Math.min(scaleFactor, MAX_SCALE_FACTOR));
                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();
                top = focusY - (focusY - top) * (scaleFactor / previousScaleFactor);
                left = focusX - (focusX - left) * (scaleFactor / previousScaleFactor);

                pivotX = (focusX - left) / scaleFactor;
                offsetX = focusX - pivotX;
                pivotY = (focusY - top) / scaleFactor;
                offsetY = focusY - pivotY;

                rectifyGoingOutOfBounds();

                invalidate();
                return true;
            }
        });

        gd = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                    float distanceX, float distanceY) {

                rectifyGoingOutOfBounds(distanceX, distanceY);
                invalidate();
                return true;
            }
        });
    }

    public void setHints(SudokuHint[] hints) {
        for (int i = 0; i < hints.length; i++) {
            board[hints[i].row][hints[i].col].value = hints[i].value;
            board[hints[i].row][hints[i].col].isHint = true;
        }

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        sd.onTouchEvent(event);
        gd.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isMeasuresKnown) {
            viewWidth = getWidth();
            viewHeight = getHeight();
            gridColor = new Paint();
            gridColor.setStyle(Paint.Style.STROKE);
            hintBackground = new Paint();
            hintBackground.setColor(Color.LTGRAY);
            textColor = new Paint();
            textColor.setAntiAlias(true);
            textColor.setTextSize(FONT_SIZE);
            textColor.setTextAlign(Paint.Align.CENTER);
            //textColor.setColor(Color.WHITE);
            isMeasuresKnown = true;
        }
        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scaleFactor, scaleFactor, pivotX, pivotY);
        /* draw the grid of the board */
        float entryWidth = viewWidth / (float)BOARD_SIZE;
        float entryHeight = viewHeight / (float)BOARD_SIZE;
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j].isHint)
                    canvas.drawRect(j * entryWidth, i * entryHeight, (j + 1) * entryWidth, (i + 1) * entryHeight, hintBackground);

                canvas.drawRect(j * entryWidth, i * entryHeight, (j + 1) * entryWidth, (i + 1) * entryHeight, gridColor);

                if (board[i][j].value != 0) {
                    int xPos = (int)(j * entryWidth + entryWidth / 2);
                    int yPos = (int) (i * entryHeight + (entryHeight / 2) - ((textColor.descent() + textColor.ascent()) / 2)) ;
                    canvas.drawText("" + board[i][j].value, xPos, yPos, textColor);
                }
            }

        canvas.restore();
    }


    private void rectifyGoingOutOfBounds(float distanceX, float distanceY) {
        float newLeft = left - distanceX;
        float newRight = newLeft + viewWidth * scaleFactor;
        if (newLeft <= 0 && newRight >= viewWidth) {
            offsetX -= distanceX;
            left -= distanceX;
        }
        else if (newLeft > 0) {
            offsetX -= left;
            left = 0;
        }
        else {
            offsetX -= viewWidth * scaleFactor - (viewWidth - left);
            left = viewWidth * (1 - scaleFactor);
        }

        float newTop = top - distanceY;
        float newBottom = newTop + viewHeight * scaleFactor;
        if (newTop <= 0 && newBottom >= viewHeight) {
            offsetY -= distanceY;
            top -= distanceY;
        }
        else if (newTop > 0) {
            offsetY -= top;
            top = 0;
        }
        else {
            offsetY -= viewHeight * scaleFactor - (viewHeight - top);
            top = viewHeight * (1 - scaleFactor);
        }
    }

    private void rectifyGoingOutOfBounds() {
        rectifyGoingOutOfBounds(0.f, 0.f);
    }


    public SudokuEntry getEntryAt(float x, float y) {
        x -= left;
        y -= top;

        return board[(int)(y / (viewHeight * scaleFactor))][(int)(x / (viewWidth * scaleFactor))];
    }
}

