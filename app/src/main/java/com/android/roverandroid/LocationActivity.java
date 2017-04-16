package com.android.roverandroid;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;


import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.android.roverandroid.database.DbHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        SensorEventListener {

    private final static String TAG = "LocationActivityTAG";
    private TextView tvLatitude,tvLongitude,tvAccuracy,tvX,tvY,tvZ;
    private SwitchCompat switchCompatButton;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    protected Location mLastLocation;
    private Sensor mySensor;
    private SensorManager SM;
    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    boolean isMoving=false;

    private double lastLatitude,latitude,lastLongitude,longitude;
    private int accuracy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        //creating an object of GoogleApiCLient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        //Create our Sensor
        SM=(SensorManager)getSystemService(SENSOR_SERVICE);
        //Accelerometer Sensor
        mySensor=SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //defining textview and toggle Button
        tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        tvLongitude = (TextView) findViewById(R.id.tvLongitude);
        tvAccuracy = (TextView) findViewById(R.id.tvAccuracy);
        tvX=(TextView) findViewById(R.id.tvX);
        tvY=(TextView) findViewById(R.id.tvY);
        tvZ=(TextView) findViewById(R.id.tvZ);
        switchCompatButton = (SwitchCompat) findViewById(R.id.switchCompatButton);
        mGoogleApiClient.connect();
        //toggle button on CheckedChangeListener
      /*  switchCompatButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    mGoogleApiClient.connect();
                } else {
                    //disconnect google api client
                    mGoogleApiClient.disconnect();
                }
            }
        });*/
        DbHandler dbHandler=new DbHandler(this);
        dbHandler.getAllLocations();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //connect google api
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();

        //disconnect google api
    }
    @Override
    protected void onResume(){
        super.onResume();
        //Register Sensor Listener
        SM.registerListener(this,mySensor,SensorManager.SENSOR_DELAY_FASTEST);
    }
    @Override
    protected void onPause(){
        super.onPause();
        //unregister Senser Listener
        SM.unregisterListener(this);
    }

    /**
     * This Method handles the event when GoogleApiClient is connected
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation!=null)
            Log.e(TAG,"last fetched Location is"+mLastLocation.toString());
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); //update location every second

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG,"GoogleApiClient connection has been suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG,"GoogleApiClient connection has failed");
    }

    /**
     * When location change is detected
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG,location.toString());
        lastLatitude=latitude;
        lastLongitude=longitude;
        latitude=location.getLatitude();
        longitude=location.getLongitude();
        accuracy=(int)location.getAccuracy();
        tvLatitude.setText(String.valueOf(latitude));
        tvLongitude.setText(String.valueOf(longitude));
        tvAccuracy.setText(String.valueOf(accuracy));
       Log.e(TAG,"Lat : "+latitude+" Long : "+longitude+"Acc : "+accuracy);

      /* if(isMoving){
        if(isLocationChanged()) {
               Log.e(TAG, "Lat : " + latitude + " Last lat : " + lastLatitude + "Long : " + longitude + "Lastlong : " + lastLongitude);
               DbHandler dbHandler=new DbHandler(this);
               dbHandler.insertCurrentLocation(latitude,longitude,accuracy);
               dbHandler.getAllLocations();
           }
        }*/
    }

    /**
     * This method finds out if the location is actually changed
     * by taking abs differences of latitude and longitude with there old values
     * @return boolean
     */
    public boolean isLocationChanged(){

        if(Math.abs(latitude-lastLatitude)>0 || Math.abs(longitude-lastLongitude) >0)
            return true;
        else
            return false;
    }


    /**
     * Handles event when accelerometer value is changed
     * @param event
     */

    @Override
    public void onSensorChanged(SensorEvent event) {
        tvX.setText("X : "+String.valueOf(event.values[0]));
        tvY.setText("Y : "+String.valueOf(event.values[1]));
        tvZ.setText("Z : "+String.valueOf(event.values[2]));
      //  Log.e(TAG,"X : "+ event.values[0]+" Y : "+event.values[1]+" Z : "+event.values[2]);
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = event.values.clone();
            // Shake detection
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float)Math.sqrt(x * x + y * y + z*z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            mAccel=(float)round(mAccel,3);

            if(Math.abs(mAccel) > 1){
                isMoving=true;
            }
            else if(Math.abs(mAccel)<0.003 && Math.abs(mAccel)>0){
                isMoving=false;
            }
        }
        //Log.e("Moving : "," "+isMoving);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Method for accelerometer accuracyChanged event .Not in Use
    }
    /**
     *Static method for rounding
     * @param value
     * @param places
     * @return
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
