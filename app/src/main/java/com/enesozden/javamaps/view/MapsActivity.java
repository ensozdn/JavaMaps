package com.enesozden.javamaps.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import com.enesozden.javamaps.R;
import com.enesozden.javamaps.databinding.ActivityMapsBinding;
import com.enesozden.javamaps.model.Place;
import com.enesozden.javamaps.roomdb.PlaceDao;
import com.enesozden.javamaps.roomdb.PlaceDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    LocationManager locationManager;
    LocationListener locationListener;
    ActivityResultLauncher<String> permissionLauncher;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    Double selectedLatitude;
    Double selectedLongitude;
    Place placeFromMain;
    PlaceDatabase db;
    PlaceDao placeDao;
    SharedPreferences sharedPreferences;
    boolean trackBoolean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        registerLauncher();

        sharedPreferences = getSharedPreferences("com.enesozden.javamaps", MODE_PRIVATE);
        trackBoolean = false;

        selectedLatitude = 0.0;
        selectedLongitude = 0.0;

        binding.saveButton.setEnabled(false);

        db = Room.databaseBuilder(getApplicationContext(), PlaceDatabase.class, "Places").build();
        placeDao = db.placeDao();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if ("new".equals(info)) {
            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.GONE);

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationListener = location -> {
                trackBoolean = sharedPreferences.getBoolean("trackBoolean", false);
                if (!trackBoolean) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    sharedPreferences.edit().putBoolean("trackBoolean", true).apply();
                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Snackbar.make(binding.getRoot(), "Permission needed for location", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Give Permission", v -> permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION))
                            .show();
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                }
                mMap.setMyLocationEnabled(true);
            }

        } else if ("old".equals(info)) {
            placeFromMain = (Place) intent.getSerializableExtra("place");

            if (placeFromMain != null) {
                LatLng latLng = new LatLng(placeFromMain.latitude, placeFromMain.longitude);
                mMap.addMarker(new MarkerOptions().position(latLng).title(placeFromMain.name));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                binding.placeNameText.setText(placeFromMain.name);
                binding.saveButton.setVisibility(View.GONE);
                binding.deleteButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));
        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;
        binding.saveButton.setEnabled(true);
    }

    public void save(View view) {
        Place place = new Place(binding.placeNameText.getText().toString(), selectedLatitude, selectedLongitude);
        compositeDisposable.add(placeDao.insert(place)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse));
    }

    public void delete(View view) {
        if (placeFromMain != null) {
            compositeDisposable.add(placeDao.delete(placeFromMain)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse));
        }
    }

    private void handleResponse() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void registerLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), result -> {
                    if (result) {
                        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (lastLocation != null) {
                                LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                            }
                        }
                    } else {
                        Toast.makeText(MapsActivity.this, "Permission needed!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
