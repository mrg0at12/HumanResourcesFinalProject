package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.humanresourcesfinalproject.model.Course;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CreateCourse extends AppCompatActivity {

    private EditText etCourseName, etPriceForPupil, etPriceForTeacher;
    private CalendarView calendarViewStart, calendarViewEnd;
    private Button btnSave;
    private DatabaseReference databaseReference;

    private Date startDate, endDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_course);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("courses");


        Button btnGoBack = findViewById(R.id.btnGobackCreateCouse);
        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to StartPage
                Intent intent = new Intent(CreateCourse.this, SystemManagement.class);
                startActivity(intent);
                finish(); // Finish the current activity (LoginActivity)
            }
        });


        // Initialize UI elements
        etCourseName = findViewById(R.id.etCourseName);
        etPriceForPupil = findViewById(R.id.etPriceForp);
        etPriceForTeacher = findViewById(R.id.etPricforT);
        calendarViewStart = findViewById(R.id.calendarView);
        calendarViewEnd = findViewById(R.id.calendarView2);
        btnSave = findViewById(R.id.doneCreateCourse);


        databaseReference = FirebaseDatabase.getInstance().getReference("courses");

        // Set listeners for calendar views
        calendarViewStart.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            startDate = convertToDate(year, month, dayOfMonth);
        });

        calendarViewEnd.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            endDate = convertToDate(year, month, dayOfMonth);

            // Ensure end date is not earlier than start date
            if (endDate != null && startDate != null && endDate.before(startDate)) {
                // Show a toast to inform the user
                Toast.makeText(CreateCourse.this, "End date cannot be earlier than start date", Toast.LENGTH_SHORT).show();

                // Reset the end date to the start date
                endDate = startDate;
                // Optionally, update the calendar view to reflect the reset end date
                calendarViewEnd.setDate(startDate.getTime());
            }
        });

        // Button click to save course to Firebase
        btnSave.setOnClickListener(view -> saveCourseToFirebase());

        // Button click to go back to the previous activity (System Management)

    }

    // Method to convert the selected year, month, and day into a Date object
    private Date convertToDate(int year, int month, int day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = year + "-" + (month + 1) + "-" + day;
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to save the course to Firebase Realtime Database
    private void saveCourseToFirebase() {
        String courseName = etCourseName.getText().toString();
        String pricePupilStr = etPriceForPupil.getText().toString();
        String priceTeachStr = etPriceForTeacher.getText().toString();

        // Validate input fields
        if (courseName.isEmpty() || pricePupilStr.isEmpty() || priceTeachStr.isEmpty() || startDate == null || endDate == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double pricePupil = Double.parseDouble(pricePupilStr);
        double priceTeach = Double.parseDouble(priceTeachStr);

        String courseId = UUID.randomUUID().toString(); // Generate a unique ID for the course
        Course course = new Course(courseId, courseId, courseName, startDate, endDate, pricePupil, priceTeach);

        // Save the course to Firebase
        databaseReference.child(courseId).setValue(course)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(CreateCourse.this, "Course Saved!", Toast.LENGTH_SHORT).show();

                    // Navigate to the System Management page
                    Intent intent = new Intent(CreateCourse.this, SystemManagement.class);
                    startActivity(intent);

                    // Optionally, close the CreateCourse activity
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(CreateCourse.this, "Failed to Save", Toast.LENGTH_SHORT).show());
    }
}