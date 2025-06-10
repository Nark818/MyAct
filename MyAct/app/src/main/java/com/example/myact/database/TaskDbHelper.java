package com.example.myact.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myact.api.models.Category;
import com.example.myact.api.models.Task;
import com.example.myact.utils.TaskFilterSettings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Database helper class to manage database operations for tasks and categories.
 * Last updated: 2025-06-10 15:08:31
 * By user: Nark818
 */
public class TaskDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 1;

    // Singleton instance
    private static TaskDbHelper instance;

    // Private constructor to prevent direct instantiation
    private TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Get singleton instance
    public static synchronized TaskDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TaskDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create categories table first (for foreign key constraint)
        db.execSQL(TaskContract.CategoryEntry.CREATE_TABLE);

        // Create tasks table
        db.execSQL(TaskContract.TaskEntry.CREATE_TABLE);

        // Insert default categories
        insertDefaultCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL(TaskContract.TaskEntry.DROP_TABLE);
        db.execSQL(TaskContract.CategoryEntry.DROP_TABLE);

        // Create tables again
        onCreate(db);
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        // Insert default categories
        for (String[] category : TaskContract.DEFAULT_CATEGORIES) {
            ContentValues values = new ContentValues();
            values.put(TaskContract.CategoryEntry.COLUMN_NAME, category[0]);
            values.put(TaskContract.CategoryEntry.COLUMN_COLOR, category[1]);
            db.insert(TaskContract.CategoryEntry.TABLE_NAME, null, values);
        }
    }

    // Method to get all tasks
    @SuppressLint("Range")
    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String selectQuery = "SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME +
                    " ORDER BY " + TaskContract.TaskEntry.COLUMN_DUE_DATE;
            cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    task.setId(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry._ID)));
                    task.setTitle(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TITLE)));
                    task.setDescription(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DESCRIPTION)));
                    task.setDueDate(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_DATE)));
                    task.setPriority(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_PRIORITY)));
                    task.setCategoryId(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_CATEGORY_ID)));
                    task.setCompleted(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_IS_COMPLETED)) == 1);
                    taskList.add(task);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return taskList;
    }

    // Method to get filtered tasks
    @SuppressLint("Range")
    public List<Task> getFilteredTasks(int statusFilter, int[] priorityFilter, int categoryFilter, int dueDateFilter) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Build the query
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM ").append(TaskContract.TaskEntry.TABLE_NAME).append(" WHERE 1=1");

            // Priority filter
            if (priorityFilter != null && priorityFilter.length > 0) {
                queryBuilder.append(" AND ").append(TaskContract.TaskEntry.COLUMN_PRIORITY).append(" IN (");
                for (int i = 0; i < priorityFilter.length; i++) {
                    queryBuilder.append(priorityFilter[i]);
                    if (i < priorityFilter.length - 1) {
                        queryBuilder.append(",");
                    }
                }
                queryBuilder.append(")");
            }

            // Status filter
            if (statusFilter == TaskFilterSettings.STATUS_ACTIVE) {
                queryBuilder.append(" AND ").append(TaskContract.TaskEntry.COLUMN_IS_COMPLETED).append(" = 0");
            } else if (statusFilter == TaskFilterSettings.STATUS_COMPLETED) {
                queryBuilder.append(" AND ").append(TaskContract.TaskEntry.COLUMN_IS_COMPLETED).append(" = 1");
            }

            // Category filter
            if (categoryFilter > 0) {
                queryBuilder.append(" AND ").append(TaskContract.TaskEntry.COLUMN_CATEGORY_ID)
                        .append(" = ").append(categoryFilter);
            }

            // Due date filter
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            if (dueDateFilter == TaskFilterSettings.DATE_TODAY) {
                String today = dateFormat.format(calendar.getTime());
                queryBuilder.append(" AND ").append(TaskContract.TaskEntry.COLUMN_DUE_DATE)
                        .append(" = '").append(today).append("'");
            } else if (dueDateFilter == TaskFilterSettings.DATE_THIS_WEEK) {
                String today = dateFormat.format(calendar.getTime());
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                String nextWeek = dateFormat.format(calendar.getTime());
                queryBuilder.append(" AND ").append(TaskContract.TaskEntry.COLUMN_DUE_DATE)
                        .append(" BETWEEN '").append(today).append("' AND '").append(nextWeek).append("'");
            } else if (dueDateFilter == TaskFilterSettings.DATE_OVERDUE) {
                String today = dateFormat.format(calendar.getTime());
                queryBuilder.append(" AND ").append(TaskContract.TaskEntry.COLUMN_DUE_DATE)
                        .append(" < '").append(today).append("'");
                queryBuilder.append(" AND ").append(TaskContract.TaskEntry.COLUMN_IS_COMPLETED).append(" = 0");
            }

            // Order by
            queryBuilder.append(" ORDER BY ").append(TaskContract.TaskEntry.COLUMN_DUE_DATE);

            // Execute query
            cursor = db.rawQuery(queryBuilder.toString(), null);

            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    task.setId(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry._ID)));
                    task.setTitle(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TITLE)));
                    task.setDescription(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DESCRIPTION)));
                    task.setDueDate(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_DATE)));
                    task.setPriority(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_PRIORITY)));
                    task.setCategoryId(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_CATEGORY_ID)));
                    task.setCompleted(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_IS_COMPLETED)) == 1);
                    taskList.add(task);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return taskList;
    }

    // Method to search tasks
    @SuppressLint("Range")
    public List<Task> searchTasks(String query) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String selectQuery = "SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME +
                    " WHERE " + TaskContract.TaskEntry.COLUMN_TITLE + " LIKE '%" + query + "%' OR " +
                    TaskContract.TaskEntry.COLUMN_DESCRIPTION + " LIKE '%" + query + "%'" +
                    " ORDER BY " + TaskContract.TaskEntry.COLUMN_DUE_DATE;
            cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    task.setId(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry._ID)));
                    task.setTitle(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TITLE)));
                    task.setDescription(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DESCRIPTION)));
                    task.setDueDate(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_DATE)));
                    task.setPriority(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_PRIORITY)));
                    task.setCategoryId(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_CATEGORY_ID)));
                    task.setCompleted(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_IS_COMPLETED)) == 1);
                    taskList.add(task);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return taskList;
    }

    // Method to get a single task by ID
    @SuppressLint("Range")
    public Task getTaskById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Task task = null;

        try {
            cursor = db.query(TaskContract.TaskEntry.TABLE_NAME, null,
                    TaskContract.TaskEntry._ID + " = ?",
                    new String[]{String.valueOf(id)}, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                task = new Task();
                task.setId(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry._ID)));
                task.setTitle(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TITLE)));
                task.setDescription(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DESCRIPTION)));
                task.setDueDate(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_DATE)));
                task.setPriority(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_PRIORITY)));
                task.setCategoryId(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_CATEGORY_ID)));
                task.setCompleted(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_IS_COMPLETED)) == 1);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return task;
    }

    // Method to add a new task
    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TaskContract.TaskEntry.COLUMN_TITLE, task.getTitle());
        values.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, task.getDescription());
        values.put(TaskContract.TaskEntry.COLUMN_DUE_DATE, task.getDueDate());
        values.put(TaskContract.TaskEntry.COLUMN_PRIORITY, task.getPriority());
        values.put(TaskContract.TaskEntry.COLUMN_CATEGORY_ID, task.getCategoryId());
        values.put(TaskContract.TaskEntry.COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);

        long id = db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
        return id;
    }

    // Method to update a task
    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TaskContract.TaskEntry.COLUMN_TITLE, task.getTitle());
        values.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, task.getDescription());
        values.put(TaskContract.TaskEntry.COLUMN_DUE_DATE, task.getDueDate());
        values.put(TaskContract.TaskEntry.COLUMN_PRIORITY, task.getPriority());
        values.put(TaskContract.TaskEntry.COLUMN_CATEGORY_ID, task.getCategoryId());
        values.put(TaskContract.TaskEntry.COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);

        return db.update(TaskContract.TaskEntry.TABLE_NAME, values,
                TaskContract.TaskEntry._ID + " = ?",
                new String[]{String.valueOf(task.getId())});
    }

    // Method to delete a task
    public int deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TaskContract.TaskEntry.TABLE_NAME,
                TaskContract.TaskEntry._ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // Method to get all categories
    @SuppressLint("Range")
    public List<Category> getAllCategories() {
        List<Category> categoryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String selectQuery = "SELECT * FROM " + TaskContract.CategoryEntry.TABLE_NAME +
                    " ORDER BY " + TaskContract.CategoryEntry.COLUMN_NAME;
            cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Category category = new Category();
                    category.setId(cursor.getInt(cursor.getColumnIndex(TaskContract.CategoryEntry._ID)));
                    category.setName(cursor.getString(cursor.getColumnIndex(TaskContract.CategoryEntry.COLUMN_NAME)));
                    category.setColor(cursor.getString(cursor.getColumnIndex(TaskContract.CategoryEntry.COLUMN_COLOR)));
                    categoryList.add(category);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return categoryList;
    }

    // Method to get a category by ID
    @SuppressLint("Range")
    public Category getCategoryById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Category category = null;

        try {
            cursor = db.query(TaskContract.CategoryEntry.TABLE_NAME, null,
                    TaskContract.CategoryEntry._ID + " = ?",
                    new String[]{String.valueOf(id)}, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                category = new Category();
                category.setId(cursor.getInt(cursor.getColumnIndex(TaskContract.CategoryEntry._ID)));
                category.setName(cursor.getString(cursor.getColumnIndex(TaskContract.CategoryEntry.COLUMN_NAME)));
                category.setColor(cursor.getString(cursor.getColumnIndex(TaskContract.CategoryEntry.COLUMN_COLOR)));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return category;
    }

    // Method to add a new category
    public long addCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TaskContract.CategoryEntry.COLUMN_NAME, category.getName());
        values.put(TaskContract.CategoryEntry.COLUMN_COLOR, category.getColor());

        long id = db.insert(TaskContract.CategoryEntry.TABLE_NAME, null, values);
        return id;
    }

    // Method to update a category
    public int updateCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TaskContract.CategoryEntry.COLUMN_NAME, category.getName());
        values.put(TaskContract.CategoryEntry.COLUMN_COLOR, category.getColor());

        return db.update(TaskContract.CategoryEntry.TABLE_NAME, values,
                TaskContract.CategoryEntry._ID + " = ?",
                new String[]{String.valueOf(category.getId())});
    }

    // Method to delete a category
    public int deleteCategory(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TaskContract.CategoryEntry.TABLE_NAME,
                TaskContract.CategoryEntry._ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // Statistics methods

    // Get total task count
    public int getTaskCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;

        try {
            String countQuery = "SELECT COUNT(*) FROM " + TaskContract.TaskEntry.TABLE_NAME;
            cursor = db.rawQuery(countQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    // Get completed task count
    public int getCompletedTaskCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;

        try {
            String countQuery = "SELECT COUNT(*) FROM " + TaskContract.TaskEntry.TABLE_NAME +
                    " WHERE " + TaskContract.TaskEntry.COLUMN_IS_COMPLETED + " = 1";
            cursor = db.rawQuery(countQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    // Get active task count
    public int getActiveTaskCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;

        try {
            String countQuery = "SELECT COUNT(*) FROM " + TaskContract.TaskEntry.TABLE_NAME +
                    " WHERE " + TaskContract.TaskEntry.COLUMN_IS_COMPLETED + " = 0";
            cursor = db.rawQuery(countQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    // Get task count by priority
    public int getTaskCountByPriority(int priority) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;

        try {
            String countQuery = "SELECT COUNT(*) FROM " + TaskContract.TaskEntry.TABLE_NAME +
                    " WHERE " + TaskContract.TaskEntry.COLUMN_PRIORITY + " = " + priority;
            cursor = db.rawQuery(countQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    // Get task count by category
    @SuppressLint("Range")
    public List<Object[]> getTaskCountByCategory() {
        List<Object[]> categoryStats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT c." + TaskContract.CategoryEntry._ID +
                    ", c." + TaskContract.CategoryEntry.COLUMN_NAME +
                    ", c." + TaskContract.CategoryEntry.COLUMN_COLOR +
                    ", COUNT(t." + TaskContract.TaskEntry._ID + ") as task_count " +
                    "FROM " + TaskContract.CategoryEntry.TABLE_NAME + " c " +
                    "LEFT JOIN " + TaskContract.TaskEntry.TABLE_NAME + " t " +
                    "ON c." + TaskContract.CategoryEntry._ID + " = t." + TaskContract.TaskEntry.COLUMN_CATEGORY_ID + " " +
                    "GROUP BY c." + TaskContract.CategoryEntry._ID + " " +
                    "ORDER BY task_count DESC";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Object[] stat = new Object[4];
                    stat[0] = cursor.getInt(cursor.getColumnIndex(TaskContract.CategoryEntry._ID));
                    stat[1] = cursor.getString(cursor.getColumnIndex(TaskContract.CategoryEntry.COLUMN_NAME));
                    stat[2] = cursor.getString(cursor.getColumnIndex(TaskContract.CategoryEntry.COLUMN_COLOR));
                    stat[3] = cursor.getInt(cursor.getColumnIndex("task_count"));
                    categoryStats.add(stat);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return categoryStats;
    }
}