package com.jgersztyn.pothole_pal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener,
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //set up for the accelerometer
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener((SensorEventListener) listen, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //set up the content and view of this activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //obtain the SupportMapFragment and get notified when the map is ready to be used.
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
                .setInterval(1000)          //1 second interval
                .setFastestInterval(100);   //0.1 second interval
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

            //take the three points and...
            double vectorProduct = Math.sqrt(x * x + y * y + z * z);

            //SimpleDateFormat date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z");
            //String currentDateTime = date.format(new Date());

                /*if (Math.abs(last_x - x) > 10) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(44.842354, -123.2304))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .title("The x axis moved..." + currentDateTime));
                }*/

            //listen for movement along the y-axis
            //the logic statement dictates the amount of movement which needs to occur for a response
            //if (Math.abs(last_y - y) > 5) {

            //listen for a significant amount of movement; a higher number means less sensitivity
            if (Math.abs(vectorProduct) > 14) {

                //add a marker at the specified coordinates
                //marker is null to start
                Location location = null;

                //register the last known location of this device
                try {
                    location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                } catch (SecurityException e) {
                    Log.i(TAG, "Not able to get location");
                }

                //gets the current time at this very moment
                long currentTime = System.currentTimeMillis();

                //we do not attempt to add a point if the last update was less than x (7) seconds ago
                if (Math.abs(currentTime - lastUpdate) > (7 * 1000)) {

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
                        //validate the change in location and record it
                        onLocationChanged(location);
                        //may need to think of a different area in the code to process this location management
                        //currently it records the data twice, which means it does it here
                        //and also from the listener itself
                    }

                    //the last moment in time a point was added
                    //update this when a new point is added
                    lastUpdate = currentTime;
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

            //last_x = x;
            //last_y = y;
            //last_z = z;
        }
    }

    /*
needs to be here, I guess.
 */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /*
    Implements the functionality of the search bar
     */
    public void onSearch(View view) {

        try {
            EditText location_tf = (EditText) findViewById(R.id.TFaddress);
            String location = location_tf.getText().toString();

            //Toast.makeText(this, "You typed " + location, Toast.LENGTH_SHORT).show();

            List<Address> addressList = null;

            if (location != null || !location.equals("")) {

                Geocoder geocoder = new Geocoder(this);
                try {
                    addressList = geocoder.getFromLocationName(location, 1);
                } catch (IOException e) {
                    Log.i(TAG, "Location not found");
                }

                Address address = addressList.get(0);
                LatLng latNlng = new LatLng(address.getLatitude(), address.getLongitude());
                //add marker here, if we want!

                //move to location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latNlng, 15.0f));
            }
        } catch (Exception e) {
            Toast.makeText(this, "No location found", Toast.LENGTH_SHORT).show();
        }
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

            //there has to be something better than these try/catch blocks
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } catch (SecurityException e) {
                //should we do more here?
                Log.i(TAG, "Better than nothing");
            }
        } else {
            //if the location exists, a new marker will be added there
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

        /* A convenient way to round the coordinates to less decimal places
        * Note that this is only for the sake of readability. We do not want
        * to store these rounded values in the database
        * */
//        DecimalFormat dflat = new DecimalFormat("#.###");
//        currentLatitude = Double.valueOf(dflat.format(currentLatitude));
//        DecimalFormat dflon = new DecimalFormat("#.###");
//        currentLongitude = Double.valueOf(dflon.format(currentLongitude));

        //get the current date and time
        SimpleDateFormat date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z");
        String currentDateTime = date.format(new Date());
        //define a marker with the coordinates and helpful text
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title("Location: " + Double.toString(currentLatitude) + ", "
                        + Double.toString(currentLongitude) + "; Time: " + currentDateTime);

        //add a marker to the map; does not add to database
        mMap.addMarker(options);


        //DATABASE OPTIONS

        //allow access to the database
        data.open();
        //convert lat and lng to strings
        String lattttt = Double.toString(location.getLatitude());
        String loooong = Double.toString(location.getLongitude());
        //add a marker to the map and store it inside of the database
        data.addMarker(new PinPointObj("Location: " + Double.toString(currentLatitude) + ", "
                + Double.toString(currentLongitude) + "; Time: " + currentDateTime, lattttt + ", " + loooong));
        //close the data source
        data.close();
        //move to that location on the map
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                location.getLongitude()), 18.0f));
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

        /**CREATE LISTENER TO ADD DATA POINT MANUALLY**/
        /**********************************************/

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            public void onMapLongClick(final LatLng latlng) {
                LayoutInflater layout = LayoutInflater.from(context);
                final View v = layout.inflate(R.layout.alert_layout, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setView(v);
                builder.setCancelable(false);

                builder.setPositiveButton("Add Marker", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        //get the text from the field inside of the window
                        EditText title = (EditText) v.findViewById(R.id.atitle);

                        mMap.addMarker(new MarkerOptions()
                                        .position(latlng)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                        .title(title.getText().toString())
                                //.title(latlng.toString())
                        );


                        //allow access to the database
                        data.open();
                        //convert lat and lng to strings
                        String lat = Double.toString(latlng.latitude);
                        String lng = Double.toString(latlng.longitude);

                        //add a marker to the map and store it inside of the database

                        data.addMarker(new PinPointObj(title.getText().toString(), lat + ", " + lng));
                        //close the data source
                        data.close();
                        //move to that location on the map
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 12.0f));
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        /**END LISTENER WHICH ADDS DATA POINT MANUALLY**/
        /***********************************************/

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

        //String locateMe = "44.864763, -123.014778";
        //data.open();
        //marker to add
        //data.addMarker(new PinPointObj("this point is near my house", locateMe));
        //data.addMarker(new PinPointObj("I will add this as a test!", "44.794763, -122.994778"));
        //close the data source
        //data.close();

        /************END DATABASE ACCESS************/
        /*******************************************/

        /*******DELETE AND MODIFY DATA POINT********/
        /*******************************************/

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            //upon clicking on the window for a point, a series of options will be presented to the user
            public void onInfoWindowClick(final Marker marker) {

                LayoutInflater layout = LayoutInflater.from(context);
                final View v = layout.inflate(R.layout.edit_layout, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setView(v);
                builder.setCancelable(false);

                builder.setNeutralButton("Confirm Edit", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        //get the text from the field inside of the window
                        EditText title = (EditText) v.findViewById(R.id.edttitle);
                        //update the text of the title
                        marker.setTitle(title.getText().toString());

                        //this does not update in the database...
                    }
                });

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        //here we attempt to delete a point from the map
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

                builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        /**********END OF DELETION CONTEXT**********/
        /*******************************************/

        //zoom to this point on the map at start
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.8608, -123.1394), 8.9f));
    }
}
