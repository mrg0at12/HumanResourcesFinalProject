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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class SignUpForCourse extends AppCompatActivity {
    private Button goBack;
    private ListView lvCourses;
    private DatabaseReference databaseReference;
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

        databaseReference = FirebaseDatabase.getInstance().getReference("courses");
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
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> courseDetailsList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Course course = snapshot.getValue(Course.class);
                    if (course != null) {
                        String courseDetails = formatCourseDetails(course);
                        courseDetailsList.add(courseDetails);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(SignUpForCourse.this, android.R.layout.simple_list_item_1, courseDetailsList);
                lvCourses.setAdapter(adapter);
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Toast.makeText(SignUpForCourse.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void assignUserToCourse(String courseId) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            userReference.child(userId).child("enrolledCourses").push().setValue(courseId)
                    .addOnSuccessListener(aVoid -> Toast.makeText(SignUpForCourse.this, "Successfully enrolled in course", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(SignUpForCourse.this, "Failed to enroll in course", Toast.LENGTH_SHORT).show());
        }
    }

    // Format course details to display in a readable way
    private String formatCourseDetails(Course course) {
        // Format dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startDate = dateFormat.format(course.getStartDate());
        String endDate = dateFormat.format(course.getEndDate());

        // Get price details
        String pricePupil = String.format(Locale.getDefault(), "$%.2f", course.getPricePupil());
        String priceTeacher = String.format(Locale.getDefault(), "$%.2f", course.getPriceTeach());

        // Format the full course details string
        return String.format("%s\nStart Date: %s\nEnd Date: %s\nPrice - Pupil: %s / Teacher: %s",
                course.getCourseName(), startDate, endDate, pricePupil, priceTeacher);
    }
}