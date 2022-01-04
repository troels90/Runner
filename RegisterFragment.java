package com.example.troels.runner;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
 * Created by Troels on 25-10-2017.
 */

public class RegisterFragment extends Fragment{

    private AppCompatButton btn_register;
    private EditText et_email, et_password,et_name;
    private TextView tv_login;
    private ProgressBar progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register,container,false);

        initViews(view);
        return view;
    }

    private void initViews(View view){

        btn_register = (AppCompatButton)view.findViewById(R.id.btn_register);
        tv_login = (TextView)view.findViewById(R.id.tv_login);
        et_name = (EditText)view.findViewById(R.id.et_name);
        et_email = (EditText)view.findViewById(R.id.et_email);
        et_password = (EditText)view.findViewById(R.id.et_password);

        progress = (ProgressBar)view.findViewById(R.id.progress);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = et_name.getText().toString();
                String email = et_email.getText().toString();
                String password = et_password.getText().toString();

                if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {

                    progress.setVisibility(View.VISIBLE);

                    User user = new User();
                    user.setName(name);
                    user.setEmail(email);
                    user.setPassword(password);

                    JSONObject jsonUser = new JSONObject();
                    JSONObject data = new JSONObject();
                    try {
                        jsonUser.put("username", user.getFullName());
                        jsonUser.put("email", user.getEmail());
                        jsonUser.put("password", user.getPassword());
                        data.put("operation", Constants.REGISTER_OPERATION);
                        data.put("user", jsonUser);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    NetworkConnect connect = new NetworkConnect();
                    connect.execute(data);
                    progress.setVisibility(View.INVISIBLE);


                }
                else{
                    Snackbar.make(getView(), "Fields are empty !", Snackbar.LENGTH_LONG).show();}
                goToLogin();

            }
        });
    }

    private void goToLogin(){

        Fragment login = new LoginFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame,login);
        ft.commit();
    }


}