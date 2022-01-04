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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import java.util.Locale;

/**
 * Created by Troels on 28-09-2017.
 */

public class RunActivity extends FragmentActivity implements  LocationListener {
    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private GoogleMap mMap;
    int timeFinal;
    int n;
    int startCode;
    Spinner distanceChooser;
    ArrayAdapter<CharSequence> distanceAdapter;
    TextToSpeech toSpeech;
    MarkerOptions mo;
    LocationManager locationManager;
    TextView tv_Distance;
    TextView tv_Coordinates;
    TextView tv_OldCoordinates;
    TextView tv_Time;
    TextView tv_Pace;
    LatLng oldCoordinates;
    LatLng myCoordinates;
    List<Double> latArray;
    List<Double> lonArray;
    List<Double> latArray2;
    List<Double> lonArray2;
    double distToRun;
    double tempdistance;
    double distance;
    public double finalDistance;
    public long finalTime;
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
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,REFRESH_RATE);
                    //text view is updated every second,
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
        setContentView(R.layout.activity_run);
        n = 500;
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mo = new MarkerOptions().position(new LatLng(0, 0)).title("My Current Location");
        distanceChooser = (Spinner) findViewById(R.id.spinner_Distance);
        distanceAdapter = ArrayAdapter.createFromResource(this, R.array.running_distances, android.R.layout.simple_spinner_item);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceChooser.setAdapter(distanceAdapter);

        distanceChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    distToRun = 3000;
                }
                if(position ==  1){
                    distToRun = 5000;
                }
                if(position == 2){
                    distToRun = 10000 ;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_Start = (Button) findViewById(R.id.btn_start);
        btn_Stop = (Button) findViewById(R.id.btn_Stop);
        btn_End = (Button) findViewById(R.id.btn_End);
        btn_End.setVisibility(View.INVISIBLE);
        tv_Time = (TextView) findViewById(R.id.tv_time);
        tv_Distance = (TextView) findViewById(R.id.tv_distance);
        tv_Coordinates = (TextView) findViewById(R.id.tv_coord);
        tv_OldCoordinates = (TextView) findViewById(R.id.tv_oldcoords);
        tv_Pace = (TextView) findViewById(R.id.tv_Pace);
        latArray = new ArrayList<Double>();
        lonArray = new ArrayList<Double>();
        toSpeech = new TextToSpeech(RunActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
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

        distance = 0;
        startCode = 0;

        btn_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(distToRun == 3000 || distToRun == 5000 || distToRun == 100000) {

                    if(latArray.size() >= 1) {
                        startCode = 1;
                        mHandler.sendEmptyMessage(MSG_START_TIMER);
                        btn_Start.setVisibility(View.INVISIBLE);
                        tv_Distance.setText("" + distToRun);
                        distance = distToRun;
                        n = (int) distance - 500;

                    }
                    else{
                        Toast.makeText(v.getContext(), "GPS is not ready - Please wait", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(v.getContext(), "Please choose distance to start!", Toast.LENGTH_LONG).show();
                }
            }
        });
        btn_Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalTime = timer.getElapsedTimeSecs();
                mHandler.sendEmptyMessage(MSG_STOP_TIMER);
                btn_End.setVisibility(View.VISIBLE);
                btn_Stop.setVisibility(View.INVISIBLE);

            }
        });
        btn_End.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalDistance = distToRun - distance;
                timeFinal = (int) finalTime;

                Intent i = new Intent(v.getContext(), MapsActivity.class);
                if(latArray.size() >= 1) {
                    startCode = 3;
                    i.putExtra("latArrayList", (Serializable) latArray);
                    i.putExtra("lonArrayList", (Serializable) lonArray);
                    i.putExtra("distance", finalDistance);
                    i.putExtra("finaltime", finalTime);
                    i.putExtra("startcode", startCode);
                }
                startActivity(i);

            }
        });



    }

    @Override
    public void onLocationChanged(Location location) {
        if (location!=null){
            double longitudea = location.getLongitude();
            double latitudeb = location.getLatitude();
        }
        if(latArray.size() >= 1) {
            //tv_Coordinates.setText("GPS is ready!");
            //tv_OldCoordinates.setText("");
        }

        myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
        latArray.add(myCoordinates.latitude);
        lonArray.add(myCoordinates.longitude);
        tv_Coordinates.setText(String.valueOf(myCoordinates.latitude) + " " + String.valueOf(myCoordinates.longitude));

        if(latArray.size() >= 2 && startCode == 1) {
            oldCoordinates = new LatLng(latArray.get(latArray.size()-2) , lonArray.get(lonArray.size()-2));
            tv_OldCoordinates.setText(String.valueOf(oldCoordinates.latitude) + " " + String.valueOf(oldCoordinates.longitude) );
            tempdistance = Haversine.distance(oldCoordinates.latitude, oldCoordinates.longitude, myCoordinates.latitude, myCoordinates.longitude);
            distance -= tempdistance*1000;


            int distanceRound = (int) Math.ceil(distance);
            tv_Distance.setText(String.valueOf(distance));
            if(n < distance){

                long timeInSec = timer.getElapsedTimeSecs();
                long minutesSoFar = timeInSec / 60;
                double minutesRounded = Math.floor(minutesSoFar);
                int minutesRounded2 = (int) minutesRounded;
                long timeCalc = timer.getElapsedTimeSecs() % 60; // Number of seconds left in the minute.
                String textToSpeak = n + "Meters, in " + minutesRounded2 + " minutes and " + timeCalc + " seconds.";
                toSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
                n -= 500;
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
        locationManager.requestLocationUpdates(provider, 10000, 10, this);
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
