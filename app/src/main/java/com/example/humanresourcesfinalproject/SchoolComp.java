package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.humanresourcesfinalproject.model.Admin;
import com.example.humanresourcesfinalproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SchoolComp extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> schoolMembersList;
    private DatabaseReference usersRef, adminsRef;
    private String currentUserSchool = "";
    private FirebaseAuth auth;
    private int pendingQueries = 0;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_school_comp);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        searchView=findViewById(R.id.SvSchoolComp);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // Not needed for live filtering
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        Button goBackBtn = findViewById(R.id.GoBackCompSchool);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to StartPage
                Intent intent = new Intent(SchoolComp.this, MyLists.class);
                startActivity(intent);
                finish(); // Finish the current activity (LoginActivity)
            }
        });

        listView = findViewById(R.id.schoolListView);
        schoolMembersList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, schoolMembersList);
        listView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        adminsRef = FirebaseDatabase.getInstance().getReference("Admins");

        fetchCurrentUserSchool();

    }

    private void fetchCurrentUserSchool() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        currentUserSchool = user.getSchool();
                        fetchSchoolMembers();
                    } else {
                        Toast.makeText(SchoolComp.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SchoolComp.this, "Failed to fetch user school", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchSchoolMembers() {
        if (currentUserSchool.isEmpty()) {
            Toast.makeText(this, "No school found for user", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingQueries = 2; // We are making two queries (Users & Admins)

        fetchUsers();
        fetchAdmins();
    }

    private void fetchUsers() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user != null && user.getSchool().equals(currentUserSchool)) {
                        String details = "👤 " + user.getFname() + " " + user.getLname() +
                                "\n📧 Email: " + user.getEmail() +
                                "\n📞 Phone: " + user.getPhone() +
                                "\n🏫 Role: " + getUserRole(user) +
                                "\n--------------------------";
                        schoolMembersList.add(details);
                    }
                }
                checkQueriesComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SchoolComp.this, "Failed to load users.", Toast.LENGTH_SHORT).show();
                checkQueriesComplete();
            }
        });
    }

    private void fetchAdmins() {
        adminsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Admin admin = data.getValue(Admin.class);
                    if (admin != null && admin.getSchool().equals(currentUserSchool)) {
                        String details = "👑 ADMIN: " + admin.getFname() + " " + admin.getLname() +
                                "\n📧 Email: " + admin.getEmail() +
                                "\n📞 Phone: " + admin.getPhone() +
                                "\n🏫 School: " + admin.getSchool() +
                                "\n--------------------------";
                        schoolMembersList.add(details);
                    }
                }
                checkQueriesComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SchoolComp.this, "Failed to load admins.", Toast.LENGTH_SHORT).show();
                checkQueriesComplete();
            }
        });
    }

    private String getUserRole(User user) {
        if (Boolean.TRUE.equals(user.getIsTeacher())) return "Teacher";
        if (Boolean.TRUE.equals(user.getIsGuide())) return "Guide";
        return "Student";
    }

    private void checkQueriesComplete() {
        pendingQueries--;
        if (pendingQueries == 0) {
            adapter.notifyDataSetChanged();
            if (schoolMembersList.isEmpty()) {
                Toast.makeText(SchoolComp.this, "No members found.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}