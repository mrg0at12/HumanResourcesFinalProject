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
    private DatabaseReference usersRef, adminsRef;
    private String currentUserSchool = "";
    private FirebaseAuth auth;
    private int pendingQueries = 0;
    private SearchView searchView;
    private DrawerLayout drawerLayout;


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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        searchView=findViewById(R.id.SvSchoolComp);
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

        Button goBackBtn = findViewById(R.id.GoBackCompSchool);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to StartPage
                Intent intent = new Intent(SchoolComp.this, MyLists.class);
                startActivity(intent);
                finish(); // Finish the current activity (LoginActivity)
            }
        });

        listView = findViewById(R.id.schoolListView);
        schoolMembersList = new ArrayList<>();
       userAdapter = new UserAdapter(this, 0, schoolMembersList);
        listView.setAdapter(userAdapter);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        adminsRef = FirebaseDatabase.getInstance().getReference("Admins");

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
            // Already in this activity
            drawerLayout.closeDrawer(GravityCompat.START);
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


    private void fetchCurrentUserSchool() {
        String userId  = auth.getCurrentUser().getUid();
       if(userId!=null){

            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {

                        //Toast.makeText(SchoolComp.this, user.getSchool(), Toast.LENGTH_LONG).show();

                        currentUserSchool = user.getSchool();
                        fetchSchoolMembers();
                    } else {
                        Toast.makeText(SchoolComp.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SchoolComp.this, "Failed to fetch user school", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchSchoolMembers() {
        if (currentUserSchool.isEmpty()) {
            Toast.makeText(this, "No school found for user", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingQueries = 2; // We are making two queries (Users & Admins)

        fetchUsers();
        fetchAdmins();
    }

    private void fetchUsers() {


        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user != null && user.getSchool().equals(currentUserSchool)) {

                        schoolMembersList.add(user);

                        Toast.makeText(SchoolComp.this, user.getFname(), Toast.LENGTH_LONG).show();

                        //   String details = "👤 " + user.getFname() + " " + user.getLname() +
                      //          "\n📧 Email: " + user.getEmail() +
                      //          "\n📞 Phone: " + user.getPhone() +
                       //         "\n🏫 Role: " + getUserRole(user) +
                       //         "\n--------------------------";
                       // schoolMembersList.add(details);
                    }
                }
                userAdapter = new UserAdapter(SchoolComp.this, 0, schoolMembersList);
                listView.setAdapter(userAdapter);

                //checkQueriesComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SchoolComp.this, "Failed to load users.", Toast.LENGTH_SHORT).show();
                checkQueriesComplete();
            }
        });
    }

    private void fetchAdmins() {
        adminsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Admin admin = data.getValue(Admin.class);

                    if (admin != null && admin.getSchool().equals(currentUserSchool)) {
                        String details = "👑 ADMIN: " + admin.getFname() + " " + admin.getLname() +
                                "\n📧 Email: " + admin.getEmail() +
                                "\n📞 Phone: " + admin.getPhone() +
                                "\n🏫 School: " + admin.getSchool() +
                                "\n--------------------------";
                        schoolMembersList.add(admin);
                    }
                }
                checkQueriesComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SchoolComp.this, "Failed to load admins.", Toast.LENGTH_SHORT).show();
                checkQueriesComplete();
            }
        });
    }

    private String getUserRole(User user) {
        if (Boolean.TRUE.equals(user.getIsTeacher())) return "Teacher";
        if (Boolean.TRUE.equals(user.getIsGuide())) return "Guide";
        return "Student";
    }

    private void checkQueriesComplete() {
        pendingQueries--;
        if (pendingQueries == 0) {
            userAdapter.notifyDataSetChanged();
            if (schoolMembersList.isEmpty()) {
                Toast.makeText(SchoolComp.this, "No members found.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}