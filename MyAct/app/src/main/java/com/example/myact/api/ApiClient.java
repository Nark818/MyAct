package com.example.myact.api;

import android.os.Handler;
import android.os.Looper;

import com.example.myact.api.models.GiphyResponse;
import com.example.myact.api.models.Quote;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ApiClient implements ApiService {

    private static final String QUOTES_API_URL = "https://zenquotes.io/api/quotes";
    private static final String GIPHY_API_URL = "https://api.giphy.com/v1/gifs/search";
    private static final String GIPHY_API_KEY = "HMObBUWlQiFs1F7A72Gr8hQwgZAvCp8y"; // Replace with your Giphy API key

    private final Executor executor;
    private final Handler mainHandler;
    private final Gson gson;

    public ApiClient() {
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        gson = new Gson();
    }

    @Override
    public void getQuotes(QuoteCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(QUOTES_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    Type quoteListType = new TypeToken<List<Quote>>(){}.getType();
                    List<Quote> quotes = gson.fromJson(response.toString(), quoteListType);

                    // Deliver result on main thread
                    mainHandler.post(() -> callback.onSuccess(quotes));
                } else {
                    throw new IOException("HTTP Error: " + responseCode);
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    @Override
    public void getRandomGif(String query, GiphyCallback callback) {
        executor.execute(() -> {
            try {
                String urlString = GIPHY_API_URL +
                        "?api_key=" + GIPHY_API_KEY +
                        "&q=" + query +
                        "&limit=25&offset=0&rating=g&lang=en";

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    GiphyResponse giphyResponse = gson.fromJson(response.toString(), GiphyResponse.class);

                    // Deliver result on main thread
                    mainHandler.post(() -> callback.onSuccess(giphyResponse));
                } else {
                    throw new IOException("HTTP Error: " + responseCode);
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
}