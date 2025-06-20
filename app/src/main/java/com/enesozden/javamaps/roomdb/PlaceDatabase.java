package com.enesozden.javamaps.roomdb;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.enesozden.javamaps.model.Place;

@Database(entities = {Place.class},version = 3)
public abstract class PlaceDatabase extends RoomDatabase {
    public abstract PlaceDao placeDao();
}
// ✅ Eklendi