package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.humanresourcesfinalproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    EditText etFname, etLname, etPhone, etEmail, etPassword, etShirtSize, etKidId, etSchool, etParentName, etParentPhone, etSchoolYear, etHealthFund, etHealthProblems;
    Spinner spFoodType;
    ToggleButton tbIsTeacher, tbIsGuide;
    Button btnReg;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");
        sharedpreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

        // Link UI elements
        etFname = findViewById(R.id.etFname);
        etLname = findViewById(R.id.etLname);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Spinner spShirtSize = findViewById(R.id.spShirtSize);
        ArrayAdapter<CharSequence> shirtSizeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.shirt_sizes,
                android.R.layout.simple_spinner_item
        );
        shirtSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spShirtSize.setAdapter(shirtSizeAdapter);
        etKidId = findViewById(R.id.etKidId);
        etSchool = findViewById(R.id.etSchool);
        etParentName = findViewById(R.id.etParentName);
        etParentPhone = findViewById(R.id.etParentPhone);
        etSchoolYear = findViewById(R.id.etSchoolYear);
        etHealthFund = findViewById(R.id.etHealthFund);
        etHealthProblems = findViewById(R.id.etHealthProblems);
        Spinner spFoodType = findViewById(R.id.spFoodType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.food_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFoodType.setAdapter(adapter);
        tbIsTeacher = findViewById(R.id.tbIsTeacher);
        tbIsGuide = findViewById(R.id.tbIsGuide);
        btnReg = findViewById(R.id.btnReg);

        ArrayAdapter<CharSequence> foodTypeAdapter = ArrayAdapter.createFromResource(this, R.array.food_types, android.R.layout.simple_spinner_item);
        foodTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFoodType.setAdapter(foodTypeAdapter);


        Button goBackBtn = findViewById(R.id.goBackRegisterBtn);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to StartPage
                Intent intent = new Intent(Register.this, StartPage.class);
                startActivity(intent);
                finish(); // Finish the current activity (LoginActivity)
            }
        });


        btnReg.setOnClickListener(v -> {
            String fname = etFname.getText().toString().trim();
            String lname = etLname.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String shirtSize = spShirtSize.getSelectedItem().toString().trim();
            String kidId = etKidId.getText().toString().trim();
            String school = etSchool.getText().toString().trim();
            String parentName = etParentName.getText().toString().trim();
            String parentPhone = etParentPhone.getText().toString().trim();
            String schoolYear = etSchoolYear.getText().toString().trim();
            String healthFund = etHealthFund.getText().toString().trim();
            String healthProblems = etHealthProblems.getText().toString().trim();
            String foodType = spFoodType.getSelectedItem().toString();
            Boolean isTeacher = tbIsTeacher.isChecked();
            Boolean isGuide = tbIsGuide.isChecked();

            // Validate input fields before proceeding
            if (validateInput(fname, lname, phone, email, password, shirtSize, kidId, school, parentName, parentPhone, schoolYear, healthFund, foodType)) {
                // Create Firebase user
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser fireuser = mAuth.getCurrentUser();
                                // Create a User object with all fields
                                User newUser = new User(
                                        fireuser.getUid(), fname, lname, phone, email, password,
                                        shirtSize, kidId, school, parentName, parentPhone,
                                        schoolYear, healthFund, healthProblems, foodType,
                                        isTeacher, isGuide
                                );

                                // Save user to Firebase Realtime Database
                                myRef.child(fireuser.getUid()).setValue(newUser)
                                        .addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                // Save credentials in SharedPreferences
                                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                                editor.putString("email", email);
                                                editor.putString("password", password);
                                                editor.apply();

                                                Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getApplicationContext(), Login.class));
                                            } else {
                                                Log.w("TAG", "Database write failed", dbTask.getException());
                                                Toast.makeText(Register.this, "Database write failed: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            } else {
                                Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(Register.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });



    }

    private boolean validateInput(String fname, String lname, String phone, String email, String password, String shirtSize, String kidId, String school, String parentName, String parentPhone, String schoolYear, String healthFund, String foodType) {
        if (TextUtils.isEmpty(fname)) {
            etFname.setError("First name is required");
            return false;
        }
        if (TextUtils.isEmpty(lname)) {
            etLname.setError("Last name is required");
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return false;
        }
        if (TextUtils.isEmpty(shirtSize)) {
            Toast.makeText(this, "Please select a shirt size", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(kidId)) {
            etKidId.setError("Kid ID is required");
            return false;
        }
        if (TextUtils.isEmpty(school)) {
            etSchool.setError("School is required");
            return false;
        }
        if (TextUtils.isEmpty(parentName)) {
            etParentName.setError("Parent name is required");
            return false;
        }
        if (TextUtils.isEmpty(parentPhone)) {
            etParentPhone.setError("Parent phone is required");
            return false;
        }
        if (TextUtils.isEmpty(schoolYear)) {
            etSchoolYear.setError("School year is required");
            return false;
        }
        if (TextUtils.isEmpty(healthFund)) {
            etHealthFund.setError("Health fund is required");
            return false;
        }
        if (TextUtils.isEmpty(foodType)) {
            Toast.makeText(this, "Please select a food type", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

}
