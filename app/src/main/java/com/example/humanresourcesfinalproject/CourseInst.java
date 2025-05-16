package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CourseInst extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    UserAdapter userAdapter;
    ListView lvUser;
    ArrayList<User> users = new ArrayList();
    private SearchView searchView;

    Intent takeit;
    String courseId = null;

    private FirebaseDatabase database;
    private DatabaseReference myUserRefCoures;
    private DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_inst);
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

        searchView = findViewById(R.id.SvCourseInst);
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
                return false;
            }
        });

        Button goBackBtn = findViewById(R.id.GoBackCourseInst);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to MyLists
                Intent intent = new Intent(CourseInst.this, MyLists.class);
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });

        lvUser = findViewById(R.id.lvCourseInst);
        database = FirebaseDatabase.getInstance();

        takeit = getIntent();
        courseId = takeit.getStringExtra("courseId");

        if (courseId != null) {
            myUserRefCoures = database.getReference("EnrollCourses2").child(courseId);
            users.clear();

            myUserRefCoures.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot i : snapshot.getChildren()) {
                        User user = i.getValue(User.class);
                        if (user != null && (user.getIsTeacher() != null && user.getIsTeacher() ||
                                user.getIsGuide() != null && user.getIsGuide())) {
                            users.add(user);
                        }
                    }

                    userAdapter = new UserAdapter(CourseInst.this, 0, users);
                    lvUser.setAdapter(userAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle errors
                }
            });
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_course_comprehensive) {
            Intent intent = new Intent(this, ChooseYourCourse.class);
            startActivity(intent);
        } else if (id == R.id.nav_course_health) {
            Intent intent = new Intent(this, ChooseYourCourseHealth.class);
            startActivity(intent);
        } else if (id == R.id.nav_CourseInst) {
            Intent intent = new Intent(this, ChooseYourCourseInstructors.class);
            startActivity(intent);
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
}