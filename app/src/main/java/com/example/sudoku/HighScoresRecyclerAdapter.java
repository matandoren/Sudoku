package com.example.sudoku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HighScoresRecyclerAdapter extends RecyclerView.Adapter<HighScoresRecyclerAdapter.ViewHolder> {
    private List<HighScoreRecord> mData;
    private LayoutInflater mInflater;


    // data is passed into the constructor
    HighScoresRecyclerAdapter(Context context, List<HighScoreRecord> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_layout, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HighScoreRecord record = mData.get(position);
        holder.rankTV.setText("" + record.rank);
        holder.hintsTV.setText("" + record.hints);
        holder.nameTV.setText(record.name);
        long time = record.time / 1000; // converts the time from milliseconds to seconds
        int seconds = (int)(time % 60);
        time /= 60; // converts the time from seconds to minutes
        int minutes = (int)(time % 60);
        time /= 60; // converts the time from minutes to hours
        holder.timeTV.setText(String.format("%02d:%02d:%02d", time, minutes, seconds));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView rankTV;
        TextView hintsTV;
        TextView nameTV;
        TextView timeTV;

        ViewHolder(View itemView) {
            super(itemView);
            rankTV = itemView.findViewById(R.id.rank_TV);
            hintsTV = itemView.findViewById(R.id.hints_TV);
            nameTV = itemView.findViewById(R.id.name_TV);
            timeTV = itemView.findViewById(R.id.time_TV);
        }
    }

}
