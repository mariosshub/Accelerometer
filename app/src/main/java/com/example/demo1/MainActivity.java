package com.example.demo1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    TextView textViewUID, textViewSpeed, textViewAcceleration;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    Button button2, mapBtn1, mapBtn2;
    LocationManager locationManager;
    static final int REQ_LOC_CODE = 21;
    float prevSpeed = 0;
    long prevTime = 0;
    double prevLat = 0;
    double prevLong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        mAuth = FirebaseAuth.getInstance();
        textViewSpeed = findViewById(R.id.textViewSpeed);
        textViewAcceleration = findViewById(R.id.textViewAcceleration);
        textViewUID = findViewById(R.id.textViewUID);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(mAuth.getCurrentUser() != null)
            textViewUID.setText(mAuth.getCurrentUser().getEmail());

        mapBtn1 = findViewById(R.id.mapbtn);
        mapBtn2 = findViewById(R.id.mapbtn2);
        button2 = findViewById(R.id.button2);
        //check database if data exists and then enable buttons
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    mapBtn2.setEnabled(true);
                    if(snapshot.child(mAuth.getCurrentUser().getUid()).getChildren().iterator().hasNext()){
                        mapBtn1.setEnabled(true);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQ_LOC_CODE);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 50, MainActivity.this);
                }
                Toast.makeText(MainActivity.this,"collecting data..",Toast.LENGTH_LONG).show();
                // delete previous route
                databaseReference.child(mAuth.getCurrentUser().getUid()).setValue(null);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 50, MainActivity.this);
        }
    }
    //stop requesting location updates
    public void stop(View view){
        locationManager.removeUpdates(MainActivity.this);
        textViewSpeed.setText(R.string.speed);
        textViewAcceleration.setText(R.string.acceleration);
        Toast.makeText(this,"stopped collecting data",Toast.LENGTH_LONG).show();
    }

    public void logout(View view) {
        mAuth.signOut();
        finish();
    }

    public  void usersRouteBtn(View view){
        startActivity(new Intent(this,MapsActivity.class).putExtra("usersRoute",true));
    }
    public void allRoutesBtn(View view){
        startActivity(new Intent(this,MapsActivity.class).putExtra("usersRoute",false));
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // when entered the first time initialize previous values to current
        if(prevLong == 0 || prevLat == 0){
            prevLong = location.getLongitude();
            prevLat = location.getLatitude();
            prevTime = location.getTime();
            prevSpeed = location.getSpeed();
        }
        // check if users previous location is different from the current one
        if (prevLat != location.getLatitude() || prevLong != location.getLongitude()){

            float nCurrentSpeed = location.getSpeed();
            long nCurrentTime = location.getTime() / 1000;

            double acceleration = (nCurrentSpeed - prevSpeed) / (nCurrentTime - prevTime);

            textViewSpeed.setText(String.format(Locale.ENGLISH,"Speed: \n %.2f kmh", nCurrentSpeed * 3.6));
            if(acceleration < 0)
                textViewAcceleration.setText(String.format(Locale.ENGLISH,"Deceleration: \n %.2f m/s²", acceleration));
            else
                textViewAcceleration.setText(String.format(Locale.ENGLISH,"Acceleration: \n %.2f m/s²", acceleration));

            if(acceleration <0)
                textViewAcceleration.setBackgroundColor(Color.RED);
            else if(acceleration <=3)
                textViewAcceleration.setBackgroundColor(Color.GREEN);
            else if(acceleration <=8)
                textViewAcceleration.setBackgroundColor(Color.CYAN);
            else if(acceleration <=12)
                textViewAcceleration.setBackgroundColor(Color.BLUE);

            if(mAuth.getCurrentUser() != null){
                // save to database
                databaseReference.child(mAuth.getCurrentUser().getUid()).push()
                        .setValue(new Details(location.getLatitude(),location.getLongitude(),location.getSpeed(),location.getTime(),acceleration));

            }

            prevSpeed = location.getSpeed();
            prevTime = location.getTime() / 1000;
            prevLat = location.getLatitude();
            prevLong = location.getLongitude();
        }
    }
}