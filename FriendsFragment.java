package com.example.troels.runner;


import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Troels on 03-10-2017.
 */

public class FriendsFragment extends Fragment {
    private static final String POST_URL = "http://f15-preview.biz.nf/troelspet.dk/User.php";

    List<String> FriendsList = new ArrayList<String>();
    List<String> FriendRequestsList = new ArrayList<String>();
    ListView lv_Friends;
    ListView lv_friendRequests;
    Button btn_friendrequests;
    Button btn_AddFriend;
    SharedPreferences pref;
    String jsonResponse;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        lv_Friends = (ListView) view.findViewById(R.id.lv_Friends);

        pref = this.getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String name = pref.getString(Constants.NAME, "");
        btn_AddFriend = (Button) view.findViewById(R.id.btn_addFriend);

        JSONObject data = new JSONObject();
        JSONObject jsonUser = new JSONObject();
        try {
            jsonUser.put("username", name);
            data.put("operation", Constants.GETFRIENDS_OPERATION);
            data.put("user", jsonUser);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("OnCreateView", "OnCreateView");

        }
        CreateListAsyncTask task = new CreateListAsyncTask();
        task.execute(data);

        btn_AddFriend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

                alert.setTitle("Write username to send friend request:");

                final EditText input = new EditText(v.getContext());
                alert.setView(input);

                alert.setPositiveButton("Send Request", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        JSONObject data = new JSONObject();
                        JSONObject jsonUser = new JSONObject();
                        final String friendName = input.getText().toString();

                        try {
                            jsonUser.put("username", name);
                            jsonUser.put("friendname", friendName);
                            data.put("operation", Constants.FRIENDREQUEST_OPERATION);
                            data.put("user", jsonUser);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("OnCreateView", "OnCreateView");

                        }
                        SendData task2 = new SendData();
                        task2.execute(data);

                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();
            }
        });

        btn_friendrequests = (Button) view.findViewById(R.id.btn_friendRequests);


        return view;
    }

    private class SendData extends AsyncTask<JSONObject, Void, Boolean> {

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
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            return true;
        }
    }
    private class CreateListAsyncTask extends AsyncTask<JSONObject, Void, Boolean> {
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(FriendsList != null){

            final ArrayAdapter<String> listAdapter =
                    new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, FriendsList);

            lv_Friends.setAdapter(listAdapter);
            lv_friendRequests = new ListView(getContext());

            final ArrayAdapter<String> listAdapter2 =
                    new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, FriendRequestsList);
            lv_friendRequests.setAdapter(listAdapter2);

            btn_friendrequests.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(lv_friendRequests.getParent()!=null) {
                        ((ViewGroup) lv_friendRequests.getParent()).removeView(lv_friendRequests);
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setCancelable(false);
                    builder.setNeutralButton("Done", null);
                    builder.setView(lv_friendRequests);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
            lv_friendRequests.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(view.getContext());
                    final String FriendName =  lv_friendRequests.getItemAtPosition(position).toString();
                    final int ph_position = position;
                    builder2.setMessage("Add " + FriendName + " as a friend?");
                    builder2.setCancelable(true);
                    builder2.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            JSONObject data3 = new JSONObject();
                            JSONObject jsonUser3 = new JSONObject();
                            try {
                                jsonUser3.put("username", pref.getString(Constants.NAME, ""));
                                jsonUser3.put("friendname", FriendName);
                                jsonUser3.put("answer", 1);
                                data3.put("operation", Constants.FRIENDREQUESTANSWER_OPERATION);
                                data3.put("user", jsonUser3);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            SendData task2 = new SendData();
                            task2.execute(data3);

                            FriendsList.add(FriendName);
                            listAdapter.notifyDataSetChanged();
                            FriendRequestsList.remove(ph_position);
                            listAdapter2.notifyDataSetChanged();
                        }
                    });
                    builder2.setNegativeButton("No", null);
                    AlertDialog dialog2 = builder2.create();
                    dialog2.show();
                }

            });
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
            ArrayList<List<String>> result;
            result = extractFeatureFromJson(response);
            if(result != null){
                FriendsList = result.get(0);
                FriendRequestsList = result.get(1);
            }

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
    private ArrayList<List<String>> extractFeatureFromJson(String friendsJSON) {

        if(TextUtils.isEmpty(friendsJSON)){
            return null;
        }

        try {

            System.out.println(jsonResponse);
            JSONObject jsonObj = new JSONObject(friendsJSON.substring(9));

            List<String> temp = new ArrayList<String>();
            List<String> tempZero = new ArrayList<>();
            ArrayList<List<String>> resultLists = new ArrayList<List<String>>();

            String result = jsonObj.getString("result");
            String message = jsonObj.getString("message");
            System.out.print(result);

            if (result.equals(Constants.SUCCESS)) {
                if (message.equals(Constants.MsgFriendSucces)){

                }
                else {

                    JSONArray friendsArray = new JSONArray();
                    friendsArray = jsonObj.getJSONArray("user");

                    for (int i = 0; friendsArray.length() > i; i++) {
                        JSONObject currentFriend = friendsArray.getJSONObject(i);

                        String name = currentFriend.getString("username");
                        int statusid = currentFriend.getInt("statusid");

                        if (statusid == 1) {
                            String friendToList = new String(name);
                            System.out.print(" NAMEES : " + name);
                            temp.add(friendToList);
                        }
                        if (statusid == 0) {
                            String friendToList2 = new String(name);
                            System.out.print(" NAMEES : " + name);
                            tempZero.add(friendToList2);
                        }

                    }
                }
            }

            resultLists.add(temp);
            resultLists.add(tempZero);
            return resultLists;

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSONHTTPPOST", "JSONHTTPPOST");
        }
        return null;
    }




}