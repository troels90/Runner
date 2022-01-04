package com.example.troels.runner;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Troels on 09-10-2017.
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String POST_URL = "http://f15-preview.biz.nf/troelspet.dk/User.php";
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;
    int time;
    int dueltime;
    int distance;
    double PaceCalc;
    int distRan;
    TextView tv_runConclusion;
    TextView tv_whoWon;
    TextView tv_pace;
    List<Double> Arraylist_lat;
    List<Double> Arraylist_lon;
    List<LatLng> points;
    String friendname;
    Button btn_publish;
    int startCode;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        pref = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String name = pref.getString(Constants.NAME, "");

        tv_runConclusion = (TextView) findViewById(R.id.tv_runConclusion);
        tv_whoWon = (TextView) findViewById(R.id.tv_DidYouWin);
        tv_pace = (TextView) findViewById(R.id.tv_concPace);
        btn_publish = (Button) findViewById(R.id.btn_Publish);

        Arraylist_lat = (ArrayList<Double>) getIntent().getSerializableExtra("latArrayList");
        Arraylist_lon = (ArrayList<Double>) getIntent().getSerializableExtra("lonArrayList");

        startCode = getIntent().getIntExtra("startcode", 0);
        time = getIntent().getIntExtra("finaltime", 0);
        distance = getIntent().getIntExtra("distance", 0);

        if(startCode == 2){
            dueltime = getIntent().getIntExtra("dueltime", 0);
            friendname = getIntent().getStringExtra("friendname");

            distRan = distance / 1000;
            PaceCalc =  1000 / ((double) distance / (double) time);
            Double PaceRoundinSec = PaceCalc % 60;
            int PaceRoundinSecInt = (int) Math.floor(PaceRoundinSec);
            double PaceInMin = Math.floor(PaceCalc/60);
            int PaceInMinInt = (int) PaceInMin;

            double timeInSec = time % 60;
            double timeInMin = Math.floor(time / 60);
            int timeInSecText = (int) timeInSec;
            int timeInMinText = (int) timeInMin;

            tv_runConclusion.setText("You ran " + distRan + " Kilometers, in the time: " + timeInMinText + ":" + timeInSecText);
            tv_pace.setText("Average min / km: " + PaceInMinInt + ":" + PaceRoundinSecInt );

            if(dueltime > time){
                int timeDiff = dueltime - time;
                tv_whoWon.setText("Congratulations, you beat " + friendname + " ! You were " + timeDiff + " seconds faster!"  );
            }
            if(dueltime > time){
                tv_whoWon.setText("Well done! You and " + friendname + " ran the " + distance + " kilometers, at exactly the same pace!"  );
            }
            if(dueltime > time){
                int timeDiff = time - dueltime;
                tv_whoWon.setText("Unfortunately, " + friendname + " were " + timeDiff + " seconds faster. Better luck next time!"  );
            }
        }else if(startCode == 3){

            double timeInSec = time % 60;
            double timeInMin = Math.floor(time / 60);
            int timeInSecText = (int) timeInSec;
            int timeInMinText = (int) timeInMin;
            tv_runConclusion.setText("You ran " + distance + "meters, in" + timeInMinText + " minutes and " + timeInSecText + " seconds.");
        }

        btn_publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject data = new JSONObject();
                JSONObject jsonUser = new JSONObject();

                try {
                    jsonUser.put("username", name);
                    jsonUser.put("distance", distRan);
                    jsonUser.put("time", time);
                    data.put("operation", Constants.INSERTTRACK_OPERATION);
                    data.put("user", jsonUser);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SendData task = new SendData();
                task.execute(data);
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        List<LatLng> latlngs = new ArrayList<>();
        for(int i=0; i < Arraylist_lat.size(); i++){
            latlngs.add(new LatLng(Arraylist_lat.get(i), Arraylist_lon.get(i)));
        }
        PolylineOptions rectOptions = new PolylineOptions().addAll(latlngs);

        mGoogleMap.addPolyline(rectOptions);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Arraylist_lat.get(0), Arraylist_lon.get(0)), 15));

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
}