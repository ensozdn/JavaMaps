package com.enesozden.javamaps.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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
                .build();

        placeDao = db.placeDao();

        // 🔥 VERİYİ EKLE VE BEKLE
        placeDao.insert(new Place("İlk kayıt", 0.0, 0.0)).blockingAwait();

        // 🔥 VERİYİ AL VE ADAPTER’E GÖNDER
        List<Place> placeList = placeDao.getAllDirect();
        handleResponse(placeList);
    }



    private void handleResponse(List<Place> placeList) {
        Log.e("ADAPTER", "Veri sayısı: " + placeList.size()); // 👈 Gözüküyor mu?
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        PlaceAdapter placeAdapter = new PlaceAdapter(placeList);
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
}
