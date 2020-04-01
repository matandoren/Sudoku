package com.example.sudoku;

public class SudokuSolver {
    public static int boardSize = 9;
    private static boolean[][] rows; // [row][value]
    private static boolean[][] cols; // [col][value]
    private static boolean[][] squares; // [square][value]
    private static boolean isValidArrays;
    private static boolean isRunning;
    private static boolean terminateRunning;

    public static void stop() {
        if (isRunning)
            terminateRunning = true;
    }

    public static boolean isSolved(SudokuEntry[][] board) {
        // checks that there are no blank entries
        for (int i = 0; i < boardSize; i++)
            for (int j = 0; j < boardSize; j++)
                if (board[i][j].value == 0)
                    return false;

        // checks that there are no violations of Sudoku rules
        constructArrays(board);
        return isValidArrays;
    }

    public static boolean isSolvable(SudokuEntry[][] board) {
        isRunning = true;
        constructArrays(board);
        if (!isValidArrays) {// there were Sudoku rules violations
            isRunning = false;
            return false;
        }

        // try solving the puzzle using brute force with backtracking
        boolean result = bruteForcePuzzle(board, 0, 0, false);
        isRunning = false;
        terminateRunning = false;
        return result;
    }

    private static void constructArrays(SudokuEntry[][] board) {
        int squareSize = (int)Math.sqrt(boardSize);
        isValidArrays = false;
        rows = new boolean[boardSize][boardSize];
        cols = new boolean[boardSize][boardSize];
        squares = new boolean[boardSize][boardSize];

        for (int i = 0; i < boardSize; i++)
            for (int j = 0; j < boardSize; j++) {
                int squareIdx = j / squareSize + (i / squareSize) * squareSize;
                int value = board[i][j].value - 1;
                if (value >= 0) {
                    // checks if there is a violation of the Sudoku rules in the board
                    if (rows[i][value] || cols[j][value] || squares[squareIdx][value])
                        return; // isValidArrays remains false
                    else {
                        rows[i][value] = true;
                        cols[j][value]  = true;
                        squares[squareIdx][value] = true;
                    }
                }
            }

        isValidArrays = true;
    }

    private static boolean bruteForcePuzzle(SudokuEntry[][] board, int row, int col, boolean isShowSolution) { // if isShowSolution is 'true', the board values will reflect the values of the first solution that was found
        if (board[row][col].isHint) // If the current square is a hint, proceed to the next square.
        {
            if (col + 1 == boardSize) // If proceeding to the next column, results getting out of bounds, proceed to the next row and start from the first column.
            {
                if (row + 1 == boardSize) // If proceeding to the next row, results getting out of bounds, the Sudoku is completed successfully.
                    return true;
                return bruteForcePuzzle(board, row + 1, 0, isShowSolution); // Proceed to the first column in the next row.
            }
            return bruteForcePuzzle(board, row, col + 1, isShowSolution); // Proceed to the next column.
        }

        int squareSize = (int)Math.sqrt(boardSize);
        int squareIdx = col / squareSize + (row / squareSize) * squareSize;
        for (int value = 0; value < boardSize && !terminateRunning; value++) // Assign every possible value to the current entry.
        {
            if (!rows[row][value] && !cols[col][value] && !squares[squareIdx][value]) // If the current value fits in the current entry according to the Sudoku rules,
            {
                if (isShowSolution)
                    board[row][col].value = value + 1; // Assign that value to the solution matrix.
                rows[row][value] = true; // Signal that the value is taken in that particular row.
                cols[col][value] = true; // // Signal that the value is taken in that particular column.
                squares[squareIdx][value] = true; // Signal that the value is taken in that particular square.
                if (col + 1 == boardSize) // Proceed to the next entry.
                {
                    if (row + 1 == boardSize)
                        return true;
                    if (bruteForcePuzzle(board, row + 1, 0, isShowSolution))
                        return true;
                }
                else if (bruteForcePuzzle(board, row, col + 1, isShowSolution))
                    return true;
                // If the next entry hit a Dead End, the current value of the current entry is wrong.
                rows[row][value] = false; // Signal that the value is no longer taken in that particular row.
                cols[col][value] = false; // Signal that the value is no longer taken in that particular column.
                squares[squareIdx][value] = false; // Signal that the value is no longer taken in that particular square.
            }
        } // Try the next value.
        return false; // None of the values fits. This means at least one of the preceding entries has a wrong value.
    }
}
