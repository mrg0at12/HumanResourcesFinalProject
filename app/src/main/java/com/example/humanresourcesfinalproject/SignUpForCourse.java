package com.example.humanresourcesfinalproject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
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
    private DatabaseReference coursesReference, userReference, enrollUsersReference, enrollCoursesReference;
    private FirebaseUser currentUser;
    private ArrayList<String> courseIds;
    private User user = null;

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
        goBack = findViewById(R.id.GoBackSignUpCourseBtn);
        lvCourses = findViewById(R.id.LVcourse);

        requestNotificationPermission(); // Runtime permission for Android 13+

        coursesReference = FirebaseDatabase.getInstance().getReference("courses");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        enrollUsersReference = FirebaseDatabase.getInstance().getReference("EnrollForUsers2");
        enrollCoursesReference = FirebaseDatabase.getInstance().getReference("EnrollCourses2");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        courseIds = new ArrayList<>();
        getCoursesFromFirebase();

        goBack.setOnClickListener(v -> {
            startActivity(new Intent(SignUpForCourse.this, MainPage.class));
            finish();
        });

        if (currentUser != null) {
            String userId = currentUser.getUid();
            userReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        user = snapshot.getValue(User.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SignUpForCourse.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                        courses.add(course);
                        courseIds.add(snapshot.getKey());
                    }
                }

                courseAdapter<Course> adapter = new courseAdapter<>(SignUpForCourse.this, 0, 0, courses);
                lvCourses.setAdapter(adapter);

                lvCourses.setOnItemClickListener((parent, view, position, id) -> {
                    if (user == null) {
                        Toast.makeText(SignUpForCourse.this, "User info not loaded", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Course course = (Course) parent.getItemAtPosition(position);
                    String courseId = course.getCourseId();



                   // Enroll newEnroll = new Enroll( user.getId(), course);
                   // enrollUsersReference.child(user.getId()).setValue(newEnroll);

                    enrollUsersReference.child(user.getId()).child(courseId).setValue(course);
                    enrollCoursesReference.child(courseId).child(user.getId()).setValue(user);

                    scheduleNotification(user.getFname(), course.getCourseName());
                    Toast.makeText(SignUpForCourse.this, "Signed up for course: " + course.getCourseName(), Toast.LENGTH_SHORT).show();

                    if(user.getIsTeacher()){

                        Intent go=new Intent(SignUpForCourse.this, CourseCompList.class);

                        go.putExtra("courseId",courseId);
                        startActivity(go);

                    }

                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SignUpForCourse.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scheduleNotification(String username, String courseName) {
        Intent intent = new Intent(SignUpForCourse.this, NotificationReceiver.class);
        intent.putExtra("username", username);
        intent.putExtra("courseName", courseName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                SignUpForCourse.this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = System.currentTimeMillis() + 5000; // 5 seconds delay
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
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