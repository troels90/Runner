package com.example.troels.runner;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;

/**
 * Created by Troels on 05-08-2017.
 */

public class MainActivity extends AppCompatActivity {
    private SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getSharedPreferences("settings", 0);
        initFragment();

    }

    private void initFragment(){
        Fragment fragment;
        if(pref.getBoolean(Constants.IS_LOGGED_IN,false)){
            fragment = new MenuFragment();
        }else {
            fragment = new LoginFragment();
        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame,fragment);
        ft.commit();
    }

}