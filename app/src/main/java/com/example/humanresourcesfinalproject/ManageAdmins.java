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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.humanresourcesfinalproject.model.Admin;
import com.example.humanresourcesfinalproject.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageAdmins extends AppCompatActivity {
    private ListView lvUserAdminManage;
    private ArrayList<String> userList;
    private ArrayList<User> userObjects;
    private ArrayAdapter<String> adapter;
    private DatabaseReference usersRef, adminsRef;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_admins);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button goBackBtn = findViewById(R.id.btngoBackManageAdmin);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to StartPage
                Intent intent = new Intent(ManageAdmins.this, SystemManagement.class);
                startActivity(intent);
                finish(); // Finish the current activity (LoginActivity)
            }
        });

        searchView = findViewById(R.id.searchViewUserAdmin);


        lvUserAdminManage = findViewById(R.id.lvUserAdminManage);
        userList = new ArrayList<>();
        userObjects = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        lvUserAdminManage.setAdapter(adapter);

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

        // Initialize Firebase references
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        adminsRef = FirebaseDatabase.getInstance().getReference("Admins");

        fetchUsers();

        // Set long-click listener to promote user to admin
        lvUserAdminManage.setOnItemLongClickListener((parent, view, position, id) -> {
            User selectedUser = userObjects.get(position);
            showAdminConfirmationDialog(selectedUser);
            return true;
        });
    }
    private void fetchUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userList.clear();
                userObjects.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        userObjects.add(user);
                        String userInfo = user.getFname() + " " + user.getLname() + " - " + user.getEmail() + " - " + user.getPhone();
                        userList.add(userInfo);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ManageAdmins.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAdminConfirmationDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Promote to Admin")
                .setMessage("Are you sure you want to make " + user.getFname() + " an admin?")
                .setPositiveButton("Yes", (dialog, which) -> makeUserAdmin(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void makeUserAdmin(User user) {
        String userId = user.getId();

        // Create an Admin object using User's data
        Admin admin = new Admin(
                user.getId(), user.getFname(), user.getLname(), user.getPhone(), user.getEmail(), user.getPassword(),
                user.getShirtSize(), user.getKidId(), user.getSchool(), user.getParentName(), user.getParentPhone(),
                user.getSchoolYear(), user.getHealthFund(), user.getHealthProblems(), user.getFoodType(),
                user.getIsTeacher(), user.getIsGuide(), true // Set isAdmin = true
        );

        // Add to "Admins" node
        adminsRef.child(userId).setValue(admin)
                .addOnSuccessListener(aVoid -> {
                    // Remove from "Users"
                    usersRef.child(userId).removeValue()
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(ManageAdmins.this, user.getFname() + " is now an admin!", Toast.LENGTH_SHORT).show();
                                fetchUsers(); // Refresh user list
                            })
                            .addOnFailureListener(e -> Toast.makeText(ManageAdmins.this, "Failed to remove from Users", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(ManageAdmins.this, "Failed to promote user", Toast.LENGTH_SHORT).show());
    }
}