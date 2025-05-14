package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
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

import com.example.humanresourcesfinalproject.model.User;
import com.example.humanresourcesfinalproject.model.UserAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SchoolHealth extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener{
    private ListView listView;
    private UserAdapter userAdapter;
    private ArrayList<User> healthIssueList;
    private ArrayList<User> filteredList;
    private DatabaseReference databaseReference;
    private String currentUserSchool = ""; // User's school
    private FirebaseAuth auth;
    private SearchView searchView;

    private DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_school_health);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up toolbar and drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        searchView = findViewById(R.id.SvSchoolHealth);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // Not needed for live filtering
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                userAdapter.getFilter().filter(newText);
                return false;
            }
        });

        Button goBackBtn = findViewById(R.id.GoBackHealthSchool);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SchoolHealth.this, MyLists.class);
                startActivity(intent);
                finish();
            }
        });

        listView = findViewById(R.id.healthListView);
        healthIssueList = new ArrayList<>();
        userAdapter = new UserAdapter(this, 0, healthIssueList);
        listView.setAdapter(userAdapter);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        fetchCurrentUserSchool();
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_course_comprehensive) {
            Intent intent = new Intent(this, CourseCompList.class);
            startActivity(intent);
        } else if (id == R.id.nav_course_health) {
            Toast.makeText(this, "Course Health Report selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_CourseInst) {
            Toast.makeText(this, "Course instructors and teachers selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_school_comprehensive) {
            Intent intent = new Intent(this, SchoolComp.class);
            startActivity(intent);
        } else if (id == R.id.nav_instructors) {
            Intent intent = new Intent(this, SchoolInst.class);
            startActivity(intent);
        } else if (id == R.id.nav_school_health) {
            // Already in this activity
            drawerLayout.closeDrawer(GravityCompat.START);
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
    private void fetchCurrentUserSchool() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        currentUserSchool = user.getSchool();
                        fetchUsersWithHealthIssues();
                    } else {
                        Toast.makeText(SchoolHealth.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SchoolHealth.this, "Failed to fetch user school", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchUsersWithHealthIssues() {
        if (currentUserSchool.isEmpty()) {
            Toast.makeText(this, "No school found for user", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                healthIssueList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user != null && user.getSchool().equals(currentUserSchool)) {
                        String healthProblems = user.getHealthProblems();
                        if (healthProblems != null && !healthProblems.equalsIgnoreCase("None") && !healthProblems.trim().isEmpty()) {
                            healthIssueList.add(user);
                        }
                    }
                }
                userAdapter.notifyDataSetChanged();
                if (healthIssueList.isEmpty()) {
                    Toast.makeText(SchoolHealth.this, "No students with health issues found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SchoolHealth.this, "Failed to load students.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}