package com.example.myact.api.models;

public class Quote {
    private String q;  // quote text
    private String a;  // author
    private String h;  // HTML format

    public Quote() {
    }

    public String getQuote() {
        return q;
    }

    public String getAuthor() {
        return a;
    }

    public String getHtml() {
        return h;
    }

    @Override
    public String toString() {
        return "\"" + q + "\" - " + a;
    }
}