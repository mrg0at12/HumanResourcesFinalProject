package com.example.humanresourcesfinalproject;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.humanresourcesfinalproject.model.Admin;
import com.example.humanresourcesfinalproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ResetPassword extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private EditText currentPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private EditText targetUserIdEditText; // For admin to specify which user's password to change
    private Button changePasswordButton, goBackButton;
    private boolean isAdmin = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        targetUserIdEditText = findViewById(R.id.targetUserIdEditText); // Add this EditText in your layout
        changePasswordButton = findViewById(R.id.changePasswordButton);
        goBackButton = findViewById(R.id.goBackButton);

        // Check if current user is admin
        checkAdminStatus();

        changePasswordButton.setOnClickListener(v -> changePassword());
        goBackButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResetPassword.this, MainPage.class);
            startActivity(intent);
            finish();
        });
    }

    private void checkAdminStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Admin admin = snapshot.getValue(Admin.class);
                    if (admin != null && Boolean.TRUE.equals(admin.getIsAdmin())) {
                        isAdmin = true;
                        // Show admin-specific UI elements
                        targetUserIdEditText.setVisibility(View.VISIBLE);
                        currentPasswordEditText.setHint("Admin password"); // For verification
                    } else {
                        // Regular user UI
                        targetUserIdEditText.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ResetPassword.this, "Failed to check admin status", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void changePassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String targetUserId = targetUserIdEditText.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "New password fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isAdmin) {
            // Admin password change flow
            if (TextUtils.isEmpty(currentPassword)) {
                Toast.makeText(this, "Admin password required for verification", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(targetUserId)) {
                Toast.makeText(this, "Please specify user ID to change", Toast.LENGTH_SHORT).show();
                return;
            }

            changePasswordAsAdmin(currentPassword, targetUserId, newPassword);
        } else {
            // Regular user password change flow
            if (TextUtils.isEmpty(currentPassword)) {
                Toast.makeText(this, "Current password is required", Toast.LENGTH_SHORT).show();
                return;
            }

            changePasswordAsUser(currentPassword, newPassword);
        }
    }

    private void changePasswordAsUser(String currentPassword, String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // First verify current password
            mAuth.signInWithEmailAndPassword(user.getEmail(), currentPassword)
                    .addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            // Password verified, now update
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // Update in database
                                            usersRef.child(user.getUid()).child("password").setValue(newPassword)
                                                    .addOnCompleteListener(dbTask -> {
                                                        if (dbTask.isSuccessful()) {
                                                            Toast.makeText(ResetPassword.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Log.e("ResetPassword", "Database update failed", dbTask.getException());
                                                            Toast.makeText(ResetPassword.this, "Database update failed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Log.e("ResetPassword", "Password change failed", task.getException());
                                            Toast.makeText(ResetPassword.this, "Password change failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Log.e("ResetPassword", "Current password verification failed", authTask.getException());
                            Toast.makeText(ResetPassword.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void changePasswordAsAdmin(String adminPassword, String targetUserId, String newPassword) {
        FirebaseUser adminUser = mAuth.getCurrentUser();
        if (adminUser != null) {
            // First verify admin password
            mAuth.signInWithEmailAndPassword(adminUser.getEmail(), adminPassword)
                    .addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            // Admin verified, now change target user's password
                            usersRef.child(targetUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User targetUser = snapshot.getValue(User.class);
                                    if (targetUser != null) {
                                        // Update password in authentication
                                        mAuth.getCurrentUser().updatePassword(newPassword)
                                                .addOnCompleteListener(authUpdateTask -> {
                                                    if (authUpdateTask.isSuccessful()) {
                                                        // Update in database
                                                        usersRef.child(targetUserId).child("password").setValue(newPassword)
                                                                .addOnCompleteListener(dbTask -> {
                                                                    if (dbTask.isSuccessful()) {
                                                                        Toast.makeText(ResetPassword.this,
                                                                                "Password for user " + targetUser.getFname() + " changed successfully",
                                                                                Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        Log.e("ResetPassword", "Database update failed", dbTask.getException());
                                                                        Toast.makeText(ResetPassword.this, "Database update failed", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    } else {
                                                        Log.e("ResetPassword", "Password change failed", authUpdateTask.getException());
                                                        Toast.makeText(ResetPassword.this,
                                                                "Password change failed: " + authUpdateTask.getException().getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(ResetPassword.this, "Target user not found", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(ResetPassword.this, "Failed to find target user", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.e("ResetPassword", "Admin password verification failed", authTask.getException());
                            Toast.makeText(ResetPassword.this, "Admin password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}