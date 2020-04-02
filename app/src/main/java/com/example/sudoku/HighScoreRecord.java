package com.example.sudoku;

import java.io.Serializable;

public class HighScoreRecord implements Serializable, Comparable<HighScoreRecord> {
    public int rank;
    public int hints;
    public String name;
    public long time;

    @Override
    public int compareTo(HighScoreRecord o) {
        if (hints == o.hints)
            return (int)(time - o.time);
        else
            return hints - o.hints;
    }
}
