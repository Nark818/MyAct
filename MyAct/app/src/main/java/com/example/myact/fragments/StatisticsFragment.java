package com.example.myact.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myact.R;
import com.example.myact.adapters.CategoryStatAdapter;
import com.example.myact.database.TaskDbHelper;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StatisticsFragment extends Fragment {

    private TextView textViewDate;
    private TextView textViewTotalTasks;
    private TextView textViewCompletedTasks;
    private TextView textViewActiveTasks;
    private TextView textViewHighPriority;
    private TextView textViewMediumPriority;
    private TextView textViewLowPriority;
    private LinearProgressIndicator progressBarCompleted;
    private TextView textViewCompletionPercentage;
    private RecyclerView recyclerViewCategories;

    private TaskDbHelper dbHelper;
    private Executor executor;
    private Handler mainHandler;
    private CategoryStatAdapter categoryStatAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Initialize views
        textViewDate = view.findViewById(R.id.text_view_date);
        textViewTotalTasks = view.findViewById(R.id.text_view_total_tasks);
        textViewCompletedTasks = view.findViewById(R.id.text_view_completed_tasks);
        textViewActiveTasks = view.findViewById(R.id.text_view_active_tasks);
        textViewHighPriority = view.findViewById(R.id.text_view_high_priority);
        textViewMediumPriority = view.findViewById(R.id.text_view_medium_priority);
        textViewLowPriority = view.findViewById(R.id.text_view_low_priority);
        progressBarCompleted = view.findViewById(R.id.progress_bar_completed);
        textViewCompletionPercentage = view.findViewById(R.id.text_view_completion_percentage);
        recyclerViewCategories = view.findViewById(R.id.recycler_view_categories);

        // Initialize database helper, executor and handler
        dbHelper = TaskDbHelper.getInstance(getContext());
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Setup RecyclerView for category statistics
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryStatAdapter = new CategoryStatAdapter(getContext());
        recyclerViewCategories.setAdapter(categoryStatAdapter);

        // Display current date
        displayCurrentDate();

        // Load statistics
        loadStatistics();

        return view;
    }

    private void displayCurrentDate() {
        // Display the user-provided current date
        textViewDate.setText("2025-06-10 14:42:45");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload statistics when returning to this fragment
        loadStatistics();
    }

    private void loadStatistics() {
        executor.execute(() -> {
            // Get task counts
            final int totalTasks = dbHelper.getTaskCount();
            final int completedTasks = dbHelper.getCompletedTaskCount();
            final int activeTasks = dbHelper.getActiveTaskCount();

            // Get priority counts
            final int highPriorityTasks = dbHelper.getTaskCountByPriority(3);
            final int mediumPriorityTasks = dbHelper.getTaskCountByPriority(2);
            final int lowPriorityTasks = dbHelper.getTaskCountByPriority(1);

            // Calculate completion percentage
            final int completionPercentage = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;

            // Get category statistics
            List<Object[]> categoryData = dbHelper.getTaskCountByCategory();
            final List<CategoryStatAdapter.CategoryStat> categoryStats = new ArrayList<>();

            for (Object[] data : categoryData) {
                String categoryName = (String) data[1];
                String categoryColor = (String) data[2];
                int taskCount = (int) data[3];
                categoryStats.add(new CategoryStatAdapter.CategoryStat(categoryName, categoryColor, taskCount));
            }

            // Update UI on main thread
            mainHandler.post(() -> {
                // Update task counts
                textViewTotalTasks.setText(String.valueOf(totalTasks));
                textViewCompletedTasks.setText(String.valueOf(completedTasks));
                textViewActiveTasks.setText(String.valueOf(activeTasks));

                // Update priority counts
                textViewHighPriority.setText(String.valueOf(highPriorityTasks));
                textViewMediumPriority.setText(String.valueOf(mediumPriorityTasks));
                textViewLowPriority.setText(String.valueOf(lowPriorityTasks));

                // Update completion percentage
                progressBarCompleted.setProgress(completionPercentage);
                textViewCompletionPercentage.setText(completionPercentage + "%");

                // Update category statistics
                categoryStatAdapter.setCategoryStats(categoryStats);
            });
        });
    }
}