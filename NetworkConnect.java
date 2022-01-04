package com.example.troels.runner;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Troels on 13-09-2017.
 */

public class NetworkConnect extends AsyncTask<JSONObject, Void, Boolean> {
    @Override
    protected Boolean doInBackground(JSONObject... jsonObjects) {
        JSONObject json = jsonObjects[0];
        URL url = null;
        String response  ="";

        try {
            url = new URL("http://f15-preview.biz.nf/troelspet.dk/User.php");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            response = JSONHttpPost(url, json);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return true;
    }

    private String JSONHttpPost(URL url, JSONObject json) throws IOException {
        String jsonResponse = "";

        if( url == null){
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
}

