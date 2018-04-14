package com.cs3301.a150004675.cs3301uberapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class HistoryActivityAdapter extends RecyclerView.Adapter<HistoryActivityAdapter.ViewHolder> {
    private ArrayList<String> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string
        public TextView mTextView;
        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

    public HistoryActivityAdapter(ArrayList<String> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public HistoryActivityAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_text_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}