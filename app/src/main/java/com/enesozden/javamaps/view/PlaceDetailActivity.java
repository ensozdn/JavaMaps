package com.enesozden.javamaps.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.enesozden.javamaps.R;
import com.enesozden.javamaps.model.Place;
import com.enesozden.javamaps.roomdb.PlaceDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class PlaceDetailActivity extends AppCompatActivity {

    TextView textViewTitle, textViewDescription;
    Button buttonDelete, buttonSelectImage;
    ImageView imageViewSelected;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Place selectedPlace;

    private static final int REQUEST_CODE_READ_MEDIA_IMAGES = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewDescription = findViewById(R.id.textViewDescription);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonSelectImage = findViewById(R.id.selectImageButton);
        imageViewSelected = findViewById(R.id.imageView);

        // 📌 Android 13+ izin kontrolü
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_CODE_READ_MEDIA_IMAGES);
            }
        }


        // 📌 Launcher tanımı
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri originalUri = result.getData().getData();

                        if (originalUri != null) {
                            try (InputStream inputStream = getContentResolver().openInputStream(originalUri)) {

                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                String filename = "image_" + System.currentTimeMillis() + ".jpg";
                                File file = new File(getFilesDir(), filename);

                                try (FileOutputStream fos = new FileOutputStream(file)) {
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                }

                                Uri savedUri = Uri.fromFile(file);
                                imageViewSelected.setImageURI(savedUri);

                                if (selectedPlace != null) {
                                    selectedPlace.imageUri = savedUri.toString();
                                    PlaceDatabase db = Room.databaseBuilder(getApplicationContext(),
                                                    PlaceDatabase.class, "Places")
                                            .allowMainThreadQueries()
                                            .fallbackToDestructiveMigration()
                                            .build();

                                    db.placeDao().update(selectedPlace);
                                }

                            } catch (Exception e) {
                                Log.e("IMAGE_SAVE_ERROR", "Görsel işlenemedi: " + e.getMessage());
                            }
                        }
                    }
                }
        );

        buttonSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        selectedPlace = (Place) getIntent().getSerializableExtra("place");

        if (selectedPlace != null) {
            Log.e("PLACE_DETAIL", "Gelen ID: " + selectedPlace.id);
            textViewTitle.setText(selectedPlace.getName());
            textViewDescription.setText(selectedPlace.getDescription());

            if (selectedPlace.imageUri != null) {
                try {
                    Uri parsedUri = Uri.parse(selectedPlace.imageUri);
                    String path = parsedUri.getPath();

                    if (path != null) {
                        File imageFile = new File(path);
                        if (imageFile.exists()) {
                            Uri uri = Uri.fromFile(imageFile);
                            imageViewSelected.setImageURI(uri);
                            Log.e("LOAD_IMAGE", "Görsel yüklendi: " + uri.toString());
                        } else {
                            Log.e("LOAD_IMAGE_ERROR", "Görsel dosyası bulunamadı: " + path);
                        }
                    } else {
                        Log.e("LOAD_IMAGE_ERROR", "URI path null döndü.");
                    }

                } catch (Exception e) {
                    Log.e("LOAD_IMAGE_ERROR", "Görsel yüklenemedi: " + e.getMessage());
                }
            }

            buttonDelete.setOnClickListener(v -> {
                PlaceDatabase db = Room.databaseBuilder(getApplicationContext(),
                                PlaceDatabase.class, "Places")
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build();
                db.placeDao().deleteById(selectedPlace.id);
                finish();
            });
        }
    }

    // 📌 İzin sonucu geri dönüş kontrolü
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("PERMISSION", "İzin verildi");
            } else {
                Toast.makeText(this, "Görsel seçmek için izin gerekli!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
