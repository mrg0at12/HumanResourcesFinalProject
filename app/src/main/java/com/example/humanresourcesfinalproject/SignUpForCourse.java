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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class SignUpForCourse extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    private Button goBack;
    private ListView lvCourses;
    private TextView tvUserRole;

    // Firebase References
    private DatabaseReference coursesReference;
    private DatabaseReference userReference;
    private DatabaseReference enrollUsersReference;
    private DatabaseReference enrollCoursesReference;
    private FirebaseUser currentUser;

    // Data
    private ArrayList<String> courseIds;
    private User user = null;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_for_course);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initializeViews();

        // Request notification permission
        requestNotificationPermission();

        // Setup Firebase references
        setupFirebaseReferences();

        // Load user data
        loadUserData();

        // Get courses from Firebase
        getCoursesFromFirebase();

        // Set up button click listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        goBack = findViewById(R.id.GoBackSignUpCourseBtn);
        lvCourses = findViewById(R.id.LVcourse);
        tvUserRole = findViewById(R.id.tvUserRole);
    }

    private void setupFirebaseReferences() {
        coursesReference = FirebaseDatabase.getInstance().getReference("courses");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        enrollUsersReference = FirebaseDatabase.getInstance().getReference("EnrollForUsers2");
        enrollCoursesReference = FirebaseDatabase.getInstance().getReference("EnrollCourses2");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        courseIds = new ArrayList<>();
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
                                tvUserRole.setText("Admin Mode");
                                return;
                            }
                        }

                        // If not admin, load as regular user
                        user = snapshot.getValue(User.class);
                        if (user != null) {
                            // Initialize user ID if null
                            if (user.getId() == null) {
                                user.setId(currentUser.getUid());
                            }

                            boolean isTeacher = user.getIsTeacher() != null && user.getIsTeacher();
                            if (isTeacher) {
                                tvUserRole.setText("Teacher Mode");
                            } else {
                                tvUserRole.setText("Student Mode");
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                    Toast.makeText(SignUpForCourse.this, "Database error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getCoursesFromFirebase() {
        coursesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Course> courses = new ArrayList<>();
                courseIds.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Course course = snapshot.getValue(Course.class);
                    if (course != null) {
                        course.setCourseId(snapshot.getKey()); // Ensure course has ID
                        courses.add(course);
                        courseIds.add(snapshot.getKey());
                    }
                }

                courseAdapter<Course> adapter = new courseAdapter<>(SignUpForCourse.this, 0, 0, courses);
                lvCourses.setAdapter(adapter);

                lvCourses.setOnItemClickListener((parent, view, position, id) -> {
                    if (user == null || user.getId() == null) {
                        Toast.makeText(SignUpForCourse.this, "User information not available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Course course = (Course) parent.getItemAtPosition(position);
                    if (course == null || course.getCourseId() == null) {
                        Toast.makeText(SignUpForCourse.this, "Course information not available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    enrollUserToCourse(user, course);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load courses: " + databaseError.getMessage());
                Toast.makeText(SignUpForCourse.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enrollUserToCourse(User user, Course course) {
        String userId = user.getId();
        String courseId = course.getCourseId();

        // Validate all required fields
        if (userId == null || userId.isEmpty() || courseId == null || courseId.isEmpty()) {
            Toast.makeText(this, "Missing required information", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enroll user to course
        enrollUsersReference.child(userId).child(courseId).setValue(course)
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        enrollCoursesReference.child(courseId).child(userId).setValue(user)
                                .addOnCompleteListener(courseTask -> {
                                    if (courseTask.isSuccessful()) {
                                        // Successfully enrolled
                                        scheduleNotification(
                                                user.getFname() != null ? user.getFname() : "User",
                                                course.getCourseName() != null ? course.getCourseName() : "Course"
                                        );

                                        String message = isAdmin ? "Assigned to course: " : "Signed up for course: ";
                                        Toast.makeText(SignUpForCourse.this, message + course.getCourseName(), Toast.LENGTH_SHORT).show();

                                        // Navigate if teacher or admin
                                        boolean isTeacher = user.getIsTeacher() != null && user.getIsTeacher();
                                        if (isTeacher || isAdmin) {
                                            Intent intent = new Intent(SignUpForCourse.this, CourseCompList.class);
                                            intent.putExtra("courseId", courseId);
                                            startActivity(intent);
                                        }
                                    } else {
                                        Log.e(TAG, "Course enrollment failed: " + courseTask.getException());
                                        Toast.makeText(SignUpForCourse.this, "Failed to enroll in course", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.e(TAG, "User enrollment failed: " + userTask.getException());
                        Toast.makeText(SignUpForCourse.this, "Failed to enroll in course", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupButtonListeners() {
        goBack.setOnClickListener(v -> {
            startActivity(new Intent(SignUpForCourse.this, MainPage.class));
            finish();
        });
    }

    private void scheduleNotification(String username, String courseName) {
        try {
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("username", username);
            intent.putExtra("courseName", courseName);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pendingIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notification", e);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}