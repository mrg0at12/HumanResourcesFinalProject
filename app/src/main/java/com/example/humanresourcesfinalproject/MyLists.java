package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.humanresourcesfinalproject.model.Admin;
import com.example.humanresourcesfinalproject.model.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyLists extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, adminsRef;

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_lists);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        adminsRef = FirebaseDatabase.getInstance().getReference("Admins");


        Button goBackBtn = findViewById(R.id.GoBackMyLists);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to StartPage
                Intent intent = new Intent(MyLists.this, MainPage.class);
                startActivity(intent);
                finish(); // Finish the current activity (LoginActivity)
            }
        });



        Button btnCourseComprhensive = findViewById(R.id.btnEntireSchool);
        btnCourseComprhensive.setOnClickListener(v -> checkUserCourses());


        Button btnInstroucturs=findViewById(R.id.btnInstroucturs);
        btnInstroucturs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to StartPage
                Intent intent = new Intent(MyLists.this, SchoolInst.class);
                startActivity(intent);
                finish(); // Finish the current activity (LoginActivity)
            }
        });

        Button btnSchoolHealth=findViewById(R.id.btnHealthReportSchool);
        btnSchoolHealth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to StartPage
                Intent intent = new Intent(MyLists.this, SchoolHealth.class);
                startActivity(intent);
                finish(); // Finish the current activity (LoginActivity)
            }
        });

        Button btnEntSchool=findViewById(R.id.btnEntireSchool2);
        btnEntSchool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to StartPage
                Intent intent = new Intent(MyLists.this, SchoolComp.class);
                startActivity(intent);
                finish(); // Finish the current activity (LoginActivity)
            }
        });
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_course_comprehensive) {
            Intent intent = new Intent(this, CourseCompList.class);
            startActivity(intent);
        } else if (id == R.id.nav_course_health) {
            // Add navigation to Course Health Report
            // Implement the intent based on your app structure
            Toast.makeText(this, "Course Health Report selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_CourseInst) {
            // Add navigation to Buses division
            // Implement the intent based on your app structure
            Toast.makeText(this, "Division into buses selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_school_comprehensive) {
            Intent intent = new Intent(this, SchoolComp.class);
            startActivity(intent);
        } else if (id == R.id.nav_instructors) {
            Intent intent = new Intent(this, SchoolInst.class);
            startActivity(intent);
        } else if (id == R.id.nav_school_health) {
            Intent intent = new Intent(this, SchoolHealth.class);
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void checkUserCourses() {
        String userId = mAuth.getCurrentUser().getUid();

        // First, check in Users table
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    handleCourseCheck(user);
                } else {
                    // If not found in Users, check in Admins table
                    adminsRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Admin admin = snapshot.getValue(Admin.class);
                                handleCourseCheck(admin);
                            } else {
                                Toast.makeText(MyLists.this, "User not found in database", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(MyLists.this, "Error fetching admin data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MyLists.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleCourseCheck(User user) {
        if (user != null && user.getEnrolledCourses() != null) {
            int courseCount = user.getEnrolledCourses().size();

            if (courseCount >= 2) {
                // Redirect to ChooseCourse if user has 2 or more courses
                Intent intent = new Intent(MyLists.this, ChooseCourse.class);
                startActivity(intent);
            } else {
                // Redirect to CourseCompList otherwise
                Intent intent = new Intent(MyLists.this, CourseCompList.class);
                startActivity(intent);
            }
        } else {
            Toast.makeText(MyLists.this, "No courses found.", Toast.LENGTH_SHORT).show();
        }
    }

}