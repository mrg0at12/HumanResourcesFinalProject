package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StartPage extends AppCompatActivity {

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });




        Button startLoginBtn = findViewById(R.id.StartLoginBtn);
        Button startRegisterBtn = findViewById(R.id.StartRegisterBtn);
        Button startAboutBtn = findViewById(R.id.StartAboutBtn);





        startLoginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(StartPage.this, Login.class);
            startActivity(intent);
        });

        startRegisterBtn.setOnClickListener(v -> {
            Intent intent = new Intent(StartPage.this, Register.class);
            startActivity(intent);
        });

        startAboutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(StartPage.this, About.class);
            startActivity(intent);
        });
    }
}