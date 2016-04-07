package com.sethi.gurdane.usfhvz;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HeatMap extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    //Declare button views
    ImageButton btMyLocation;
    ImageButton btAlert;
    ImageButton btHelp;

    //Declare variables
    String playerTeam;
    String opposingTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);
        setUpMapIfNeeded();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        //Initialize class variables
        playerTeam = Login.pref.getString(Login.KEY_STATE, "");
        if (playerTeam.equals("Human")) {
            opposingTeam = "Zombie";
        } else {
            opposingTeam = "Human";
        }

        //Initialize buttons
        btMyLocation = (ImageButton)findViewById(R.id.bt_my_location);
        btAlert = (ImageButton)findViewById(R.id.bt_enemy_sighted);
        btHelp = (ImageButton)findViewById(R.id.bt_help);

        btAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                //perform action
                UploadAlert obj = new UploadAlert();
                obj.execute();
            }
        });

        btHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //perform action
                TestUploadLocation obj = new TestUploadLocation();
                obj.execute();
            }
        });

        //Place existing alerts on page
        LoadLocations ll = new LoadLocations();
        ll.execute();
    }

    public class LoadLocations extends AsyncTask<String, Void, String> {

        int hour;
        int min;
        List<USFHvZ_Location> locations;
        String toastString;

        @Override
        protected void onPreExecute() {
            //onPreExecute
        }

        @Override
        protected String doInBackground(String...params) {
            //try {
                AmazonDynamoDBClient client = new AmazonDynamoDBClient(Home.credentialsProvider);
                DynamoDBMapper map = new DynamoDBMapper(client);

                //Get current time and adjust to one hour ago
                Calendar c = Calendar.getInstance(); //create calendar
                c.setTime(new Date()); //set calendar to current date
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");

                //Get current hour and minute
                SimpleDateFormat sdfHour = new SimpleDateFormat("HH");
                SimpleDateFormat sdfMinute = new SimpleDateFormat("mm");
                min = Integer.parseInt(sdfMinute.format(c.getTime()));
                hour = Integer.parseInt(sdfHour.format(c.getTime()));

                String queryString2 = sdf.format(c.getTime()); //get current time
                c.add(Calendar.HOUR_OF_DAY, -1); //subtract one hour
                String queryString1 = sdf.format(c.getTime()); //get hour one past

                USFHvZ_Location locationsToFind = new USFHvZ_Location();
                locationsToFind.setTeam(opposingTeam);

                //Perform two queries: One for the previous hour, one for the current hour.

                Condition rangeKeyCondition1 = new Condition()
                        .withComparisonOperator(ComparisonOperator.BEGINS_WITH.toString())
                        .withAttributeValueList(new AttributeValue().withS(queryString1.toString()));
                Condition rangeKeyCondition2 = new Condition()
                        .withComparisonOperator(ComparisonOperator.BEGINS_WITH.toString())
                        .withAttributeValueList(new AttributeValue().withS(queryString2.toString()));

                DynamoDBQueryExpression queryExpression1 = new DynamoDBQueryExpression()
                        .withHashKeyValues(locationsToFind)
                        .withRangeKeyCondition("DateTime", rangeKeyCondition1)
                        .withConsistentRead(false);
                DynamoDBQueryExpression queryExpression2 = new DynamoDBQueryExpression()
                        .withHashKeyValues(locationsToFind)
                        .withRangeKeyCondition("DateTime", rangeKeyCondition2)
                        .withConsistentRead(false);

                PaginatedQueryList<USFHvZ_Location> result1 = map.query(USFHvZ_Location.class, queryExpression1);
                PaginatedQueryList<USFHvZ_Location> result2 = map.query(USFHvZ_Location.class, queryExpression2);

                //Add query results to locations list if they are within the last hour
                locations = new ArrayList<USFHvZ_Location>();
                //Delete below if addAll works
                for (int i=0; i<result1.size(); i++) {
                    USFHvZ_Location a = result1.get(i);
                    if (a.getMinute() >= min) {
                        locations.add(a);
                    }
                }
                for (int i=0; i<result2.size(); i++) {
                    USFHvZ_Location a = result2.get(i);
                    locations.add(a);
                }
                toastString = "result1.size = " + result1.size()
                        + "; result2.size() = " + result2.size()
                        + "; locations.size() = " + locations.size();

            //} catch (Exception e) {
                //handle exception
            //}
            return null;
        }

        protected void onPostExecute(String page) {
            //onPostExecute, draw a marker of the appropriate color for each location
            Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_SHORT).show();
            for (int i=0; i<locations.size(); i++) {
                USFHvZ_Location location = locations.get(i);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude()); //get latitude & longitude of marker

                //Calculate difference between current time and time of location
                String currentTime = hour + ":" + min;
                String locationTime = location.getHour() + ":" + location.getMinute();
                SimpleDateFormat sdfHM = new SimpleDateFormat("HH:mm");
                long difference = -1;
                try {
                    Date c = sdfHM.parse(currentTime);
                    Date l = sdfHM.parse(locationTime);
                    difference = c.getTime() - l.getTime();
                    difference = difference /(60*1000) % 60;
                } catch (ParseException e) {
                    //handle exception
                }
                if (difference != -1) {
                    MarkerOptions options;
                    if (difference < 5) {
                        options = new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.zombie_marker_1));
                    } else if (difference < 10) {
                        options = new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.zombie_marker_2));
                    } else if (difference < 15) {
                        options = new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.zombie_marker_3));
                    } else if (difference <30) {
                        options = new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.zombie_marker_4));
                    } else if (difference < 45) {
                        options = new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.zombie_marker_5));
                    } else {
                        options = new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.zombie_marker_6));
                    }
                    mMap.addMarker(options);
                } else {
                    Toast.makeText(getApplicationContext(), "Error loading dynamic map.", Toast.LENGTH_SHORT).show();
                }


                /*MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)));*/
                //

            }
        }
    }

    public class UploadAlert extends AsyncTask<String, Void, String> {

        private String formattedDate = "";
        private double currentLatitude;
        private double currentLongitude;
        private int h;
        private int m;

        @Override
        protected void onPreExecute() {
            //onPreExecute
        }

        @Override
        protected String doInBackground(String...params) {
            try {
                AmazonDynamoDBClient client = new AmazonDynamoDBClient(Home.credentialsProvider);
                DynamoDBMapper map = new DynamoDBMapper(client);

                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdfTotal = new SimpleDateFormat("yyyyMMddHHmmss");
                SimpleDateFormat sdfHour = new SimpleDateFormat("HH");
                SimpleDateFormat sdfMinute = new SimpleDateFormat("mm");
                formattedDate = sdfTotal.format(c.getTime());
                h = Integer.parseInt(sdfHour.format(c.getTime()));
                m = Integer.parseInt(sdfMinute.format(c.getTime()));

                //Get current latitude and longitude
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (location == null) {
                    formattedDate = "Error uploading your alert.";
                }
                else { //Upload current location
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                    formattedDate = formattedDate
                            + " " + String.valueOf(currentLatitude)
                            + " " + String.valueOf(currentLongitude);

                    //Create new location object and upload to database
                    USFHvZ_Location alert = new USFHvZ_Location();
                    alert.setTeam("Zombie");
                    alert.setDateTime(formattedDate);
                    alert.setLatitude(currentLatitude);
                    alert.setLongitude(currentLongitude);
                    alert.setHour(h);
                    alert.setMinute(m);

                    map.save(alert);
                }

            } catch (Exception e) {
                //handle exception
            }
            return null;
        }

        protected void onPostExecute(String page) {
            //onPostExecute
            Toast.makeText(getApplicationContext(), formattedDate, Toast.LENGTH_SHORT).show();
        }
    }

    public class TestUploadLocation extends AsyncTask<String, Void, String> {

        USFHvZ_Location alert;
        USFHvZ_Announcement ann;

        @Override
        protected void onPreExecute() {
            //onPreExecute
        }

        @Override
        protected String doInBackground(String...params) {
            try {
                AmazonDynamoDBClient client = new AmazonDynamoDBClient(Home.credentialsProvider);
                DynamoDBMapper map = new DynamoDBMapper(client);

                alert = new USFHvZ_Location();
                alert.setTeam("Human");
                alert.setDateTime("4/6/2016");
                alert.setHour(10);
                alert.setMinute(53);
                alert.setLatitude(-10.6);
                alert.setLongitude(5.3);

                map.save(alert);

                ann = new USFHvZ_Announcement();
                ann.setDateTime("Test");
                ann.setTitle("HeatMap Announcement");
                ann.setBody("If you see this, we have a problem with USFHvZ_Announcement");
                map.save(ann);
            } catch (Exception e) {
                //handle exception
            }
            return null;
        }

        protected void onPostExecute(String page) {
            if (alert == null && ann == null) {
                Toast.makeText(getApplicationContext(), "Both null", Toast.LENGTH_SHORT).show();
            } else if (alert == null) {
                Toast.makeText(getApplicationContext(), "Alert is null", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Ann is null", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }
    }

    private void handleNewLocation(Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        /*MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Your location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mMap.addMarker(options);*/
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //nothing, really
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dynamic_map, menu);
        return true;
    }
}
