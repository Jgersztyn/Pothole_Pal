package com.jgersztyn.pothole_pal;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float last_x, last_y, last_z;
    private SensorEventListener listen;

    private long lastUpdate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //setup for the accelerometer
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener((SensorEventListener) listen, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /*
    listener for changes in movement
  */
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        //ensure that the type of sensor we are grabbing is of type Accelerometer
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long currentTime = System.currentTimeMillis();

            //we do not update anything if the last update was less than 2 seconds ago
            if (Math.abs(currentTime - lastUpdate) > 2000) {

                SimpleDateFormat date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z");
                String currentDateTime = date.format(new Date());
                lastUpdate = currentTime;

                /*if (Math.abs(last_x - x) > 10) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(44.842354, -123.2304))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .title("The x axis moved..." + currentDateTime));
                }*/

                //listen for movement along the y-axis
                //the logic statement dictates the amount of movement which needs to occur for a response
                if (Math.abs(last_y - y) > 10) {
                    //add a marker at the specified coordinates
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(44.842354, -123.2354))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                            .title("The y axis moved on... " + currentDateTime));
                }

                /*if (Math.abs(last_z - z) > 10) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(44.842354, -123.2434))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .title("The z axis moved..." + currentDateTime));
                }*/

                last_x = x;
                last_y = y;
                //last_z = z; //MAY NEED THIS LATER
            }
        }
    }

    /*
    needs to be here, I guess.
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /*
    resume the sensor as necessary
    essentially, only when the screen is turned ON
     */
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /*
    suppress the sensor as necessary
    essentially, only when the screen is OFF
     */
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener((SensorEventListener) this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker at Western Oregon University
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Location a marker will be added
        LatLng monmouthOR = new LatLng(44.8528, -123.2394);
        // Add a marker in Monmouth
        mMap.addMarker(new MarkerOptions().position(monmouthOR).title("Western Oregon University"));
        // Zoom to this point on the map
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.8528, -123.2394), 14.9f));
    }
}
