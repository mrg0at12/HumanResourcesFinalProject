package com.example.humanresourcesfinalproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myUserRef, myAdminRef;
    private String email2;
    private String pass2;

    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedpreferences=getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);



        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myUserRef = database.getReference("Users");  // Access User table
        myAdminRef = database.getReference("Admins");  // Access Admin table

        // Link UI elements
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);

        email2=sharedpreferences.getString("email","");
        pass2=sharedpreferences.getString("password","");

        etEmail.setText(email2);
        etPassword.setText(pass2);

        Button goBackBtn = findViewById(R.id.goBackLoginBtn);
        goBackBtn.setOnClickListener(v -> {
            // Go back to StartPage
            Intent intent = new Intent(Login.this, StartPage.class);
            startActivity(intent);
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInput(email, password)) {
                loginUser(email, password);
            }
        });
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return false;
        }
        return true;
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("TAG", "signInWithEmail:success");

                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("email", email);
                        editor.putString("password", password);
                        editor.apply();


                        // Check if the user exists in the "Admins" table first
                        checkIfAdminExists(user.getUid());
                    } else {
                        Log.w("TAG", "signInWithEmail:failure", task.getException());
                        Toast.makeText(this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfAdminExists(String userId) {
        // Query the Admins table to see if the user exists
        myAdminRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // The user is found in the Admins table
                    Log.d("AdminCheck", "Admin user logged in.");
                    redirectToMainPage();
                } else {
                    // The user doesn't exist in the Admins table, check the Users table
                    checkIfUserExists(userId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AdminCheck", "Error checking admin existence", databaseError.toException());
                Toast.makeText(Login.this, "Error checking admin status. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfUserExists(String userId) {
        // Query the Users table to see if the user exists
        myUserRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // The user exists in the Users table (regular user)
                    Log.d("UserCheck", "Regular user logged in.");
                    redirectToMainPage();
                } else {
                    // User doesn't exist in both tables
                    Log.d("UserCheck", "User not found in either Users or Admins.");
                    Toast.makeText(Login.this, "User not found. Please check your credentials.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("UserCheck", "Error checking user existence", databaseError.toException());
                Toast.makeText(Login.this, "Error checking user status. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToMainPage() {
        // Both regular users and admins are redirected to the MainPage
        Intent goHome = new Intent(getApplicationContext(), MainPage.class);
        startActivity(goHome);
        finish();
    }
}
