package com.th.monicadzhaleva.treasurehunt;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import android.support.v7.widget.Toolbar;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private User activeUser;
    private TextView userGreeting;
    private TextView userLevel;
    private TextView userExperience;
    private ImageButton arrowDown;
    private ImageButton huntButton;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    MarkerOptions currLocOptions;
    LocationRequest mLocationRequest;
    final ArrayList<Treasure> treasuresList=new ArrayList<Treasure>();
    FirebaseDatabase database;
    private Toolbar mTopToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        database = FirebaseDatabase.getInstance();
        mTopToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mTopToolbar.setTitle("");
        mTopToolbar.setSubtitle("");
        getUserDetails();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MapStyleOptions styleOptions= MapStyleOptions.loadRawResourceStyle(this, R.raw.googlestyle);
        mMap.setMapStyle(styleOptions);
        mMap.setOnMarkerClickListener(this);
        //Initialize Google Play Services

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);


            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_userprofile) {
            Intent i=new Intent(MapsActivity.this, UserActivity.class);
            i.putExtra("username",activeUser.getUsername());
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        currLocOptions = new MarkerOptions();
        currLocOptions.position(latLng);
        currLocOptions.title(activeUser.getUsername());
        currLocOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.person));
        mCurrLocationMarker = mMap.addMarker(currLocOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    public void getUserDetails()
    {
        // Get user details from login intent screen
        activeUser=new User();
        final DatabaseReference activeUserRef=database.getReference().child("users").child(getIntent().getStringExtra("username"));
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                activeUser= dataSnapshot.getValue(User.class);
                setUserToolbar(activeUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        activeUserRef.addValueEventListener(postListener);
    }

    public void setUserToolbar(final User user)
    {
        userGreeting=(TextView) findViewById(R.id.usergreeting);
        userLevel=(TextView) findViewById(R.id.level);
        userExperience=(TextView) findViewById(R.id.experience);
        huntButton= (ImageButton) findViewById(R.id.huntButton);

        huntButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Hunt","Hunt");
                getNearbyTreasures();

            }
        });

        userGreeting.setText("Hello, " + user.getUsername());
        userLevel.setText("Level: " + user.getLevel());
        userExperience.setText("Experience: " +  user.getExperience());
    }

    public void getNearbyTreasures()
    {
        final DatabaseReference treasuresRef=database.getReference().child("treasures");
        treasuresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Treasure treasure = snapshot.getValue(Treasure.class);
                    treasuresList.add(treasure);
                }

                injectTreasuresToMap(treasuresList);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void setCurrLoc()
    {
        mCurrLocationMarker = mMap.addMarker(currLocOptions);
    }

    public void injectTreasuresToMap(ArrayList<Treasure> treasuresList)
    {
        for(final Treasure treasure: treasuresList)
        {
            final LatLng latLng = new LatLng(treasure.getLatitude(), treasure.getLongitude());
            final DatabaseReference userToTreasureRef=database.getReference().child("user_to_treasure");
            final DatabaseReference treasureCollectionRef=userToTreasureRef.child(activeUser.getUsername()).child(treasure.getName());
            treasureCollectionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserToTreasure userToTreasure = dataSnapshot.getValue(UserToTreasure.class);
                    if(userToTreasure!=null) {
                        if (userToTreasure.isCollected()) {
                            // user has already collected the treasure: do not display!
                            Log.i(treasure.getName(), " already collected");
                            return;
                        }else
                        {
                            // user has not collected the treasure
                            addTreasureMarker(treasure,latLng);

                        }
                    }else {
                        addTreasureMarker(treasure,latLng);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    public void addTreasureMarker(Treasure treasure, LatLng latLng)
    {
        Marker newMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(treasure.getName()));
        newMarker.setSnippet("Type: " + treasure.getType() + " | Points: " + treasure.getPoints());
        newMarker.setTag(treasure.getPoints());
        if (treasure.getType() != null) {
            if (treasure.getType().equals("bronze")) {
                newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bronze));
            } else if (treasure.getType().equals("silver")) {
                newMarker.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.silver)));
            } else if (treasure.getType().equals("gold")) {
                newMarker.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.gold)));
            }
        } else {
            Log.i(treasure.getName(), "No type defined for this treasure");
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.i("CLICK", "Marker was clicked");
        Log.i("Marker Position: ", marker.getPosition().toString());
        if (!marker.getTitle().equals(activeUser.getUsername())) {
            /* Marker Location */
            LatLng markerPos = marker.getPosition();
            Location markerLoc = new Location("");
            markerLoc.setLatitude(markerPos.latitude);
            markerLoc.setLongitude(markerPos.longitude);

            /* User Current Location */
            if (mCurrLocationMarker != null) {
                LatLng userPos = mCurrLocationMarker.getPosition();
                Location userLoc = new Location("");
                userLoc.setLatitude(userPos.latitude);
                userLoc.setLongitude(userPos.longitude);

                float distanceInMeters = userLoc.distanceTo(markerLoc);
                if (distanceInMeters <= 1000) // if distance in meters is less than 1000
                {
                    // User can collect the treasure
                    Log.i("Alert", "User can collect the treasure");
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Dear traveller, you are close enough to collect the treasure!");
                    builder.setTitle("Collect the treasure: " + marker.getTitle());
                    builder.setIcon(R.drawable.icon);
                    builder.setPositiveButton("Collect", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //TODO: User collects the treasure! Treasure is added in user_to_treasure db

                            // Mark treasure as "collected"
                            UserToTreasure user_to_treasure = new UserToTreasure();
                            user_to_treasure.setCollected(true);
                            final DatabaseReference userToTreasureRef = database.getReference().child("user_to_treasure");
                            userToTreasureRef.child(activeUser.getUsername()).child(marker.getTitle()).setValue(user_to_treasure);

                            // Add experience points to user
                            final DatabaseReference userAccRef = database.getReference().child("users").child(activeUser.getUsername());
                            final DatabaseReference expRef=userAccRef.child("experience");


                            expRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    long value =(long) dataSnapshot.getValue();
                                    value = value + Integer.parseInt(marker.getTag().toString());
                                    dataSnapshot.getRef().setValue(value);
                                    // user - > treasure -- > collected = true
                                    mMap.clear();
                                    setCurrLoc();
                                    getNearbyTreasures();
                                    Toast.makeText(MapsActivity.this, "Sucessfully collected", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }

                            });

                            // Check if user has leveled up or not
                            final int userExperience= activeUser.getExperience() + Integer.parseInt(marker.getTag().toString());
                            int userLevel =activeUser.getLevel();
                            final int nextLevel=userLevel+1;
                            final DatabaseReference levelRef = database.getReference().child("level").child(nextLevel+"").child("max_exp");

                            levelRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    long maxExp =(long) dataSnapshot.getValue();

                                    if(userExperience>=maxExp)
                                    {
                                        Log.i("LEVEL UP!","User has succesfully leveled up.");
                                        ImageView view = new ImageView(MapsActivity.this);
                                        view.setImageResource(R.drawable.levelup);
                                        Toast toast = Toast.makeText(MapsActivity.this, "message", Toast.LENGTH_LONG);
                                        toast.setText("LEVEL UP! You have succesfully leveled up.");
                                        toast.setGravity(Gravity.BOTTOM, 0, 200);
                                        toast.setView(view);
                                        toast.show();

                                        // Add the extra level:
                                        DatabaseReference levelRef=userAccRef.child("level");
                                        levelRef.setValue(nextLevel);
                                        // Check if any experience is carried on
                                        DatabaseReference expRef=userAccRef.child("experience");
                                        int extraExperience = (int) ( userExperience-maxExp);
                                        if(extraExperience>0)
                                        {
                                            expRef.setValue(extraExperience);
                                        }else{
                                            expRef.setValue(0);
                                        }
                                    }else
                                    {
                                        int experienceNeeded= (int) (maxExp-userExperience);
                                        Log.i("Still cannot level up", "User needs " + experienceNeeded+ " experience to level up.");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }

                            });

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    AlertDialog dialog = builder.create();

                    dialog.show();

                } else {
                    // User is too far to collect the treasure
                    Log.i("Alert", "User is too far to collect the treasure");
                    Toast.makeText(this, "You are too far to collect the treasure!", Toast.LENGTH_LONG).show();

                }
            }
        }
        return false;
    }

}