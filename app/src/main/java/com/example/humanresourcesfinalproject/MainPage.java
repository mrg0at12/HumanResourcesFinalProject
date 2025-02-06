package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainPage extends AppCompatActivity {
    private Button changePasswordButton;
    private Button logOutButton;
    private Button reportsButton;
    private Button manageButton;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef, adminRef;

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

        mAuth = FirebaseAuth.getInstance();

        changePasswordButton = findViewById(R.id.ChangePassword);
        reportsButton = findViewById(R.id.Reports);
        logOutButton = findViewById(R.id.LogOut);
        manageButton = findViewById(R.id.Mange); // System Management button

        reportsButton.setEnabled(false);
        manageButton.setEnabled(false); // Disable buttons initially

        checkUserRole();

        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainPage.this, ResetPassword.class);
            startActivity(intent);
        });

        logOutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainPage.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void checkUserRole() {
        String userId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        adminRef = FirebaseDatabase.getInstance().getReference("Admins").child(userId);

        // Check if the user is a teacher
        userRef.child("isTeacher").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {
                    reportsButton.setEnabled(true);
                    reportsButton.setOnClickListener(v -> {
                        Intent intent = new Intent(MainPage.this, MyLists.class);
                        startActivity(intent);
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainPage.this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
            }
        });

        // Check if the user is an admin
        adminRef.child("isAdmin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {
                    manageButton.setEnabled(true);
                    manageButton.setOnClickListener(v -> {
                        Intent intent = new Intent(MainPage.this, SystemManagement.class);
                        startActivity(intent);
                    });

                    // Admins can also access the MyLists page
                    reportsButton.setEnabled(true);
                    reportsButton.setOnClickListener(v -> {
                        Intent intent = new Intent(MainPage.this, MyLists.class);
                        startActivity(intent);
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainPage.this, "Error checking admin status.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
