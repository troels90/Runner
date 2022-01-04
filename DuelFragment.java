package com.example.troels.runner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Troels on 04-10-2017.
 */

public class DuelFragment extends android.app.Fragment {
    private static final String POST_URL = "http://f15-preview.biz.nf/troelspet.dk/User.php";
    ArrayList<TrackItem> Tracks;
    ListView lv_challengeFriend;
    SharedPreferences pref;
    String jsonResponse;
    TrackItem trackBundle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_duel, container, false);
        lv_challengeFriend = (ListView) view.findViewById(R.id.lv_challengeList);

        pref = this.getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String name = pref.getString(Constants.NAME, "");

        JSONObject data = new JSONObject();
        JSONObject jsonUser = new JSONObject();

        try {
            jsonUser.put("username", name);
            data.put("operation", Constants.GET_TRACKS);
            data.put("user", jsonUser);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("OnCreateView", "OnCreateView");

        }
        CreateListAsyncTask task = new CreateListAsyncTask();
        task.execute(data);

        return view;
    }


    private class CreateListAsyncTask extends AsyncTask<JSONObject, Void, Boolean> {
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(Tracks != null){
                FriendAdapter listAdapter4 = new FriendAdapter(getActivity(), Tracks);
                final ArrayAdapter<TrackItem> listAdapter3 =
                        new ArrayAdapter<TrackItem>(getActivity(), android.R.layout.simple_list_item_1, Tracks);

                lv_challengeFriend.setAdapter(listAdapter4);
                lv_challengeFriend.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        trackBundle = (TrackItem) lv_challengeFriend.getItemAtPosition(position);
                        goToDuelRace();
                    }
                });

            }else{
                Toast.makeText(getContext(), "Your friends have made no recent runs", Toast.LENGTH_SHORT).show();
            }



            super.onPostExecute(aBoolean);
        }

        @Override
        protected Boolean doInBackground(JSONObject... jsonObjects) {
            JSONObject json = jsonObjects[0];
            URL url = null;
            String response = "";

            try {
                url = new URL(POST_URL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                response = JSONHttpPost(url, json);
                System.out.print(response);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            ArrayList<TrackItem> result;
            result = extractFeatureFromJson(response);
            Tracks = result;

            return true;
        }
    }


    private String JSONHttpPost(URL url, JSONObject json) {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            OutputStream os = urlConnection.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (urlConnection.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
                jsonResponse = output;
            }
            System.out.println(jsonResponse);

            urlConnection.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
        return jsonResponse;

    }
    private ArrayList<TrackItem> extractFeatureFromJson(String tracksJSON) {

        if(TextUtils.isEmpty(tracksJSON)){
            return null;
        }

        try {

            System.out.println(jsonResponse);
            JSONObject jsonObj = new JSONObject(tracksJSON.substring(9));

            ArrayList<TrackItem> temp = new ArrayList<TrackItem>();

            String result = jsonObj.getString("result");
            String message = jsonObj.getString("message");
            System.out.print(result);

            if (result.equals(Constants.SUCCESS)) {
                    JSONArray trackArray = new JSONArray();
                    trackArray = jsonObj.getJSONArray("user");

                    for (int i = 0; trackArray.length() > i; i++) {
                        JSONObject currentTrack = trackArray.getJSONObject(i);

                        String name = currentTrack.getString("username");
                        String distance = currentTrack.getString("distance");
                        String time = currentTrack.getString("time");
                        String date = currentTrack.getString("date");

                            TrackItem trackToList = new TrackItem(name, distance, time, date);
                            temp.add(trackToList);
                        }


                }
            return temp;
            } catch (JSONException e1) {
            e1.printStackTrace();
        }

        return null;
    }

    private void goToDuelRace(){
        Intent singleIntent = new Intent(getActivity(), DuelActivity.class);
        singleIntent.putExtra("TrackName", trackBundle.friendname);
        singleIntent.putExtra("TrackDistance", trackBundle.distance);
        singleIntent.putExtra("TrackTime", trackBundle.time);
        singleIntent.putExtra("TrackDate", trackBundle.date);

        startActivity(singleIntent);
    }


}
