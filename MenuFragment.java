package com.example.troels.runner;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Troels on 03-10-2017.
 */

public class MenuFragment extends Fragment{

    private TextView NormalRun, RaceFriends, LiveRun,Leaderboards,
                    Friendlist, Settings, Logout, userGreeting;
    private SharedPreferences pref;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu,container,false);

        pref = this.getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = pref.getString(Constants.NAME, "");

        userGreeting = (TextView) view.findViewById(R.id.tv_UserGreeting);
        userGreeting.setText("Hello  " + name);

        initViews(view);
        return view;
    }

    private void initViews(View view){
        final Animation animTranslate = AnimationUtils.loadAnimation(view.getContext(), R.anim.anim_translate);
        final Animation animAlpha = AnimationUtils.loadAnimation(view.getContext(), R.anim.anim_alpha);
        NormalRun = (TextView)view.findViewById(R.id.tv_NormalRun);
        NormalRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNormalRun();
            }
        });
        RaceFriends = (TextView)view.findViewById(R.id.tv_RaceFriends);
        RaceFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRaceAgainst();

            }
        });
        Friendlist = (TextView)view.findViewById(R.id.tv_FriendList);
        Friendlist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                v.startAnimation(animAlpha);
                goToFriendList();
            }
        });
        Settings = (TextView)view.findViewById(R.id.tv_Settings);

        Logout = (TextView)view.findViewById(R.id.tv_Logout);
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

    }

    private void logout() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Constants.IS_LOGGED_IN,false);
        editor.putString(Constants.EMAIL,"");
        editor.putString(Constants.NAME,"");
        editor.putString(Constants.UNIQUE_ID,"");
        editor.apply();
        goToLogin();
    }

    private void goToLogin(){

        Fragment login = new LoginFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame,login);
        ft.addToBackStack(null);
        ft.commit();
    }
    private void goToFriendList(){
        Fragment friendlist = new FriendsFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, friendlist);
        ft.addToBackStack(null);
        ft.commit();
    }
    private void goToRaceAgainst(){
        Fragment duel = new DuelFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, duel);
        ft.addToBackStack(null);
        ft.commit();
    }
    private void goToNormalRun(){
        Intent singleIntent = new Intent(getActivity(), RunActivity.class);
        startActivity(singleIntent);
    }


}