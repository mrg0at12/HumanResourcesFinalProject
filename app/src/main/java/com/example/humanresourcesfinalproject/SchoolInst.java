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

public class SchoolInst extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ListView listView;
    private UserAdapter userAdapter;
    private ArrayList<User> instructorList;
    private DatabaseReference usersRef;
    private String currentUserSchool = "";
    private FirebaseAuth auth;
    private SearchView searchView;
    private int pendingQueries = 0;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_inst);
        EdgeToEdge.enable(this);
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
        searchView = findViewById(R.id.SvSchoolInst);
        listView = findViewById(R.id.instructorListView);
        instructorList = new ArrayList<>();
        userAdapter = new UserAdapter(this, 0, instructorList);
        listView.setAdapter(userAdapter);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        fetchCurrentUserSchool();

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

        Button goBackBtn = findViewById(R.id.btnGoBackInst);
        goBackBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SchoolInst.this, MyLists.class);
            startActivity(intent);
            finish();
        });
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
            // Already in this activity
            drawerLayout.closeDrawer(GravityCompat.START);
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

    private void fetchCurrentUserSchool() {
        String userId = auth.getCurrentUser().getUid();
        if (userId != null) {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        //Toast.makeText(SchoolInst.this, user.getSchool(), Toast.LENGTH_LONG).show();
                        currentUserSchool = user.getSchool();
                        fetchInstructors();
                    } else {
                        Toast.makeText(SchoolInst.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SchoolInst.this, "Failed to fetch user school", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchInstructors() {
        if (currentUserSchool.isEmpty()) {
            Toast.makeText(this, "No school found for user", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingQueries = 1; // We are making one query (Users)

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user != null && user.getSchool().equals(currentUserSchool) &&
                            (Boolean.TRUE.equals(user.getIsTeacher()) || Boolean.TRUE.equals(user.getIsGuide()))) {

                        instructorList.add(user);
                        //Toast.makeText(SchoolInst.this, user.getFname(), Toast.LENGTH_LONG).show();
                    }
                }

                userAdapter = new UserAdapter(SchoolInst.this, 0, instructorList);
                listView.setAdapter(userAdapter);
                checkQueriesComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SchoolInst.this, "Failed to load instructors.", Toast.LENGTH_SHORT).show();
                checkQueriesComplete();
            }
        });
    }

    private void checkQueriesComplete() {
        pendingQueries--;
        if (pendingQueries == 0) {
            userAdapter.notifyDataSetChanged();
            if (instructorList.isEmpty()) {
                Toast.makeText(SchoolInst.this, "No instructors found.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}