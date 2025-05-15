package com.example.humanresourcesfinalproject;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.humanresourcesfinalproject.model.UserAdapter;
import com.example.humanresourcesfinalproject.model.courseAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CourseCompList extends AppCompatActivity {

    UserAdapter userAdapter;
    ListView lvUser;
    ArrayList<User> users = new ArrayList();

    Intent takeit;
    String courseId = null;

    private FirebaseDatabase database;
    private DatabaseReference myUserRefCoures, myAdminRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_comp_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lvUser=findViewById(R.id.lvCourseComp);
        database = FirebaseDatabase.getInstance();


        takeit = getIntent();
        courseId = takeit.getStringExtra("courseId");

        if (courseId != null) {
            myUserRefCoures = database.getReference("EnrollCourses2").child(courseId);
            users.clear();

            myUserRefCoures.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {


                    for (DataSnapshot i : snapshot.getChildren()) {
                        User user = i.getValue(User.class);
                        if (user != null) {
                            users.add(user);

                        }
                    }

                    userAdapter = new UserAdapter(CourseCompList.this, 0,  users);
                    lvUser.setAdapter(userAdapter);


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }

            });

        }
    }
}