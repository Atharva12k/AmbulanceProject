package com.example.hp.ambulenceproject;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getName();
    private GoogleMap mMap;
    final private int REQUEST_COURSE_ACCESS = 123;
    boolean permissionGranted = false;
    private FusedLocationProviderClient mfusedlocationclient;
    private LatLng currentLatLng;
    private FirebaseFirestore firestore;
    private DocumentReference documentReference;
    private FirebaseUser user;
    FirebaseAuth firebaseAuth;
    private GeoPoint geoPoint;
    private HashMap<String,GeoPoint> hashMap;
    private String name,email,stringLocation;
    private LatLng victimLocation;
    private boolean thisIsIntentCall = false;
    private TextView info;
    private Button navigate;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


            if (getIntent().getExtras() != null && getIntent().getStringExtra("Name") != null) {
                name = (String) getIntent().getStringExtra("Name");
                Log.d("Received in mapActivity", "Name: + " + name);
                email = (String) getIntent().getStringExtra("Email");
                Log.d("Received in mapActivity", "Email: + " + email);
                stringLocation = (String) getIntent().getStringExtra("Location");
                Log.d("Received in mapActivity", "Location: + " + stringLocation);
                thisIsIntentCall = true;
            }

            info = findViewById(R.id.info);
            navigate = findViewById(R.id.navigate);
            firestore = FirebaseFirestore.getInstance();
            firebaseAuth = FirebaseAuth.getInstance();
            hashMap = new HashMap<>();
            mfusedlocationclient = LocationServices.getFusedLocationProviderClient(this);


            //FCM
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            final String token = task.getResult().getToken();


                            Map<String, Object> map = new HashMap<>();
                            map.put("Token", token);
                            firestore.collection("RESPONDER LIST").document(firebaseAuth.getCurrentUser().getUid())
                                    .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        Constants.Token = token;
                                    //Toast.makeText(getApplicationContext(), "Token written to firebase", Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Log and toast
                            String msg = "your token is: " + token;
                            Log.d("REGISTERATION TOKEN", msg);
                            //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    });



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }
            Log.d(TAG, "OnMapREADY Detected");
            mMap = googleMap;

            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_COURSE_ACCESS);
                return;
            } else {
                permissionGranted = true;
            }
            moveToUserLocation();
            mMap.setMyLocationEnabled(true);
            mMap.setTrafficEnabled(false);
            startLocationTracking();

            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    currentLatLng = intent.getExtras().getParcelable("CURRENT_LATLNG");
                    Constants.CurrentLocation = currentLatLng;
                    updateLocationInFirebase();
                    if (marker != null) {
                        marker.remove();
                    }
                    MarkerOptions markerOptions = new MarkerOptions().position(currentLatLng);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    if(marker!=null)
                        marker.remove();
                    marker = mMap.addMarker(markerOptions);

                }
            };
            LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                    new IntentFilter(MapService.MAP_SERVICE_BROADCAST)
            );


    }

    void startLocationTracking()
    {
        Intent serviceIntent = new Intent(this, MapService.class);
        serviceIntent.putExtra("PERMISSION_STATUS", permissionGranted);
        startService(serviceIntent);
    }

    void updateLocationInFirebase()
    {
        if (Constants.CurrentLocation != null)
        {
            user = firebaseAuth.getCurrentUser();

            geoPoint = new GeoPoint(Constants.CurrentLocation.latitude, Constants.CurrentLocation.longitude);
            Log.d("FirebaseUser","user ID: "+user.getUid()+" GeoPoint: "+geoPoint.getLatitude()+" "+geoPoint.getLongitude());
            hashMap.put("MyLocation",geoPoint);
            documentReference = firestore.collection("RESPONDER LIST").document(user.getUid());
            documentReference.update("MyLocation",geoPoint).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d("MapsActivity", "Location updated to firestore");
                    //Toast.makeText(getBaseContext(), "Location updated to firestore", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    void moveToUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(thisIsIntentCall)
        {
            StringBuilder toDisplay = new StringBuilder();
            toDisplay.append("NEW EMERGENCY REQUEST!!\n");
            toDisplay.append("VICTIM NAME: ");
            toDisplay.append(name);
            info.setTextColor(this.getResources().getColor(R.color.DarkRed));
            info.setTypeface(null, Typeface.BOLD);
            info.setText(toDisplay);
            String[] temp = stringLocation.split(",");
            victimLocation = new LatLng(Double.parseDouble(temp[0]),Double.parseDouble(temp[1]));
            //mMap.addMarker(new MarkerOptions().position(victimLocation).title("Victim: "+name));
            Marker victimMarker = mMap.addMarker(new MarkerOptions().position(victimLocation).title("Victim: "+name));

            mMap.addCircle(new CircleOptions()
                    .center(victimLocation)
                    .radius(50)
                    .strokeColor(Color.RED)
                    .fillColor(Color.BLUE));

            LinearLayout layout =(LinearLayout)findViewById(R.id.mapLayout);
            layout.setBackgroundResource(R.drawable.customborder_red);

//            LatLngBounds.Builder builder = new LatLngBounds.Builder();
//            builder.include(Constants.CurrentLocation);
//            builder.include(victimMarker.getPosition());
//            LatLngBounds bounds = builder.build();
//            int padding = 0; // offset from edges of the map in pixels
//            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//            mMap.animateCamera(cu);

            navigate.setVisibility(Button.VISIBLE);
        }
        else
        {
            LinearLayout layout =(LinearLayout)findViewById(R.id.mapLayout);
            layout.setBackgroundResource(R.drawable.customborderblue);
        }
        mfusedlocationclient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if(location!=null) {
                    Log.d(TAG,"INSIDE FUSED: "+ "" + location.getLatitude() + " " + location.getLongitude());
                    Log.d(TAG,"BEARING : "+ "" + location.getBearing());
                    Log.d(TAG,"ACCURACY : "+ "" + location.getAccuracy());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),16));
//                    mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude()))
//                            .title("My Location")
//                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//                    Toast.makeText(getBaseContext(), "Your Location: \nlat:" + location.getLatitude() + " Lng:" + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    Constants.CurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    updateLocationInFirebase();
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                }
            }
        });
    }

    public void startNavigation(View view) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+stringLocation);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
