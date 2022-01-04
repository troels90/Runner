package com.example.troels.runner;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Troels on 26-09-2017.
 */

public class LoginFragment extends Fragment {

    private AppCompatButton btn_login;
    private EditText et_email, et_password;
    private TextView tv_register;
    private ProgressBar progress;
    private static final String POST_URL = "http://f15-preview.biz.nf/troelspet.dk/User.php";
    SharedPreferences pref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {

        btn_login = (AppCompatButton) view.findViewById(R.id.btn_login);
        tv_register = (TextView) view.findViewById(R.id.tv_register);
        et_email = (EditText) view.findViewById(R.id.et_email);
        et_password = (EditText) view.findViewById(R.id.et_password);

        progress = (ProgressBar) view.findViewById(R.id.progress);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = et_email.getText().toString();
                String password = et_password.getText().toString();

                if (!email.isEmpty() && !password.isEmpty()) {

                    User user = new User();
                    user.setEmail(email);
                    user.setPassword(password);

                    JSONObject data = new JSONObject();
                    JSONObject jsonUser = new JSONObject();
                    try {
                        jsonUser.put("email", user.getEmail());
                        jsonUser.put("password", user.getPassword());
                        data.put("operation", Constants.LOGIN_OPERATION);
                        data.put("user", jsonUser);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    LoginProcess login = new LoginProcess();
                    login.execute(data);
                    progress.setVisibility(View.VISIBLE);


                } else {

                    Snackbar.make(getView(), "Fields are empty !", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRegister();
            }
        });
    }

    private class LoginProcess extends AsyncTask<JSONObject, Void, Boolean> {

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
            try {
                System.out.println(jsonResponse);
                JSONObject jsonObj = new JSONObject(jsonResponse.substring(9));
                String result = jsonObj.getString("result");
                String message = jsonObj.getString("message");
                if (result.equals(Constants.SUCCESS)) {

                    JSONObject jsonUser = jsonObj.getJSONObject("user");

                    Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
                    System.out.println(message);

                    System.out.println(jsonUser.get("username"));
                    System.out.println(jsonUser.get("email"));

                    SharedPreferences pref = this.getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(Constants.IS_LOGGED_IN, true);
                    editor.putString(Constants.NAME, jsonUser.getString("username"));
                    editor.putString(Constants.EMAIL, jsonUser.getString("email"));
                    editor.apply();
                    goToMenu();


                } else {
                    Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
                    System.out.println(message + "login failure");
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


            progress.setVisibility(View.INVISIBLE);

            urlConnection.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
        return jsonResponse;

    }

    private void goToRegister() {
        Fragment register = new RegisterFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, register);
        ft.commit();
    }

    private void goToMenu() {
        Fragment menu = new MenuFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, menu);
        ft.commit();
    }
}