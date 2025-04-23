package com.example.humanresourcesfinalproject;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.humanresourcesfinalproject.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserInfo extends AppCompatActivity {

    TextView userInfoText;

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


        userInfoText = findViewById(R.id.tvUserInfo);

        String userId = getIntent().getStringExtra("userId");
        if (userId != null) {
            FirebaseDatabase.getInstance().getReference("Users").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {

                                String Name="Name: "+user.getFname() + " " + user.getLname();
                                String Phone="Phone: "+ user.getPhone();
                                String Email="Email: "+user.getEmail();
                                String School="School: "+user.getSchool();
                                String ParentName= "Parent Name: "+ user.getParentName();

                                String Techer;
                                if(user.getIsTeacher()==true)
                                {
                                    Techer="Teacher: This user is a teacher";
                                }
                                else {
                                    Techer="Techer: This user is not a teacher";
                                }

                                String Guide;
                                if(user.getIsGuide()==true)
                                {
                                    Guide="Guide: This user is a guide";
                                }
                                else {
                                    Guide="Guide: This user is not a guide";
                                }
                                String KidId="Kid ID: "+user.getKidId();
                                String schoolYear="School Year: "+user.getSchoolYear();
                                String HealthFund="Health Fund: "+user.getHealthFund();
                                String HealthProblems="Health Problems: "+user.getHealthProblems();
                                String Courses="Enrolled Courses: "+user.getEnrolledCourses();

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            userInfoText.setText("Failed to load user info.");
                        }
                    });
        }

    }
}