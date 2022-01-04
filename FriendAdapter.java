package com.example.troels.runner;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Troels on 01-10-2017.
 */

public class FriendAdapter extends ArrayAdapter<TrackItem>{

    public FriendAdapter(Activity context, ArrayList<TrackItem> friend){
        super(context, 0, friend);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItemView = convertView;
        if(convertView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.simple_list_item, parent,false);
        }

        TrackItem currentFriend = getItem(position);

        TextView friendNameView = (TextView) listItemView.findViewById(R.id.name_text_view);
        friendNameView.setText(currentFriend.getName());

        TextView friendDistanceView = (TextView) listItemView.findViewById(R.id.distance_text_view);
        friendDistanceView.setText("Distance: " + currentFriend.getDistance() + " Kilometers");

        TextView friendTimeView = (TextView) listItemView.findViewById(R.id.time_text_view);
        int time = Integer.parseInt(currentFriend.getTime());
        long minutesSoFar = time / 60;
        double minutesRounded = Math.floor(minutesSoFar);
        int minutesRounded2 = (int) minutesRounded;
        long timeCalc = time % 60; // Number of seconds left in the minute.
        friendTimeView.setText("Time: " + minutesRounded2 + " : " + timeCalc);

        TextView friendDateView = (TextView) listItemView.findViewById(R.id.date_text_view);
        friendDateView.setText("Created: " + currentFriend.getDate());

        return listItemView;
    }
}
