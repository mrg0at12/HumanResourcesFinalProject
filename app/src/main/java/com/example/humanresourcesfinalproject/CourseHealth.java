package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
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

import com.example.humanresourcesfinalproject.model.ClickHandlerUtil;
import com.example.humanresourcesfinalproject.model.User;
import com.example.humanresourcesfinalproject.model.UserAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CourseHealth extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private UserAdapter userAdapter;
    private ListView lvUser;
    private TextView emptyView;
    private ArrayList<User> allUsers = new ArrayList<>();
    private ArrayList<User> healthUsers = new ArrayList<>(); // Users with health problems
    private ArrayList<User> filteredUsers = new ArrayList<>(); // Filtered users with health problems
    private SearchView searchView;

    private Intent takeit;
    private String courseId = null;

    private FirebaseDatabase database;
    private DatabaseReference myUserRefCourses;
    private DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_health);
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

        // Initialize ListView and SearchView
        lvUser = findViewById(R.id.lvCourseHealth);
        emptyView = findViewById(R.id.emptyView);
        searchView = findViewById(R.id.SvCourseHealth);

        if (lvUser == null) {
            Toast.makeText(this, "ListView not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        lvUser.setEmptyView(emptyView);

        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (userAdapter != null) {
                    userAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });

        Button goBackBtn = findViewById(R.id.GoBackCourseHealth);
        goBackBtn.setOnClickListener(v -> {
            Intent intent = new Intent(CourseHealth.this, MyLists.class);
            startActivity(intent);
            finish();
        });

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        courseId = getIntent().getStringExtra("courseId");

        if (courseId != null) {
            loadUsersFromFirebase();
        } else {
            Toast.makeText(this, "No course ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void loadUsersFromFirebase() {
        myUserRefCourses = database.getReference("EnrollCourses2").child(courseId);
        allUsers.clear();
        healthUsers.clear();

        myUserRefCourses.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot i : snapshot.getChildren()) {
                    User user = i.getValue(User.class);
                    if (user != null) {
                        allUsers.add(user);
                        if (hasHealthProblems(user)) {
                            healthUsers.add(user);
                        }
                    }
                }

                if (healthUsers.isEmpty()) {
                    emptyView.setText("No users with health problems found");
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    userAdapter = new UserAdapter(CourseHealth.this, 0, healthUsers);
                    lvUser.setAdapter(userAdapter);

                    // Set up click listeners AFTER the adapter is set
                    ClickHandlerUtil.setupListViewClicks(lvUser, new ClickHandlerUtil.ClickCallbacks() {
                        @Override
                        public void onShortClick(int position) {
                            User selectedUser = userAdapter.getItem(position);
                            if (selectedUser != null) {
                                Intent intent = new Intent(CourseHealth.this, UserInfo.class);
                                intent.putExtra("userId", selectedUser.getId());
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onLongClick(int position) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CourseHealth.this, "Failed to load users: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean hasHealthProblems(User user) {
        return user.getHealthProblems() != null &&
                !user.getHealthProblems().isEmpty() &&
                !user.getHealthProblems().equalsIgnoreCase("none");
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
        } else if (id == R.id.nav_school_health) {
            startActivity(new Intent(this, SchoolHealth.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}