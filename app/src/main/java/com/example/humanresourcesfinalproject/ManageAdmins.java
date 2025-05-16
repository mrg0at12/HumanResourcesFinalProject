package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.humanresourcesfinalproject.model.Admin;
import com.example.humanresourcesfinalproject.model.ClickHandlerUtil;
import com.example.humanresourcesfinalproject.model.User;
import com.example.humanresourcesfinalproject.model.UserAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageAdmins extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ListView lvUserAdminManage;
    private UserAdapter userAdapter;
    private ArrayList<User> userList;
    private DatabaseReference usersRef, adminsRef;
    private SearchView searchView;
    private DrawerLayout drawerLayout;
    private Handler longPressHandler = new Handler();
    private static final int LONG_PRESS_DELAY = 500;
    private Runnable longPressRunnable;
    private int pressedPosition = ListView.INVALID_POSITION;
    private boolean isLongPressHandled = false;



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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Button goBackBtn = findViewById(R.id.btngoBackManageAdmin);
        goBackBtn.setOnClickListener(v -> {
            startActivity(new Intent(ManageAdmins.this, SystemManagement.class));
            finish();
        });

        searchView = findViewById(R.id.searchViewUserAdmin);
        lvUserAdminManage = findViewById(R.id.lvUserAdminManage);
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, 0, userList);
        lvUserAdminManage.setAdapter(userAdapter);

        ClickHandlerUtil.setupListViewClicks(lvUserAdminManage, new ClickHandlerUtil.ClickCallbacks() {
            @Override
            public void onShortClick(int position) {
                User user = userAdapter.getItem(position);
                Intent intent = new Intent(ManageAdmins.this, UserInfo.class);
                intent.putExtra("userId", user.getId());
                startActivity(intent);
            }

            @Override
            public void onLongClick(int position) {
                User user = userAdapter.getItem(position);
                showAdminConfirmationDialog(user);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                userAdapter.getFilter().filter(newText);
                return false;
            }
        });

        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        adminsRef = FirebaseDatabase.getInstance().getReference("Admins");
        fetchUsers();
    }

    private void fetchUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<User> tempList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        tempList.add(user);
                    }
                }
                userList.clear();
                userList.addAll(tempList);
                userAdapter.updateList(userList);
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
        Admin admin = new Admin(
                user.getId(), user.getFname(), user.getLname(), user.getPhone(),
                user.getEmail(), user.getPassword(), user.getShirtSize(),
                user.getKidId(), user.getSchool(), user.getParentName(),
                user.getParentPhone(), user.getSchoolYear(), user.getHealthFund(),
                user.getHealthProblems(), user.getFoodType(), user.getIsTeacher(),
                user.getIsGuide(), true
        );

        adminsRef.child(userId).setValue(admin)
                .addOnSuccessListener(aVoid -> {
                    usersRef.child(userId).removeValue()
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(ManageAdmins.this, user.getFname() + " is now an admin!", Toast.LENGTH_SHORT).show();
                                fetchUsers();
                            });
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_create_course) {
            startActivity(new Intent(this, CreateCourse.class));
        } else if (id == R.id.nav_delete_course) {
            startActivity(new Intent(this, DeleteCourse.class));
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        longPressHandler.removeCallbacksAndMessages(null);
    }
}