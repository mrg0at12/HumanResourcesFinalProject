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
    private DatabaseReference databaseReference;
    private String currentUserSchool = "";
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
        listView = findViewById(R.id.healthListView);
        healthIssueList = new ArrayList<>();
        userAdapter = new UserAdapter(this, 0, healthIssueList);

        // Set up the click listener for user items
        userAdapter.setOnUserInteractionListener(new UserAdapter.OnUserInteractionListener() {
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(SchoolHealth.this, UserInfo.class);
                intent.putExtra("userId", user.getId());
                startActivity(intent);
            }

            @Override
            public void onUserLongClick(User user) {
                // Handle long click if needed
            }
        });

        listView.setAdapter(userAdapter);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                userAdapter.getFilter().filter(newText);
                return false;
            }
        });

        Button goBackBtn = findViewById(R.id.GoBackHealthSchool);
        goBackBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SchoolHealth.this, MyLists.class);
            startActivity(intent);
            finish();
        });

        fetchCurrentUserSchool();
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_course_comprehensive) {
            startActivity(new Intent(this, ChooseYourCourse.class));
        } else if (id == R.id.nav_course_health) {
            startActivity(new Intent(this, ChooseYourCourseHealth.class));
        } else if (id == R.id.nav_CourseInst) {
            startActivity(new Intent(this, ChooseYourCourseInstructors.class));
        } else if (id == R.id.nav_school_comprehensive) {
            startActivity(new Intent(this, SchoolComp.class));
        } else if (id == R.id.nav_instructors) {
            startActivity(new Intent(this, SchoolInst.class));
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
                ArrayList<User> tempList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (isValidUserWithHealthIssues(user)) {
                        tempList.add(user);
                    }
                }

                // Now verify each user exists in main Users table
                verifyUsersInMainTable(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SchoolHealth.this, "Failed to load students.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidUserWithHealthIssues(User user) {
        if (user == null) return false;
        if (isAdminUser(user)) return false;
        if (user.getId() == null || user.getId().isEmpty() ||
                user.getFname() == null || user.getFname().isEmpty() ||
                user.getLname() == null || user.getLname().isEmpty() ||
                user.getPhone() == null || user.getPhone().isEmpty() ||
                user.getKidId() == null || user.getKidId().isEmpty()) {
            return false;
        }
        if ("null".equalsIgnoreCase(user.getFname()) || "null".equalsIgnoreCase(user.getLname())) {
            return false;
        }
        if (user.getSchool() == null || !user.getSchool().equals(currentUserSchool)) {
            return false;
        }
        String healthProblems = user.getHealthProblems();
        return healthProblems != null &&
                !healthProblems.equalsIgnoreCase("None") &&
                !healthProblems.trim().isEmpty();
    }

    private boolean isAdminUser(User user) {
        return (user.getFname() == null && user.getLname() == null) ||
                (user.getFname() != null && user.getFname().equals("null") &&
                        user.getLname() != null && user.getLname().equals("null"));
    }

    private void verifyUsersInMainTable(ArrayList<User> tempList) {
        ArrayList<User> verifiedUsers = new ArrayList<>();
        final int[] counter = {0};

        if (tempList.isEmpty()) {
            updateUI(verifiedUsers);
            return;
        }

        for (User user : tempList) {
            databaseReference.child(user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User mainUser = snapshot.getValue(User.class);
                        if (isValidUserWithHealthIssues(mainUser)) {
                            verifiedUsers.add(mainUser);
                        }
                    }

                    counter[0]++;
                    if (counter[0] == tempList.size()) {
                        updateUI(verifiedUsers);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    counter[0]++;
                    if (counter[0] == tempList.size()) {
                        updateUI(verifiedUsers);
                    }
                }
            });
        }
    }

    private void updateUI(ArrayList<User> verifiedUsers) {
        runOnUiThread(() -> {
            healthIssueList.clear();
            healthIssueList.addAll(verifiedUsers);
            userAdapter.updateList(healthIssueList);

            if (healthIssueList.isEmpty()) {
                Toast.makeText(SchoolHealth.this,
                        "No students with valid health issues found in your school",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}