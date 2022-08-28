package com.example.demo1;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.demo1.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    List<MarkerOptions> markerOptionsList;
    ArrayList<LatLng> points;
    List<Double> accelerations;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.demo1.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        markerOptionsList = new ArrayList<>();
        points = new ArrayList<>();
        accelerations = new ArrayList<>();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        // show route of current logged in user
        if(getIntent().getExtras().getBoolean("usersRoute")){
            if(mAuth.getCurrentUser() != null) {
                databaseReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            points = new ArrayList<>();
                            accelerations = new ArrayList<>();
                            int i = 0;
                            // get every details object of the user
                            for (DataSnapshot routeDetails : snapshot.getChildren()) {
                                Details routeDet = routeDetails.getValue(Details.class);

                                if (routeDet != null) {
                                    double lat = routeDet.getLat();
                                    double lon = routeDet.getLon();
                                    double acceleration = routeDet.getAcceleration();
                                    float speed = routeDet.getSpeed();
                                    Timestamp timestamp = new Timestamp(routeDet.getTime());
                                    accelerations.add(acceleration);
                                    String snippet = String.format(Locale.ENGLISH, "Speed: %.2f kmh\nAcceleration: %.2f m/s²\nTimestamp: %s", speed * 3.6, acceleration, timestamp);
                                    points.add(new LatLng(lat, lon));
                                    // add color to the connection line between two markers depending on the acceleration value
                                    if (i >= 1) {
                                        if (accelerations.get(i) < 0) {
                                            mMap.addPolyline(new PolylineOptions().color(Color.RED).width(3).add(points.get(i - 1), points.get(i)));
                                        } else {
                                            mMap.addPolyline(new PolylineOptions().color(Color.BLUE).width(3).add(points.get(i - 1), points.get(i)));
                                        }
                                    }
                                    MarkerOptions markerOpt = new MarkerOptions().position(new LatLng(lat, lon)).snippet(snippet);
                                    markerOptionsList.add(markerOpt);
                                    mMap.addMarker(markerOpt);
                                    // customizing the layout of the information box of the markers
                                    mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                        @Override
                                        public View getInfoWindow(Marker marker) {
                                            return null;
                                        }

                                        @Override
                                        public View getInfoContents(Marker marker) {
                                            LinearLayout info = new LinearLayout(MapsActivity.this);
                                            info.setOrientation(LinearLayout.VERTICAL);

                                            TextView snippet = new TextView(MapsActivity.this);
                                            snippet.setTextColor(Color.BLACK);
                                            snippet.setText(marker.getSnippet());

                                            info.addView(snippet);
                                            return info;
                                        }
                                    });
                                    i++;
                                }
                            }
                            showAllMarkers();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
        //show the routes of all users in db
        else{
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int userCounter = 1;
                        // get every user
                        for (DataSnapshot user : snapshot.getChildren()) {
                            points = new ArrayList<>();
                            accelerations = new ArrayList<>();
                            int i = 0;
                            // get every users detail objects
                            for(DataSnapshot routeDetails : user.getChildren()) {
                                Details routeDet = routeDetails.getValue(Details.class);

                                if(routeDet != null){
                                    double lat = routeDet.getLat();
                                    double lon = routeDet.getLon();
                                    float speed = routeDet.getSpeed();
                                    double acceleration = routeDet.getAcceleration();
                                    Timestamp timestamp = new Timestamp(routeDet.getTime());
                                    accelerations.add(acceleration);
                                    String tittle = "User "+ userCounter;
                                    String snippet = String.format(Locale.ENGLISH,"Speed: %.2f kmh\nAcceleration: %.2f m/s²\nTimestamp: %s",speed*3.6,acceleration, timestamp);
                                    points.add(new LatLng(lat, lon));
                                    if(i >=1){
                                        if (accelerations.get(i) < 0) {
                                            mMap.addPolyline(new PolylineOptions().color(Color.RED).width(3).add(points.get(i-1), points.get(i)));
                                        } else {
                                            mMap.addPolyline(new PolylineOptions().color(Color.BLUE).width(3).add(points.get(i-1), points.get(i)));
                                        }
                                    }
                                    MarkerOptions markerOpt = new MarkerOptions().position(new LatLng(lat, lon)).title(tittle).snippet(snippet);
                                    markerOptionsList.add(markerOpt);
                                    mMap.addMarker(markerOpt);
                                    mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                        @Override
                                        public View getInfoWindow(Marker marker) {
                                            return null;
                                        }

                                        @Override
                                        public View getInfoContents(Marker marker) {
                                            LinearLayout info = new LinearLayout(MapsActivity.this);
                                            info.setOrientation(LinearLayout.VERTICAL);

                                            TextView title = new TextView(MapsActivity.this);
                                            title.setTextColor(Color.BLACK);
                                            title.setGravity(Gravity.CENTER);
                                            title.setTypeface(null, Typeface.BOLD);
                                            title.setText(marker.getTitle());

                                            TextView snippet = new TextView(MapsActivity.this);
                                            snippet.setTextColor(Color.DKGRAY);
                                            snippet.setText(marker.getSnippet());

                                            info.addView(title);
                                            info.addView(snippet);
                                            return info;
                                        }
                                    });
                                    i++;
                                }
                            }
                            userCounter++;
                        }
                        showAllMarkers();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    // fix camera position in order to display all the markers in the screen
    private void showAllMarkers(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(MarkerOptions mo : markerOptionsList){
            builder.include(mo.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.4);

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,width,height,padding);
        mMap.animateCamera(cu);

    }

}