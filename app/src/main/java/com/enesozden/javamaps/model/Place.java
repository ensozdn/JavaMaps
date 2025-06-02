package com.enesozden.javamaps.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Place implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "latitude")
    public Double latitude;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    // Yeni constructor: açıklama dahil
    public Place(String name, String description, Double latitude, Double longitude) {
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Eski constructor (kullanılıyorsa bozulmasın diye bıraktım)
    public Place(String name, Double latitude, Double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = ""; // default boş bırak
    }

    // Boş constructor Room için
    public Place() {}

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @ColumnInfo(name = "image_uri")
    public String imageUri;
}