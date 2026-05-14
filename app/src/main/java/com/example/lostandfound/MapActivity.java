package com.example.lostandfound;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 200;

    private GoogleMap  googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseHelper db;

    private SeekBar  seekBarRadius;
    private TextView tvRadius;
    private Button   btnApplyRadius;

    // Current user location (may be null if permission denied)
    private Location userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        db = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        seekBarRadius  = findViewById(R.id.seekBarRadius);
        tvRadius       = findViewById(R.id.tvRadius);
        btnApplyRadius = findViewById(R.id.btnApplyRadius);

        // SeekBar: 1–50 km
        seekBarRadius.setMax(49); // 0-indexed, represents 1-50 km
        seekBarRadius.setProgress(9); // default 10 km
        tvRadius.setText("Radius: 10 km");

        seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int km = progress + 1;
                tvRadius.setText("Radius: " + km + " km");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnApplyRadius.setOnClickListener(v -> plotMarkers());

        // Initialise map fragment
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // Try to get user location
        requestUserLocation();
    }
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        // Plot all markers initially (no radius filter yet)
        plotMarkers();
    }
    private void plotMarkers() {
        if (googleMap == null) return;
        googleMap.clear();

        List<Item> items = db.getAllItemsWithLocation();
        int radiusKm = seekBarRadius.getProgress() + 1;
        boolean applyFilter = (userLocation != null);
        int shown = 0;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasMarkers = false;

        for (Item item : items) {
            double lat = item.getLatitude();
            double lng = item.getLongitude();

            // Radius filter — only skip if we have a user location to compare to
            if (applyFilter) {
                float[] results = new float[1];
                Location.distanceBetween(
                        userLocation.getLatitude(), userLocation.getLongitude(),
                        lat, lng, results);
                float distanceKm = results[0] / 1000f;
                if (distanceKm > radiusKm) continue; // outside radius — skip
            }

            LatLng position = new LatLng(lat, lng);
            float markerColor = item.getPostType().equals("Lost")
                    ? BitmapDescriptorFactory.HUE_RED
                    : BitmapDescriptorFactory.HUE_GREEN;

            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title("[" + item.getPostType() + "] " + item.getCategory())
                    .snippet(item.getName() + " — " + item.getLocation())
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

            boundsBuilder.include(position);
            hasMarkers = true;
            shown++;
        }

        if (hasMarkers) {
            // Move camera to fit all visible markers
            try {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(), 150));
            } catch (Exception e) {
                // Fallback if only 1 marker
                if (items.size() > 0) {
                    Item first = items.get(0);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(first.getLatitude(), first.getLongitude()), 12f));
                }
            }
        } else if (userLocation != null) {
            // No markers — zoom to user
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 12f));
        }

        String msg = applyFilter
                ? "Showing " + shown + " item(s) within " + radiusKm + " km"
                : "Showing all " + shown + " item(s) (no location for radius filter)";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    private void requestUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLocation = location;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestUserLocation();
            if (googleMap != null) {
                try {
                    googleMap.setMyLocationEnabled(true);
                } catch (SecurityException ignored) {}
            }
        }
    }
}