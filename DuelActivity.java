package com.example.troels.runner;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import java.util.Locale;

/**
 * Created by Troels on 28-09-2017.
 */

public class DuelActivity extends FragmentActivity implements  LocationListener {
    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private GoogleMap mMap;
    int n;
    MarkerOptions mo;
    LocationManager locationManager;
    TextView tv_Distance;
    TextView tv_Coordinates;
    TextView tv_OldCoordinates;
    TextView tv_Time;
    TextView tv_DuelFriend;
    TextView tv_DuelTime;
    LatLng oldCoordinates;
    LatLng myCoordinates;
    TextToSpeech toSpeech;
    List<Double> latArray;
    List<Double> lonArray;
    List<Double> latArray2;
    List<Double> lonArray2;
    String friendname;
    int finalIntTime;
    int distToRunKM;
    double avgTime500m;
    double avgTime500mHolder;
    double avgPacePrM;
    int startCode;
    double tempdistance;
    double distance;
    long duelTime;
    public double finalDistance;
    public long finalTime;
    int distToRun;
    int timeToBeat;
    Button btn_Start;
    Button btn_Stop;
    Button btn_End;
    int speechResult;
    final int MSG_START_TIMER = 0;
    final int MSG_STOP_TIMER = 1;
    final int MSG_UPDATE_TIMER = 2;

    Stopwatch timer = new Stopwatch();
    final int REFRESH_RATE = 100;

    Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_TIMER:
                    timer.start(); //start timer
                    mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
                    break;
                case MSG_UPDATE_TIMER:
                    tv_Time.setText(new SimpleDateFormat("mm:ss").format(new Date(timer.getElapsedTime())));
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,REFRESH_RATE); //text view is updated every second,
                    break;                                  //though the timer is still running
                case MSG_STOP_TIMER:
                    mHandler.removeMessages(MSG_UPDATE_TIMER); // no more updates.
                    timer.stop();//stop timer
                    tv_Time.setText(new SimpleDateFormat("mm:ss").format(new Date(timer.getElapsedTime())));;
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duel);

        friendname = getIntent().getStringExtra("TrackName");
        String distanceHolder = getIntent().getStringExtra("TrackDistance");
        String timeHolder = getIntent().getStringExtra("TrackTime");
        distance = Double.parseDouble(distanceHolder)*1000;
        distToRun = (int) distance;
        distToRunKM = distToRun/1000;
        timeToBeat = Integer.parseInt(timeHolder);
        long minutesSoFar = timeToBeat / 60;
        double minutesRounded = Math.floor(minutesSoFar);
        int minutesRounded2 = (int) minutesRounded;
        long timeSecs = timeToBeat % 60; // Number of seconds left in the minute.
        avgTime500m = (timeToBeat / (distance / 1000)) / 2;
        avgTime500mHolder = avgTime500m;
        avgPacePrM = (distance / timeToBeat);

        btn_Start = (Button) findViewById(R.id.btn_duel_start);
        btn_Stop = (Button) findViewById(R.id.btn_duel_Stop);
        btn_Stop.setVisibility(View.INVISIBLE );
        btn_End = (Button) findViewById(R.id.btn_duel_End);
        n = (Integer.parseInt(distanceHolder)*1000)-500;

        tv_Time = (TextView) findViewById(R.id.tv_duel_time);
        tv_Distance = (TextView) findViewById(R.id.tv_duel_distance);
        tv_Coordinates = (TextView) findViewById(R.id.tv_duel_coord);
        tv_OldCoordinates = (TextView) findViewById(R.id.tv_duel_oldcoords);
        tv_DuelFriend = (TextView) findViewById(R.id.tv_duel_duelName);
        tv_DuelFriend.setText(friendname);
        tv_DuelTime = (TextView) findViewById(R.id.tv_duel_TimeToBeat);
        tv_DuelTime.setText(minutesRounded2 + ":" + timeSecs);
        tv_Distance.setText(" "+ distance);

        tv_Time.setText("");
        tv_OldCoordinates.setText("");
        tv_Coordinates.setText("GPS is NOT ready");

        latArray = new ArrayList<Double>();
        lonArray = new ArrayList<Double>();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mo = new MarkerOptions().position(new LatLng(0, 0)).title("My Current Location");

        toSpeech = new TextToSpeech(DuelActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    toSpeech.setSpeechRate(0.8f);
                    speechResult = toSpeech.setLanguage(Locale.US);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Feature not supported in your device", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted()) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        } else {
            requestLocation();
        }
        if (!isLocationEnabled()){
            showAlert(1);
        }

        startCode = 0;

        btn_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(distance == 3000 || distance == 5000 || distance == 100000) {

                    if(latArray.size() >= 1) {
                        startCode = 1;
                        mHandler.sendEmptyMessage(MSG_START_TIMER);
                        btn_Start.setVisibility(View.INVISIBLE);

                    }
                    else{
                        Toast.makeText(v.getContext(), "GPS is not ready - Please wait", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(v.getContext(), "", Toast.LENGTH_LONG).show();
                }
            }
        });
        btn_Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btn_End.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(v.getContext(), MapsActivity.class);
                if(latArray.size() > 1) {
                    i.putExtra("latArrayList", (Serializable) latArray);
                    i.putExtra("lonArrayList", (Serializable) lonArray);
                }
                i.putExtra("startcode", startCode);
                i.putExtra("finaltime", finalIntTime);
                i.putExtra("distance", distToRun);
                i.putExtra("dueltime", duelTime);
                i.putExtra("friendname", friendname);
                startActivity(i);

            }
        });

    }
    @Override
    public void onBackPressed() {
    }
    @Override
    public void onLocationChanged(Location location) {
        if (location!=null){
        double longitudea = location.getLongitude();
        double latitudeb = location.getLatitude();
    }
        if(latArray.size() >= 1) {
            tv_Coordinates.setText("GPS is ready!");
            tv_OldCoordinates.setText("");
        }
        myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
        latArray.add(myCoordinates.latitude);
        lonArray.add(myCoordinates.longitude);
        if(latArray.size() >= 2 && startCode == 1) {
            oldCoordinates = new LatLng(latArray.get(latArray.size()-2) , lonArray.get(lonArray.size()-2));
            tv_OldCoordinates.setText(String.valueOf(oldCoordinates.latitude) + " " + String.valueOf(oldCoordinates.longitude) );
            tempdistance = Haversine.distance(oldCoordinates.latitude, oldCoordinates.longitude, myCoordinates.latitude, myCoordinates.longitude);
            distance -= tempdistance*1000;
            int MetersRounded = (int) distance;

            tv_Distance.setText(String.valueOf(MetersRounded));

            if(n > distance){
                int metersRan = distToRun - n;
                n -= 500;
                long timeInSec = timer.getElapsedTimeSecs();
                long minutesSoFar = timeInSec / 60;
                double minutesRounded = Math.floor(minutesSoFar);
                int minutesRounded2 = (int) minutesRounded;
                long timeCalc = timer.getElapsedTimeSecs() % 60; // Number of seconds left in the minute.

                if(n <= -500 || distance <=0){
                    mHandler.sendEmptyMessage(MSG_STOP_TIMER);
                    tv_Distance.setText("" + 0);
                    finalTime = timeInSec;
                    finalIntTime = (int) finalTime;
                    long minutesFinal = finalTime / 60;
                    double minutesRoundedFinal = Math.floor(minutesFinal);
                    int minutesRoundedFinal2 = (int) minutesRoundedFinal;
                    long timeCalc2 = finalTime % 60; // Number of seconds left in the minute.
                    tv_Time.setText(minutesRounded2 + ":" + timeCalc2);
                    if(timeToBeat > finalIntTime){
                        int timeDifference = timeToBeat - finalIntTime;
                        String textToSpeak2 = "Well done!. You beat " + friendname + " by " +  timeDifference + " seconds!.. " + distToRunKM + " Kilometers in " +
                                minutesRoundedFinal2 + " minutes " + timeCalc2 + "seconds!";
                        toSpeech.speak(textToSpeak2, TextToSpeech.QUEUE_FLUSH, null);
                    }else
                    if(timeToBeat == finalIntTime){
                        String textToSpeak2 = "Done! You and " + friendname + " ran "  + distToRunKM + " Kilometers in " +
                        minutesRoundedFinal2 + " minutes " + timeCalc2 + " seconds!.. Exactly the same time!" ;
                        toSpeech.speak(textToSpeak2, TextToSpeech.QUEUE_FLUSH, null);
                    }else
                    if(timeToBeat < finalIntTime){
                        int timeDifference = finalIntTime - timeToBeat;
                        String textToSpeak2 = "Done! Unfortunately - " + friendname + " beat you by "  + timeDifference + " seconds..." +
                                " You ran " + distToRunKM + " Kilometers in" + minutesRoundedFinal2 + " minutes " + timeCalc2 + " seconds";
                        toSpeech.speak(textToSpeak2, TextToSpeech.QUEUE_FLUSH, null);
                    }
                    startCode = 2;

                }else{
                    if(avgTime500m > timeInSec){
                        double timeDifference = avgTime500m - timeInSec;
                        double metersCalc = Math.round((avgPacePrM * timeDifference)*10);
                        double metersDifference = metersCalc/10;
                        String textToSpeak2 = metersRan + " meters, in " + minutesRounded2 + " minutes, " + timeCalc + " seconds..." + "You are "
                                + metersDifference +  " meters - in front of " + friendname + ". Keep going!";
                        toSpeech.speak(textToSpeak2, TextToSpeech.QUEUE_FLUSH, null);
                    }
                    else if(avgTime500m == timeInSec) {
                        String textToSpeak3 = metersRan + " meters, in " + minutesRounded2 + " minutes, " + timeCalc + " seconds..." +
                                "You and " + friendname + " are side by side!";
                        toSpeech.speak(textToSpeak3, TextToSpeech.QUEUE_FLUSH, null);
                    }else if(avgTime500m < timeInSec){
                        double timeDifference = timeInSec - avgTime500m;
                        double metersCalc = Math.round((avgPacePrM * timeDifference)*10);
                        double metersDifference = metersCalc/10;
                        String textToSpeak2 = metersRan + " meters, in " + minutesRounded2 + " minutes, " + timeCalc + " seconds..." + "You are "
                                + metersDifference +  " meters - behind " + friendname + ". Run a bit faster!";
                        toSpeech.speak(textToSpeak2, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                avgTime500m += avgTime500mHolder;
                }

        }



    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void requestLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 5000, 3, this);
    }
    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isPermissionGranted() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v("mylog", "Permission is granted");
            return true;
        } else {
            Log.v("mylog", "Permission not granted");
            return false;
        }
    }
    private void showAlert(final int status) {
        String message, title, btnText;
        if (status == 1) {
            message = "Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                    "use this app";
            title = "Enable Location";
            btnText = "Location Settings";
        } else {
            message = "Please allow this app to access location!";
            title = "Permission access";
            btnText = "Grant";
        }
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        if (status == 1) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        } else
                            requestPermissions(PERMISSIONS, PERMISSION_ALL);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        finish();
                    }
                });
        dialog.show();
    }
}
