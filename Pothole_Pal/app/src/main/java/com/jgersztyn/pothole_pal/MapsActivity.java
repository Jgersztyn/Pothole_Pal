package com.jgersztyn.pothole_pal;

import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float last_x, last_y, last_z;

    //listener object for capturing motion
    private SensorEventListener listen;
    //time since a motion was last listened for
    private long lastUpdate = 0;

    //context required to interact with the db
    Context context = this;
    //data source for adding points into our db
    PinPointDataSrc data;

    //required for getting the current location
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    //TAG variable used to reference this class
    public static final String TAG = MapsActivity.class.getSimpleName();

    //used in handling connection errors with the GoogleApiClient
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

//    //not likely needed again
//    private static final long ONE_MIN = 1000 * 60;
//    private static final long TWO_MIN = ONE_MIN * 2;
//    private static final long FIVE_MIN = ONE_MIN * 5;
//    private static final float MIN_ACCURACY = 25.0f;
//    private static final float MIN_LAST_READ_ACCURACY = 500.0f;
//    Location bestLocation;
//    Location mLastLocation;
//    boolean mRequestingLocationUpdates = true;

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

        //set up the GoogleApiClient, defining the context for it to exist
        //this class will be handling the listeners
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //enable a location listener object, setting its rate of activity
        //we want HIGH accuracy, since the locations of potholes should be precise
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(8000)        //8 second interval
                .setFastestInterval(1000); //1 second interval
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
                    //marker is null to start
                    Location location = null;

                    //register the last known location of this device
                    try {
                        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    } catch (SecurityException e) {
                        Log.i(TAG, "Not able to get location");
                    }

                    //not a valid location
                    if (location == null) {

                        //there has to be something better than this damn try/catch blocks
                        try {
                            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                        } catch (SecurityException e) {
                            //should we do more here?
                            Log.i(TAG, "Better than nothing");
                        }
                    } else {
                        handleNewLocation(location);
                    }

//                    mMap.addMarker(new MarkerOptions()
//                            .position(new LatLng(44.842354, -123.2354))
//                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
//                            .title("The y axis moved on... " + currentDateTime));
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
    If the app is paused or suspended, once the app is no longer suspended, then
    this method is called and it ensures that the GoogleAPIClient is connected
    and out sensor listener is still registered
     */
    protected void onResume() {
        super.onResume();

        //connect to the client as needed
        mGoogleApiClient.connect();

        //resume the sensor as necessary
        //essentially, only when the screen is turned ON
        sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /*
    If the app is paused, then we need to disconnect from the GoogleAPIClient
    and also suppress the sensor as necessary
    Essentially, this is only when the screen is OFF
     */
    protected void onPause() {
        super.onPause();

        //close connection to client if not already closed
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        //disable the sensor for now
        sensorManager.unregisterListener((SensorEventListener) this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");

        Location location = null;

        //register the last known location of this device
        try {
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException e) {
            Log.i(TAG, "Not able to get location");
        }

        //not a valid location
        if (location == null) {

            //there has to be something better than this damn try/catch blocks
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } catch (SecurityException e) {
                //should we do more here?
                Log.i(TAG, "Better than nothing");
            }
        } else {
            //handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    /*
    Listens for changes in the location of the user's device and then call the function
    to handle it
     */
    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    /*
    If a location was registered, then we want to handle it here
     */
    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        //request and store the coordinates
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        //store the coordinates in a convenient object
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        //define a marker with the coordinates and helpful text
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title("Current location is " + Double.toString(currentLatitude) + ", " + Double.toString(currentLongitude));

        //add a marker to the map
        mMap.addMarker(options);
        //move to that location on the map

        //this moves to the current point on the map... uncomment this during a sprint!
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));
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
        //LatLng monmouthOR = new LatLng(44.8528, -123.2394);
        // Add a marker in Monmouth
        //mMap.addMarker(new MarkerOptions().position(monmouthOR).title("Western Oregon University"));

        /**************DATABASE ACCESS**************/
        /*******************************************/

        //provide access to the database here
        data = new PinPointDataSrc(context);
        try {
            //open access to the data source
            data.open();
        } catch (Exception er) {
            Log.i("oops", "This is an error with the database");
        }

        //a list containing every point inside of the database
        List<PinPointObj> points = data.getAllPoints();
        for (int i = 0; i < points.size(); i++) {
            //get the position of this particular point on the map
            String position = points.get(i).getPosition();

            //perform all formatting requirements here
            position = position.replace(",", "");
            String[] latLngVal = position.split(" ");
            double latitude = Double.parseDouble(latLngVal[0]);
            double longitude = Double.parseDouble(latLngVal[1]);

            //store the lat and long in an object which is understood
            LatLng location = new LatLng(latitude, longitude);

            //add this point onto the map
            mMap.addMarker(new MarkerOptions()
                            .title(points.get(i).getText())
                                    //icon is not part of the database, but it should be when ranking by severity
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .position(location)
            );

            data.close();
        }

        String locateMe = "44.864763, -123.014778";

        //marker to add
        //data.addMarker(new PinPointObj("this point is near my house", locateMe));
        //data.addMarker(new PinPointObj("I will add this as a test!", "44.694763, -122.914778"));

        //close the data source
        //data.close();

        /************END DATABASE ACCESS************/
        /*******************************************/

        /*************DELETE DATA POINT*************/
        /*******************************************/

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            //upon clicking the text bubble of any given point, the marker will be removed from the map
            @Override
            public void onInfoWindowClick(Marker marker) {

                try {
                    //open access to the data source
                    data.open();
                } catch (Exception er) {
                    Log.i("oops", "This is an error with the database");
                }

                //actually removes the marker from the map
                marker.remove();
                //query to remove the marker from the database as well
                //we need to remove the two fields associated with it, which are text and position

                String s = marker.getPosition().latitude
                        + " " + marker.getPosition().longitude;

                //remove the marker from the database
                data.deleteMarker(new PinPointObj(marker.getTitle(), marker.getPosition().latitude
                        + ", " + marker.getPosition().longitude));

                //close the data source
                data.close();
            }
        });

        /**********END OF DELETION CONTEXT**********/
        /*******************************************/

        // Zoom to this point on the map
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.8608, -123.1394), 8.9f));
    }
}
