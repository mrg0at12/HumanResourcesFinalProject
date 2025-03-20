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
    private DatabaseReference coursesReference;
    // Reference to the "Users" node for regular users.
    private DatabaseReference userReference;
    private FirebaseUser currentUser;
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
        goBack.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpForCourse.this, MainPage.class);
            startActivity(intent);
            finish();
        });

        coursesReference = FirebaseDatabase.getInstance().getReference("courses");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        lvCourses = findViewById(R.id.LVcourse);
        getCoursesFromFirebase();

        lvCourses.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCourseId = (String) parent.getItemAtPosition(position);
            assignUserToCourse(selectedCourseId);
        });

    }

    private void getCoursesFromFirebase() {
        coursesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<Course> courses=new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Course course = snapshot.getValue(Course.class);
                    if (course != null) {

                        courses.add(course);
                    }
                }
                courseAdapter<Course> adapter = new courseAdapter<>(SignUpForCourse.this,
                        0,0, courses);
                lvCourses.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SignUpForCourse.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Updated method: Try "Users" node first, then fallback to "Admins"
    private void assignUserToCourse(String courseId) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Try to fetch data from the "Users" node
            userReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Boolean isAdmin = snapshot.child("isAdmin").getValue(Boolean.class);
                        if (isAdmin != null && isAdmin) {
                            // The logged-in user is an admin – assign via admin method.
                            assignAdminToCourse(userId, courseId);
                        } else {
                            // Regular user
                            assignStudentToCourse(userId, courseId);
                        }
                    } else {
                        // Not found in "Users" node, try the "Admins" node
                        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admins");
                        adminRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot adminSnapshot) {
                                if (adminSnapshot.exists()) {
                                    assignAdminToCourse(userId, courseId);
                                } else {
                                    Toast.makeText(SignUpForCourse.this, "User data not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError error) {
                                Toast.makeText(SignUpForCourse.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(SignUpForCourse.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void assignStudentToCourse(String userId, String courseId) {
        DatabaseReference studentCoursesRef = userReference.child(userId).child("enrolledCourses");
        studentCoursesRef.orderByValue().equalTo(courseId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(SignUpForCourse.this, "Already enrolled in this course", Toast.LENGTH_SHORT).show();
                        } else {
                            studentCoursesRef.push().setValue(courseId)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(SignUpForCourse.this, "Successfully enrolled in course", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(SignUpForCourse.this, "Failed to enroll in course", Toast.LENGTH_SHORT).show());
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(SignUpForCourse.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void assignAdminToCourse(String userId, String courseId) {
        DatabaseReference adminCoursesRef = FirebaseDatabase.getInstance().getReference("Admins")
                .child(userId).child("adminCourses");
        adminCoursesRef.orderByValue().equalTo(courseId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(SignUpForCourse.this, "Already enrolled in this course", Toast.LENGTH_SHORT).show();
                        } else {
                            adminCoursesRef.push().setValue(courseId)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(SignUpForCourse.this, "Admin successfully assigned to course", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(SignUpForCourse.this, "Failed to assign admin to course", Toast.LENGTH_SHORT).show());
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(SignUpForCourse.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String formatCourseDetails(Course course) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startDate = dateFormat.format(course.getStartDate());
        String endDate = dateFormat.format(course.getEndDate());
        String pricePupil = String.format(Locale.getDefault(), "$%.2f", course.getPricePupil());
        String priceTeacher = String.format(Locale.getDefault(), "$%.2f", course.getPriceTeach());
        return String.format("%s\nStart Date: %s\nEnd Date: %s\nPrice - Pupil: %s / Teacher: %s",
                course.getCourseName(), startDate, endDate, pricePupil, priceTeacher);
    }
}