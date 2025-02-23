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
        goBack=findViewById(R.id.GoBackSignUpCourseBtn);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to StartPage
                Intent intent = new Intent(SignUpForCourse.this, MainPage.class);
                startActivity(intent);
                finish(); // Finish the current activity (LoginActivity)
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("courses");

        // Initialize ListView
        lvCourses = findViewById(R.id.LVcourse);

        // Get courses from Firebase and display them
        getCoursesFromFirebase();

    }

    private void getCoursesFromFirebase() {
        // Create a listener to retrieve courses from Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Create a list to store course details
                ArrayList<String> courseDetailsList = new ArrayList<>();

                // Loop through the data and add course details to the list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Course course = snapshot.getValue(Course.class);
                    if (course != null) {
                        // Format the course details
                        String courseDetails = formatCourseDetails(course);
                        courseDetailsList.add(courseDetails);
                    }
                }

                // Set the adapter to display course details in the ListView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(SignUpForCourse.this, android.R.layout.simple_list_item_1, courseDetailsList);
                lvCourses.setAdapter(adapter);
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                // Handle possible errors
                Toast.makeText(SignUpForCourse.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
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