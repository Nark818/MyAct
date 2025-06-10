package com.example.myact.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.myact.R;
import com.example.myact.api.ApiClient;
import com.example.myact.api.ApiService;
import com.example.myact.api.models.GiphyData;
import com.example.myact.api.models.GiphyResponse;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Random;

public class RewardActivity extends AppCompatActivity {

    private ImageView imageViewGif;
    private ProgressBar progressBar;
    private MaterialButton buttonContinue;
    private TextView textViewCurrentDate;
    private ApiService apiService;

    // Celebratory search terms for Giphy
    private final String[] celebrationTerms = {
            "celebration",
            "congratulations",
            "success",
            "achievement",
            "victory",
            "applause",
            "cheers"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);

        // Initialize views
        imageViewGif = findViewById(R.id.image_view_gif);
        progressBar = findViewById(R.id.progress_bar);
        buttonContinue = findViewById(R.id.button_continue); // Changed from button_close to button_continue

        // Initialize API service
        apiService = new ApiClient();

        // Load a random celebratory GIF
        loadRandomGif();

        // Setup continue button instead of close button
        buttonContinue.setOnClickListener(v -> finish());
    }

    private void loadRandomGif() {
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);

        // Select a random celebration term
        Random random = new Random();
        String searchTerm = celebrationTerms[random.nextInt(celebrationTerms.length)];

        // Fetch a GIF from Giphy API
        apiService.getRandomGif(searchTerm, new ApiService.GiphyCallback() {
            @Override
            public void onSuccess(GiphyResponse giphyResponse) {
                List<GiphyData> gifs = giphyResponse.getData();
                if (gifs != null && !gifs.isEmpty()) {
                    // Pick a random GIF from results
                    GiphyData randomGif = gifs.get(random.nextInt(gifs.size()));
                    String gifUrl = randomGif.getImages().getFixedHeight().getUrl();

                    // Load the GIF with Glide
                    Glide.with(RewardActivity.this)
                            .asGif()
                            .load(gifUrl)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageViewGif);

                    // Hide loading indicator
                    progressBar.setVisibility(View.GONE);
                } else {
                    onError("No GIFs found");
                }
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RewardActivity.this, "Error loading GIF: " + errorMessage, Toast.LENGTH_SHORT).show();

                // Load a default image or icon as fallback
                imageViewGif.setImageResource(R.drawable.ic_launcher_foreground);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}