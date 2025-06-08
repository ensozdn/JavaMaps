package com.enesozden.javamaps.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import com.enesozden.javamaps.R;
import com.enesozden.javamaps.adapter.PlaceAdapter;
import com.enesozden.javamaps.databinding.ActivityMainBinding;
import com.enesozden.javamaps.model.Place;
import com.enesozden.javamaps.roomdb.PlaceDao;
import com.enesozden.javamaps.roomdb.PlaceDatabase;

import java.util.List;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    PlaceDatabase db;
    PlaceDao placeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        db = Room.databaseBuilder(getApplicationContext(),
                        PlaceDatabase.class, "Places")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        placeDao = db.placeDao();

        // 🔥 VERİYİ EKLE VE BEKLE

        // 🔥 VERİYİ AL VE ADAPTER’E GÖNDER
        List<Place> placeList = placeDao.getAllDirect();
        handleResponse(placeList);

        // Reklam başlatılıyor
        MobileAds.initialize(this, initializationStatus -> {});

// XML'de tanımladığınız AdView bileşeni buraya bağlanır
        AdView adView = findViewById(R.id.adView);

// Reklam isteği oluşturulur
        AdRequest adRequest = new AdRequest.Builder().build();

// Reklam yüklenir
        adView.loadAd(adRequest);

    }

    private void handleResponse(List<Place> placeList) {
        Log.e("ADAPTER", "Veri sayısı: " + placeList.size());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 🔥 TIKLAMA LİSTENER EKLENDİ
        PlaceAdapter placeAdapter = new PlaceAdapter(placeList, place -> {
            Intent intent = new Intent(MainActivity.this, PlaceDetailActivity.class);
            intent.putExtra("place", place);
            startActivity(intent);
        });


        binding.recyclerView.setAdapter(placeAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.travel_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_place) {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("info", "new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (placeDao != null) {
            List<Place> updatedList = placeDao.getAllDirect();
            handleResponse(updatedList); // 🔁 Liste güncelleniyor
        }
    }
}
