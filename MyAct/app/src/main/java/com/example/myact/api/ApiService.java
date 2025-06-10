package com.example.myact.api;

import com.example.myact.api.models.GiphyResponse;
import com.example.myact.api.models.Quote;

import java.util.List;

public interface ApiService {
    interface QuoteCallback {
        void onSuccess(List<Quote> quotes);
        void onError(String errorMessage);
    }

    interface GiphyCallback {
        void onSuccess(GiphyResponse giphyResponse);
        void onError(String errorMessage);
    }

    void getQuotes(QuoteCallback callback);
    void getRandomGif(String query, GiphyCallback callback);
}