package com.example.myact.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myact.R;
import com.example.myact.api.ApiClient;
import com.example.myact.api.ApiService;
import com.example.myact.api.models.Quote;
import com.example.myact.utils.ThemeManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SplashActivity extends AppCompatActivity {

    private TextView textViewQuote;
    private TextView textViewAuthor;
    private TextView textViewCurrentTime;
    private CardView cardViewQuote;
    private ApiService apiService;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before inflating the layout
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        boolean isDarkTheme = sharedPreferences.getBoolean("dark_theme", false);
        ThemeManager.applyTheme(isDarkTheme);

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        textViewQuote = findViewById(R.id.text_view_quote);
        textViewAuthor = findViewById(R.id.text_view_author);
        textViewCurrentTime = findViewById(R.id.text_view_current_time);
        cardViewQuote = findViewById(R.id.card_view_quote);

        // Display current date and time
        updateDateTime();

        // Initialize API service
        apiService = new ApiClient();
        handler = new Handler(Looper.getMainLooper());

        // Animate logo and app name
        animateSplashElements();

        // Load a quote
        loadQuote();

        // Navigate to MainActivity after delay
        handler.postDelayed(this::navigateToMainActivity, 5000); // 5 seconds
    }

    private void updateDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateTime = dateFormat.format(new Date());
        textViewCurrentTime.setText(currentDateTime);
    }

    private void animateSplashElements() {
        // Fade in animation for the quote card
        cardViewQuote.setAlpha(0f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(cardViewQuote, View.ALPHA, 0f, 1f);
        fadeIn.setDuration(1500);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setStartDelay(1000);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(fadeIn);
        animatorSet.start();
    }

    private void loadQuote() {
        apiService.getQuotes(new ApiService.QuoteCallback() {
            @Override
            public void onSuccess(List<Quote> quotes) {
                if (quotes != null && !quotes.isEmpty()) {
                    // Pick a random quote
                    Random random = new Random();
                    Quote randomQuote = quotes.get(random.nextInt(quotes.size()));

                    // Display the quote
                    textViewQuote.setText(randomQuote.getQuote());
                    textViewAuthor.setText("— " + randomQuote.getAuthor());
                }
            }

            @Override
            public void onError(String errorMessage) {
                textViewQuote.setText("Start your day with productivity!");
                textViewAuthor.setText("— MyAct");
                Toast.makeText(SplashActivity.this, "Error loading quote: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish(); // Close the splash screen so it's not in the back stack
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove any pending posts of callbacks and sent messages
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}