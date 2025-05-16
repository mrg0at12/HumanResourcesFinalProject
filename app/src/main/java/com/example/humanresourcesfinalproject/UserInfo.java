package com.example.humanresourcesfinalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserInfo extends AppCompatActivity {

    private Button goBackButton;
    private ListView coursesListView;
    private DatabaseReference enrollUsersReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        goBackButton = findViewById(R.id.GoBackUserInfo);
        coursesListView = findViewById(R.id.listView2);

        // Set click listener for the button
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This will navigate back to the previous activity in the stack
                onBackPressed();
            }
        });

        TextView tvKidId = findViewById(R.id.tvKidId);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvPhone = findViewById(R.id.tvPhone);
        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvSchool = findViewById(R.id.tvSchool);
        TextView tvSchoolYear = findViewById(R.id.tvSchoolYear);
        TextView tvParentName = findViewById(R.id.tvParentName);
        TextView tvParentPhone = findViewById(R.id.tvParentPhone);
        TextView tvHealthFund = findViewById(R.id.tvHealthFund);
        TextView tvHealthProblems = findViewById(R.id.tvHealthProblems);
        TextView tvFoodType = findViewById(R.id.tvFoodType);
        TextView tvTeacher = findViewById(R.id.tvTeacher);
        TextView tvGuide = findViewById(R.id.tvGuide);

        userId = getIntent().getStringExtra("userId");
        if (userId != null) {
            // Load user information
            FirebaseDatabase.getInstance().getReference("Users").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                String kidId = "Id: " + user.getKidId();
                                String Name = "Name: " + user.getFname() + " " + user.getLname();
                                String Phone = "Phone number: " + user.getPhone();
                                String Email = "Email: " + user.getEmail();
                                String School = "School: " + user.getSchool();
                                String ParentName = "Parent Name: " + user.getParentName();
                                String ParentPhone = "Parent phone number:" + user.getParentPhone();
                                String schoolYear = "Grade:" + user.getSchoolYear();
                                String HealthFund = "Health fund: " + user.getHealthFund();
                                String HealthProblems = "Health problems: " + user.getHealthProblems();
                                String FoodType = "Food type: " + user.getFoodType();

                                String Techer;
                                if (user.getIsTeacher() == true) {
                                    Techer = "Teacher: This user is a teacher";
                                } else {
                                    Techer = "Teacher: This user is not a teacher";
                                }

                                String Guide;
                                if (user.getIsGuide() == true) {
                                    Guide = "Guide: This user is a guide";
                                } else {
                                    Guide = "Guide: This user is not a guide";
                                }

                                tvKidId.setText(kidId);
                                tvName.setText(Name);
                                tvPhone.setText(Phone);
                                tvEmail.setText(Email);
                                tvSchool.setText(School);
                                tvSchoolYear.setText(schoolYear);
                                tvParentName.setText(ParentName);
                                tvParentPhone.setText(ParentPhone);
                                tvHealthFund.setText(HealthFund);
                                tvHealthProblems.setText(HealthProblems);
                                tvFoodType.setText(FoodType);
                                tvTeacher.setText(Techer);
                                tvGuide.setText(Guide);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(UserInfo.this, "Failed to load user info.", Toast.LENGTH_SHORT).show();
                        }
                    });

            // Load user's courses
            loadUserCourses();
        }


    }
    private void loadUserCourses() {
        enrollUsersReference = FirebaseDatabase.getInstance().getReference("EnrollForUsers2").child(userId);

        enrollUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Course> courses = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Course course = snapshot.getValue(Course.class);
                    if (course != null) {
                        courses.add(course);
                    }
                }

                // Check if any courses were found
                if (courses.isEmpty()) {
                    Toast.makeText(UserInfo.this, "No courses found for this user", Toast.LENGTH_SHORT).show();
                }

                // Set up the adapter with the courses
                courseAdapter<Course> adapter = new courseAdapter<>(UserInfo.this, 0, 0, courses);
                coursesListView.setAdapter(adapter);

                // Set item click listener if needed
                coursesListView.setOnItemClickListener((parent, view, position, id) -> {
                    Course selectedCourse = (Course) parent.getItemAtPosition(position);
                    // Handle course selection if needed
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserInfo.this, "Error loading courses: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}