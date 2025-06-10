package com.example.myact.database;

import android.provider.BaseColumns;

/**
 * Contract class for the database schema.
 * Contains definitions for all table names and column names used in the database.
 * Last updated: 2025-06-10 15:08:31
 * By user: Nark818
 */
public final class TaskContract {
    // Private constructor to prevent instantiation
    private TaskContract() {}

    // Tasks table schema
    public static class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "tasks";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DUE_DATE = "due_date";
        public static final String COLUMN_PRIORITY = "priority";
        public static final String COLUMN_IS_COMPLETED = "completed"; // Changed to match TaskDbHelper
        public static final String COLUMN_CATEGORY_ID = "category_id";

        // SQL statement for creating the tasks table
        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_TITLE + " TEXT," +
                        COLUMN_DESCRIPTION + " TEXT," +
                        COLUMN_DUE_DATE + " TEXT," +
                        COLUMN_PRIORITY + " INTEGER," +
                        COLUMN_CATEGORY_ID + " INTEGER," +
                        COLUMN_IS_COMPLETED + " INTEGER DEFAULT 0," +
                        "FOREIGN KEY(" + COLUMN_CATEGORY_ID + ") REFERENCES " +
                        CategoryEntry.TABLE_NAME + "(" + CategoryEntry._ID + ")" +
                        ")";

        // SQL statement for dropping the tasks table
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // Categories table schema
    public static class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "categories";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_COLOR = "color";

        // SQL statement for creating the categories table
        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME + " TEXT," +
                        COLUMN_COLOR + " TEXT" +
                        ")";

        // SQL statement for dropping the categories table
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // Default categories data
    public static final String[][] DEFAULT_CATEGORIES = {
            {"Work", "#2196F3"},
            {"Personal", "#4CAF50"},
            {"Health", "#F44336"},
            {"Study", "#9C27B0"},
            {"Finance", "#FF9800"}
    };
}