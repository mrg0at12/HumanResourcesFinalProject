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

import com.example.humanresourcesfinalproject.model.Admin;
import com.example.humanresourcesfinalproject.model.ClickHandlerUtil;
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
import java.util.NavigableMap;

public class SchoolComp extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ListView listView;
    private UserAdapter userAdapter;
    private ArrayList<User> schoolMembersList;
    private DatabaseReference usersRef;
    private String currentUserSchool = "";
    private FirebaseAuth auth;
    private SearchView searchView;
    private DrawerLayout drawerLayout;
    private boolean isCurrentUserAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_school_comp);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupNavigation();
        setupSearchView();
        setupButtonListeners();

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        checkIfUserIsAdmin();
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);

        listView = findViewById(R.id.schoolListView);
        searchView = findViewById(R.id.SvSchoolComp);
        schoolMembersList = new ArrayList<>();
        userAdapter = new UserAdapter(this, 0, schoolMembersList);

        // Set up the interaction listener
        userAdapter.setOnUserInteractionListener(new UserAdapter.OnUserInteractionListener() {
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(SchoolComp.this, UserInfo.class);
                intent.putExtra("userId", user.getId());
                startActivity(intent);
            }

            @Override
            public void onUserLongClick(User user) {
                // Handle long click if needed
            }
        });

        listView.setAdapter(userAdapter);
    }

    private void setupNavigation() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupSearchView() {
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
    }

    private void setupButtonListeners() {
        Button goBackBtn = findViewById(R.id.GoBackCompSchool);
        goBackBtn.setOnClickListener(v -> {
            startActivity(new Intent(SchoolComp.this, MyLists.class));
            finish();
        });
    }

    private void checkIfUserIsAdmin() {
        String userId = auth.getCurrentUser().getUid();
        // First check if the user is in the regular Users table
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getSchool() != null) {
                        currentUserSchool = user.getSchool();
                        fetchSchoolMembers();
                    } else {
                        // Check if they're in the Admin table instead
                        checkAdminTable(userId);
                    }
                } else {
                    // If not in Users table, check Admin table
                    checkAdminTable(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SchoolComp.this, "Failed to fetch user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAdminTable(String userId) {
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admins").child(userId);
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Admin admin = snapshot.getValue(Admin.class);
                    isCurrentUserAdmin = true;
                    if (admin != null && admin.getSchool() != null) {
                        currentUserSchool = admin.getSchool();
                        fetchSchoolMembers(); // Admin will see their school's members
                    } else {
                        Toast.makeText(SchoolComp.this, "Admin school not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SchoolComp.this, "User data not found in any table", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SchoolComp.this, "Failed to fetch admin data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSchoolMembers() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                schoolMembersList.clear();
                ArrayList<User> tempList = new ArrayList<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (isValidSchoolMember(user)) {
                        tempList.add(user);
                    }
                }

                // Now verify each user exists in main Users table
                verifyUsersInMainTable(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SchoolComp.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidSchoolMember(User user) {
        // Basic validation of user data
        if (user == null) return false;

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

        // Verify the user belongs to the current user's school
        return user.getSchool() != null && user.getSchool().equals(currentUserSchool);
    }

    private void verifyUsersInMainTable(ArrayList<User> tempList) {
        ArrayList<User> verifiedUsers = new ArrayList<>();
        final int[] counter = {0};

        if (tempList.isEmpty()) {
            updateUI(verifiedUsers);
            return;
        }

        for (User user : tempList) {
            usersRef.child(user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User mainUser = snapshot.getValue(User.class);
                        if (isValidSchoolMember(mainUser)) {
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
            schoolMembersList.clear();
            schoolMembersList.addAll(verifiedUsers);
            userAdapter.updateList(schoolMembersList);

            if (schoolMembersList.isEmpty()) {
                Toast.makeText(SchoolComp.this,
                        "No valid members found in " + (isCurrentUserAdmin ? "admin" : "your") + " school",
                        Toast.LENGTH_SHORT).show();
            }
        });
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
            // Already in this activity
        } else if (id == R.id.nav_instructors) {
            startActivity(new Intent(this, SchoolInst.class));
        } else if (id == R.id.nav_school_health) {
            startActivity(new Intent(this, SchoolHealth.class));
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
}