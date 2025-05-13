package com.example.humanresourcesfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.atomic.AtomicBoolean;

public class EnterAnim extends AppCompatActivity {
    private ImageView logoImage;
    private TextView appTitle;
    private TextView appSubtitle;

    // Thread control flags
    private AtomicBoolean isRunning = new AtomicBoolean(true);
    private Thread logoAnimThread;
    private Thread titleAnimThread;
    private Thread subtitleAnimThread;
    private Thread finalAnimThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_enter_anim);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logoImage = findViewById(R.id.logo_image);
        appTitle = findViewById(R.id.app_title);
        appSubtitle = findViewById(R.id.app_subtitle);

        // Initially hide elements that will be animated in
        logoImage.setAlpha(0f);
        appTitle.setAlpha(0f);
        appSubtitle.setAlpha(0f);
        startAnimationSequence();

    }
    private void startAnimationSequence() {
        // Thread 1: Logo Animation
        logoAnimThread = new Thread(() -> {
            try {
                // Start logo animation on UI thread
                runOnUiThread(() -> {
                    logoImage.animate()
                            .alpha(1f)
                            .scaleX(1.2f)
                            .scaleY(1.2f)
                            .setDuration(1000)
                            .withEndAction(() ->
                                    logoImage.animate()
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .setDuration(500)
                                            .start()
                            )
                            .start();
                });

                // Wait for logo animation to complete
                Thread.sleep(1500);

                // Start the title animation thread
                if (isRunning.get()) {
                    titleAnimThread.start();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Thread 2: Title Animation
        titleAnimThread = new Thread(() -> {
            try {
                // Start title animation on UI thread
                runOnUiThread(() -> {
                    Animation slideIn = AnimationUtils.loadAnimation(EnterAnim.this, R.anim.slide_in_right);
                    appTitle.startAnimation(slideIn);
                    appTitle.setAlpha(1f);
                });

                // Wait for title animation to complete
                Thread.sleep(1000);

                // Start the subtitle animation thread
                if (isRunning.get()) {
                    subtitleAnimThread.start();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Thread 3: Subtitle Animation
        subtitleAnimThread = new Thread(() -> {
            try {
                // Start subtitle animation on UI thread
                runOnUiThread(() -> {
                    appSubtitle.animate()
                            .alpha(1f)
                            .translationYBy(-50f)
                            .setDuration(800)
                            .start();
                });

                // Wait for subtitle animation to complete
                Thread.sleep(1500);

                // Start the final animation thread
                if (isRunning.get()) {
                    finalAnimThread.start();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Thread 4: Final Animation and Transition
        finalAnimThread = new Thread(() -> {
            try {
                // Start final pulse animation on UI thread
                runOnUiThread(() -> {
                    Animation pulse = AnimationUtils.loadAnimation(EnterAnim.this, R.anim.pulse);
                    logoImage.startAnimation(pulse);
                    appTitle.startAnimation(pulse);
                    appSubtitle.startAnimation(pulse);
                });

                // Wait for final animation to complete
                Thread.sleep(1000);

                // Transition to StartPage if still running
                if (isRunning.get()) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(EnterAnim.this, StartPage.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Start the first thread to begin the sequence
        logoAnimThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop all animation threads if activity is destroyed
        isRunning.set(false);

        // Interrupt all threads
        if (logoAnimThread != null && logoAnimThread.isAlive()) {
            logoAnimThread.interrupt();
        }
        if (titleAnimThread != null && titleAnimThread.isAlive()) {
            titleAnimThread.interrupt();
        }
        if (subtitleAnimThread != null && subtitleAnimThread.isAlive()) {
            subtitleAnimThread.interrupt();
        }
        if (finalAnimThread != null && finalAnimThread.isAlive()) {
            finalAnimThread.interrupt();
        }
    }
}