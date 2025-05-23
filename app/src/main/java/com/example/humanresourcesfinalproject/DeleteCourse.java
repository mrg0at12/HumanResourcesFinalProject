package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

import com.example.humanresourcesfinalproject.model.Course;
import com.example.humanresourcesfinalproject.model.courseAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DeleteCourse extends AppCompatActivity {
    private ListView listView;
    private courseAdapter<Course> adapter;
    private List<Course> courses;
    private List<String> courseKeys; // Stores Firebase keys for deletion
    private DatabaseReference dbRef;


    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delete_course);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer setup
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_create_course) {
                    startActivity(new Intent(DeleteCourse.this, CreateCourse.class));
                } else if (id == R.id.nav_delete_course) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_manage_admins) {
                    startActivity(new Intent(DeleteCourse.this, ManageAdmins.class));
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        listView = findViewById(R.id.listView);
        dbRef = FirebaseDatabase.getInstance().getReference("courses"); // Updated to match your database node name
        courses = new ArrayList<>();
        courseKeys = new ArrayList<>();

        Button goBackBtn = findViewById(R.id.btnGoBackDelete);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to StartPage
                Intent intent = new Intent(DeleteCourse.this, SystemManagement.class);
                startActivity(intent);
                finish(); // Finish the current activity (LoginActivity)
            }
        });

        loadCourses();

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            deleteCourse(position);
            return true;
        });
    }

    private void loadCourses() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                courses.clear();
                courseKeys.clear();

                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    Course course = courseSnapshot.getValue(Course.class);
                    String key = courseSnapshot.getKey(); // Get the Firebase key

                    if (course != null) {
                        courses.add(course);
                        courseKeys.add(key);
                    }
                }

                // Use the custom courseAdapter
                adapter = new courseAdapter<>(
                        DeleteCourse.this,
                        R.layout.courserow,
                        R.id.tvCourseName,
                        courses
                );
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DeleteCourse.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCourse(int position) {
        String selectedCourseName = courses.get(position).getCourseName();

        // Delete from "courses" node
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean courseDeleted = false;

                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    Course course = courseSnapshot.getValue(Course.class);
                    if (course != null && course.getCourseName().contains(selectedCourseName)) {
                        courseSnapshot.getRef().removeValue(); // Remove from courses
                        courseDeleted = true;
                    }
                }

                if (courseDeleted) {
                    deleteCourseFromUsers(selectedCourseName);
                    deleteCourseFromAdmins(selectedCourseName);
                    Toast.makeText(DeleteCourse.this, "Course deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DeleteCourse.this, "Course not found", Toast.LENGTH_SHORT).show();
                }

                loadCourses(); // Refresh list
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DeleteCourse.this, "Failed to delete course", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCourseFromUsers(String courseName) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    DatabaseReference enrolledCoursesRef = userSnapshot.getRef().child("enrolledCourses");

                    enrolledCoursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot courseSnapshot) {
                            for (DataSnapshot enrolledCourse : courseSnapshot.getChildren()) {
                                String enrolledCourseName = enrolledCourse.getValue(String.class);
                                if (enrolledCourseName != null && enrolledCourseName.contains(courseName)) {
                                    enrolledCourse.getRef().removeValue(); // Remove from user
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {}
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void deleteCourseFromAdmins(String courseName) {
        DatabaseReference adminsRef = FirebaseDatabase.getInstance().getReference("Admins");

        adminsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot adminSnapshot : snapshot.getChildren()) {
                    DatabaseReference adminCoursesRef = adminSnapshot.getRef().child("adminCourses");

                    adminCoursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot courseSnapshot) {
                            for (DataSnapshot adminCourse : courseSnapshot.getChildren()) {
                                String adminCourseName = adminCourse.getValue(String.class);
                                if (adminCourseName != null && adminCourseName.contains(courseName)) {
                                    adminCourse.getRef().removeValue(); // Remove from admin
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {}
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}