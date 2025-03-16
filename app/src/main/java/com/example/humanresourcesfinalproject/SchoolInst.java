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

import com.example.humanresourcesfinalproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SchoolInst extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> instructorList;
    private DatabaseReference databaseReference;
    private String currentUserSchool = ""; // Initially empty
    private FirebaseAuth auth;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_inst);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        searchView = findViewById(R.id.SvSchoolInst);

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

        listView = findViewById(R.id.instructorListView); // ✅ Use ListView from XML
        instructorList = new ArrayList<>();

        // ✅ Use Built-in `simple_list_item_1`
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, instructorList);
        listView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        fetchCurrentUserSchool();

        Button goBackBtn = findViewById(R.id.btnGoBackInst);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to StartPage
                Intent intent = new Intent(SchoolInst.this, MyLists.class);
                startActivity(intent);
                finish(); // Finish the current activity (LoginActivity)
            }
        });
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
                        fetchInstructors();
                    } else {
                        Toast.makeText(SchoolInst.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SchoolInst.this, "Failed to fetch user school", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchInstructors() {
        if (currentUserSchool.isEmpty()) {
            Toast.makeText(this, "No school found for user", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                instructorList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user != null && user.getSchool().equals(currentUserSchool) &&
                            (Boolean.TRUE.equals(user.getIsTeacher()) || Boolean.TRUE.equals(user.getIsGuide()))) {

                        // ✅ Keep the text format simple (1 line per instructor)
                        String details = "👤 " + user.getFname() + " " + user.getLname() +
                                "\n📧 Email: " + user.getEmail() +
                                "\n📞 Phone: " + user.getPhone() +
                                "\n🏫 School: " + user.getSchool() +
                                "\n👨‍👩‍👦 Parent: " + user.getParentName() + " (" + user.getParentPhone() + ")" +
                                "\n📆 Year: " + user.getSchoolYear() +
                                "\n🏥 Health Fund: " + user.getHealthFund() +
                                "\n⚕️ Health Issues: " + user.getHealthProblems() +
                                "\n🍽️ Food Type: " + user.getFoodType() +
                                "\n--------------------------";
                        instructorList.add(details);
                    }
                }
                adapter.notifyDataSetChanged();
                if (instructorList.isEmpty()) {
                    Toast.makeText(SchoolInst.this, "No instructors found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SchoolInst.this, "Failed to load instructors.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}