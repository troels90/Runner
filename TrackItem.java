package com.example.troels.runner;

/**
 * Created by Troels on 01-10-2017.
 */

public class TrackItem {
    public String friendname;
    public String distance;
    public String time;
    public String date;

    public TrackItem(String friendname, String distance, String time, String date) {
        this.friendname = friendname;
        this.distance = distance;
        this.time = time;
        this.date = date;
    }

    public String getName(){
        return friendname;
    }
    public String getDistance(){
        return distance;
    }
    public String getTime(){
        return time;
    }
    public String getDate(){
        return date;
    }
}