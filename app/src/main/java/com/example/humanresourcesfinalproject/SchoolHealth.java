package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.humanresourcesfinalproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SchoolHealth extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> healthIssueList;
    private DatabaseReference databaseReference;
    private String currentUserSchool = ""; // User's school
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_school_health);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button goBackBtn = findViewById(R.id.GoBackHealthSchool);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to StartPage
                Intent intent = new Intent(SchoolHealth.this, MyLists.class);
                startActivity(intent);
                finish(); // Finish the current activity (LoginActivity)
            }
        });

        listView = findViewById(R.id.healthListView);
        healthIssueList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, healthIssueList);
        listView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        fetchCurrentUserSchool();
    }

    private void fetchCurrentUserSchool() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        currentUserSchool = user.getSchool();
                        fetchUsersWithHealthIssues();
                    } else {
                        Toast.makeText(SchoolHealth.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SchoolHealth.this, "Failed to fetch user school", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchUsersWithHealthIssues() {
        if (currentUserSchool.isEmpty()) {
            Toast.makeText(this, "No school found for user", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                healthIssueList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user != null && user.getSchool().equals(currentUserSchool)) {
                        String healthProblems = user.getHealthProblems();
                        if (healthProblems != null && !healthProblems.equalsIgnoreCase("None") && !healthProblems.trim().isEmpty()) {
                            String details = "👤 " + user.getFname() + " " + user.getLname() +
                                    "\n📧 Email: " + user.getEmail() +
                                    "\n📞 Phone: " + user.getPhone() +
                                    "\n⚕️ Health Issues: " + healthProblems +
                                    "\n--------------------------";
                            healthIssueList.add(details);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                if (healthIssueList.isEmpty()) {
                    Toast.makeText(SchoolHealth.this, "No students with health issues found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SchoolHealth.this, "Failed to load students.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}