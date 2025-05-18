package com.example.humanresourcesfinalproject;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.humanresourcesfinalproject.model.Course;
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

public class ChooseYourCourseInstructors extends AppCompatActivity {
    private Button goBack;
    private ListView lvCourses;
    private DatabaseReference enrollUsersReference, userReference;
    private FirebaseUser currentUser;
    private User user = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choose_your_course_instructors);
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
        goBack = findViewById(R.id.btnCancelIns);
        lvCourses = findViewById(R.id.courseListViewIns);
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
                        user = snapshot.getValue(User.class);
                        if (user != null && user.getId() == null) {
                            user.setId(userId);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ChooseYourCourseInstructors.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                        course.setCourseId(snapshot.getKey());
                        courses.add(course);
                        Log.d(TAG, "Course loaded: " + course.toString());
                    }
                }

                courseAdapter<Course> adapter = new courseAdapter<>(ChooseYourCourseInstructors.this, 0, 0, courses);
                lvCourses.setAdapter(adapter);

                lvCourses.setOnItemClickListener((parent, view, position, id) -> {
                    if (user == null) {
                        Toast.makeText(ChooseYourCourseInstructors.this, "User info not loaded", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Course course = (Course) parent.getItemAtPosition(position);
                    if (course == null || course.getCourseId() == null) {
                        Toast.makeText(ChooseYourCourseInstructors.this, "Course information invalid", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    navigateToCourseInstructors(course.getCourseId());
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load courses: " + databaseError.getMessage());
                Toast.makeText(ChooseYourCourseInstructors.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToCourseInstructors(String courseId) {
        Intent intent = new Intent(ChooseYourCourseInstructors.this, CourseInst.class);
        intent.putExtra("courseId", courseId);
        startActivity(intent);
    }

    private void setupButtonListeners() {
        goBack.setOnClickListener(v -> {
            startActivity(new Intent(ChooseYourCourseInstructors.this, MyLists.class));
            finish();
        });
    }
}


