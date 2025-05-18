package com.example.humanresourcesfinalproject;

import static android.content.ContentValues.TAG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.humanresourcesfinalproject.model.Admin;
import com.example.humanresourcesfinalproject.model.Course;
import com.example.humanresourcesfinalproject.model.Enroll;
import com.example.humanresourcesfinalproject.model.NotificationReceiver;
import com.example.humanresourcesfinalproject.model.User;
import com.example.humanresourcesfinalproject.model.courseAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChooseYourCourse extends AppCompatActivity {



    private Button goBack;
    private ListView lvCourses;
    private DatabaseReference enrollUsersReference, userReference;
    private FirebaseUser currentUser;
    private User user = null;
    private boolean isAdmin = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choose_your_course);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        initializeViews();
        setupFirebaseReferences();
        loadUserData();
        getCoursesFromFirebase();
        setupButtonListeners();
    }

    private void initializeViews() {
        goBack = findViewById(R.id.btnCancel);
        lvCourses = findViewById(R.id.courseListView);
    }

    private void setupFirebaseReferences() {
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            enrollUsersReference = FirebaseDatabase.getInstance().getReference("EnrollForUsers2").child(currentUser.getUid());
        }
    }

    private void loadUserData() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            userReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Check for admin first
                        if (snapshot.hasChild("isAdmin")) {
                            Boolean adminStatus = snapshot.child("isAdmin").getValue(Boolean.class);
                            if (adminStatus != null && adminStatus) {
                                user = snapshot.getValue(Admin.class);
                                isAdmin = true;
                                return;
                            }
                        }
                        // If not admin, load as regular user
                        user = snapshot.getValue(User.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                    Toast.makeText(ChooseYourCourse.this, "Database error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getCoursesFromFirebase() {
        if (enrollUsersReference == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        enrollUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Course> courses = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Course course = snapshot.getValue(Course.class);
                    if (course != null) {
                        course.setCourseId(snapshot.getKey()); // Ensure course has ID
                        courses.add(course);
                        Log.d(TAG, "Course loaded: " + course.toString());
                    }
                }

                courseAdapter<Course> adapter = new courseAdapter<>(ChooseYourCourse.this, 0, 0, courses);
                lvCourses.setAdapter(adapter);

                lvCourses.setOnItemClickListener((parent, view, position, id) -> {
                    if (user == null) {
                        Toast.makeText(ChooseYourCourse.this, "User info not loaded", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Course course = (Course) parent.getItemAtPosition(position);
                    if (course == null || course.getCourseId() == null) {
                        Toast.makeText(ChooseYourCourse.this, "Course information invalid", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    navigateToCourseList(course.getCourseId());
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load courses: " + databaseError.getMessage());
                Toast.makeText(ChooseYourCourse.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToCourseList(String courseId) {
        Intent intent = new Intent(ChooseYourCourse.this, CourseCompList.class);
        intent.putExtra("courseId", courseId);
        startActivity(intent);
    }

    private void setupButtonListeners() {
        goBack.setOnClickListener(v -> {
            startActivity(new Intent(ChooseYourCourse.this, MyLists.class));
            finish();
        });
    }

}