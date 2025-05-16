package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private static final String TAG = "CourseHealth";

    private ListView lvUser;
    private TextView emptyView;
    private ArrayList<User> allUsers = new ArrayList<>();
    private ArrayList<User> healthUsers = new ArrayList<>(); // Users with health problems
    private ArrayList<User> filteredUsers = new ArrayList<>(); // Filtered users with health problems
    private SearchView searchView;

    private Intent takeit;
    private String courseId = null;

    private FirebaseDatabase database;
    private DatabaseReference myUserRefCourses,usersReference;
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
        initializeViews();
        setupNavigation();
        setupSearchView();
        setupButtonListeners();

        database = FirebaseDatabase.getInstance();
        courseId = getIntent().getStringExtra("courseId");

        usersReference = database.getReference("Users");

        if (courseId != null) {
            loadUsersWithHealthProblems();
        } else {
            Toast.makeText(this, "No course ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }

    }
    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        lvUser = findViewById(R.id.lvCourseHealth);
        emptyView = findViewById(R.id.emptyView);
        searchView = findViewById(R.id.SvCourseHealth);

        if (lvUser == null) {
            Toast.makeText(this, "ListView not found!", Toast.LENGTH_SHORT).show();
            finish();
        }
        lvUser.setEmptyView(emptyView);
    }

    private void setupNavigation() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, findViewById(R.id.toolbar),
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupSearchView() {
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
    }

    private void setupButtonListeners() {
        Button goBackBtn = findViewById(R.id.GoBackCourseHealth);
        goBackBtn.setOnClickListener(v -> {
            startActivity(new Intent(CourseHealth.this, MyLists.class));
            finish();
        });
    }

    private void loadUsersWithHealthProblems() {
        myUserRefCourses = database.getReference("EnrollCourses2").child(courseId);
        healthUsers.clear();

        myUserRefCourses.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                healthUsers.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (isValidUserWithHealthProblems(user)) {
                        checkUserInMainTable(user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load users: " + error.getMessage());
                Toast.makeText(CourseHealth.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserInMainTable(User user) {
        if (user == null || user.getId() == null) return;

        usersReference.child(user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // User exists in main Users table
                    User mainUser = snapshot.getValue(User.class);
                    if (mainUser != null && isValidUserWithHealthProblems(mainUser)) {
                        healthUsers.add(mainUser);
                        updateUI();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to check user in main table: " + error.getMessage());
            }
        });
    }

    private void updateUI() {
        runOnUiThread(() -> {
            if (healthUsers.isEmpty()) {
                emptyView.setText("No students with health problems found");
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
                userAdapter = new UserAdapter(CourseHealth.this, 0, healthUsers);
                lvUser.setAdapter(userAdapter);

                ClickHandlerUtil.setupListViewClicks(lvUser, new ClickHandlerUtil.ClickCallbacks() {
                    @Override
                    public void onShortClick(int position) {
                        User selectedUser = userAdapter.getItem(position);
                        if (selectedUser != null && selectedUser.getId() != null) {
                            Intent intent = new Intent(CourseHealth.this, UserInfo.class);
                            intent.putExtra("userId", selectedUser.getId());
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onLongClick(int position) {
                        // Handle long click if needed
                    }
                });
            }
        });
    }

    private boolean isValidUserWithHealthProblems(User user) {
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
        return hasHealthProblems(user);
    }

    private boolean isAdminUser(User user) {
        return (user.getFname() == null && user.getLname() == null) ||
                (user.getFname() != null && user.getFname().equals("null") &&
                        user.getLname() != null && user.getLname().equals("null"));
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
            // Already in this activity
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