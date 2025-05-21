package com.enesozden.javamaps.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.enesozden.javamaps.R;

public class LoginActivity extends AppCompatActivity {

    Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        buttonLogin = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Giriş yapıldıktan sonra HomeActivity'e geçiş
                Intent intent = new Intent(LoginActivity.this, MainActivity.class); // veya ileride HomeActivity
                startActivity(intent);
                finish(); // Login ekranına geri dönmeyi engelle
            }
        });
    }
}
