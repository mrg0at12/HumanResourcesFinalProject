package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.humanresourcesfinalproject.model.Course;
import com.example.humanresourcesfinalproject.model.Enroll;
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
    private Button goBack;
    private ListView lvCourses;
    private DatabaseReference coursesReference, userReference,enrollUsersReference,enrollCoursesReference;
    private FirebaseUser currentUser;
    private ArrayList<String> courseIds;
    private User user=null;

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

        coursesReference = FirebaseDatabase.getInstance().getReference("courses");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        enrollUsersReference = FirebaseDatabase.getInstance().getReference("EnrollForUsers").push();
        enrollCoursesReference = FirebaseDatabase.getInstance().getReference("EnrollCourses");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        courseIds = new ArrayList<>();  // Initialize course ID list
        getCoursesFromFirebase();

        goBack.setOnClickListener(v -> {
            startActivity(new Intent(SignUpForCourse.this, MainPage.class));
            finish();
        });

        String userId = currentUser.getUid();
        userReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    user = snapshot.getValue(User.class);
                    Boolean isAdmin = snapshot.child("isAdmin").getValue(Boolean.class);

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SignUpForCourse.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void getCoursesFromFirebase() {
        coursesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Course> courses = new ArrayList<>();
                courseIds.clear();  // Clear previous data

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Course course = snapshot.getValue(Course.class);
                    if (course != null) {
                        courses.add(course);
                        courseIds.add(snapshot.getKey());  // Store Firebase Course ID
                    }
                }

                courseAdapter<Course> adapter = new courseAdapter<>(SignUpForCourse.this, 0, 0, courses);
                lvCourses.setAdapter(adapter);

                lvCourses.setOnItemClickListener((parent, view, position, id) -> {
                    String selectedCourseId = courseIds.get(position);

                    Course course= (Course) parent.getItemAtPosition(position);
                    Enroll newEnroll=new Enroll(enrollUsersReference.getKey(),user.getId(),course);
                    enrollUsersReference.child(user.getId()).setValue(newEnroll);

                    enrollCoursesReference.child(course.getCourseId()).child(user.getId()).setValue(user);
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SignUpForCourse.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void assignUserToCourse(Course course) {
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }




    }

    private void assignAdminToCourse(String userId, String courseId) {
        DatabaseReference adminCoursesRef = FirebaseDatabase.getInstance().getReference("Admins").child(userId).child("adminCourses");
        adminCoursesRef.orderByValue().equalTo(courseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(SignUpForCourse.this, "Already assigned to this course", Toast.LENGTH_SHORT).show();
                } else {
                    adminCoursesRef.push().setValue(courseId)
                            .addOnSuccessListener(aVoid -> Toast.makeText(SignUpForCourse.this, "Admin successfully assigned to course", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(SignUpForCourse.this, "Failed to assign admin to course", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SignUpForCourse.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}