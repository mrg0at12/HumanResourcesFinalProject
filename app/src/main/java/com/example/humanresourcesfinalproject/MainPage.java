package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainPage extends AppCompatActivity {
    private Button changePasswordButton;
    private Button logOutButton;
    private Button reportsButton ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        changePasswordButton = findViewById(R.id.ChangePassword);
        reportsButton= findViewById(R.id.Reports);

        logOutButton = findViewById(R.id.LogOut);

        reportsButton.setOnClickListener(v -> {
            // Start the mylists activity
            Intent intent = new Intent(MainPage.this, MyLists.class);
            startActivity(intent);
        });


        // Set an OnClickListener to navigate to ResetPassword activity
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPage.this, ResetPassword.class);
                startActivity(intent);
            }
        });

        logOutButton.setOnClickListener(v -> {
            // Sign out from Firebase Auth
            FirebaseAuth.getInstance().signOut();

            // Navigate back to Login screen
            Intent intent = new Intent(MainPage.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            startActivity(intent);
            finish();
        });
    }
}