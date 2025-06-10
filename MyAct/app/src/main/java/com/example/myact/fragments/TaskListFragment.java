package com.example.myact.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myact.R;
import com.example.myact.activities.RewardActivity;
import com.example.myact.activities.TaskDetailActivity;
import com.example.myact.adapters.TaskAdapter;
import com.example.myact.api.models.Category;
import com.example.myact.api.models.Task;
import com.example.myact.database.TaskDbHelper;
import com.example.myact.utils.NetworkUtils;
import com.example.myact.utils.TaskFilterSettings;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskListFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerViewTasks;
    private TaskAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout emptyStateLayout;
    private ExtendedFloatingActionButton fabAddTask;
    private MenuItem filterMenuItem;
    private BadgeDrawable filterBadge;

    private TaskDbHelper dbHelper;
    private Executor executor;
    private Handler mainHandler;
    private TaskFilterSettings filterSettings;
    private List<Category> categories;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Enable options menu in fragment
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        // Initialize views
        recyclerViewTasks = view.findViewById(R.id.recycler_view_tasks);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        emptyStateLayout = view.findViewById(R.id.layout_empty_state);
        fabAddTask = view.findViewById(R.id.fab_add_task);

        // Setup RecyclerView
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(getContext());
        adapter.setOnTaskClickListener(this);
        recyclerViewTasks.setAdapter(adapter);

        // Initialize database helper, executor and handler
        dbHelper = TaskDbHelper.getInstance(getContext());
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        filterSettings = new TaskFilterSettings();

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshTasks);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        // Setup FAB
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
            startActivity(intent);
        });

        // Load categories (needed for filtering)
        loadCategories();

        // Load tasks
        loadTasks();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task_list, menu);

        // Setup search functionality
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_tasks));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterSettings.setSearchQuery(query);
                loadTasks();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty() && !filterSettings.getSearchQuery().isEmpty()) {
                    filterSettings.setSearchQuery("");
                    loadTasks();
                }
                return false;
            }
        });

        // Store filter menu item for badge
        filterMenuItem = menu.findItem(R.id.action_filter);
        updateFilterBadge();

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filter) {
            showFilterDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload tasks when returning to this fragment
        loadTasks();
    }

    private void loadCategories() {
        executor.execute(() -> {
            categories = dbHelper.getAllCategories();
        });
    }

    private void loadTasks() {
        // Start the refresh indicator
        swipeRefreshLayout.setRefreshing(true);

        // Use a background thread to get tasks from database
        executor.execute(() -> {
            final List<Task> tasks;

            if (!filterSettings.getSearchQuery().isEmpty()) {
                tasks = dbHelper.searchTasks(filterSettings.getSearchQuery());
            } else {
                tasks = dbHelper.getFilteredTasks(
                        filterSettings.getStatusFilter(),
                        filterSettings.getPriorityFilter(),
                        filterSettings.getCategoryFilter(),
                        filterSettings.getDueDateFilter()
                );
            }

            // Update UI on main thread
            mainHandler.post(() -> {
                adapter.setTasks(tasks);
                swipeRefreshLayout.setRefreshing(false);

                // Show/hide empty state
                if (tasks.isEmpty()) {
                    recyclerViewTasks.setVisibility(View.GONE);
                    emptyStateLayout.setVisibility(View.VISIBLE);
                } else {
                    recyclerViewTasks.setVisibility(View.VISIBLE);
                    emptyStateLayout.setVisibility(View.GONE);
                }

                // Update filter badge
                updateFilterBadge();
            });
        });
    }

    private void refreshTasks() {
        // Check for network connectivity
        if (NetworkUtils.isNetworkAvailable(getContext())) {
            // In a real app, we would fetch data from an API here
            // For now, just simulate a delay and show a message
            new Handler().postDelayed(() -> {
                Toast.makeText(getContext(), "Task list refreshed", Toast.LENGTH_SHORT).show();
                loadTasks();
            }, 1500);
        } else {
            Toast.makeText(getContext(), "No internet connection. Showing saved tasks.", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void updateFilterBadge() {
        if (getActivity() == null || filterMenuItem == null) return;

        if (filterSettings.hasActiveFilters()) {
            // Create badge if it doesn't exist
            if (filterBadge == null) {
                filterBadge = BadgeDrawable.create(getContext());
                filterBadge.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                filterBadge.setBadgeGravity(BadgeDrawable.TOP_END);
                filterBadge.setVisible(true);

                // Attach badge to menu item - depends on MaterialComponents theme
                try {
                    filterMenuItem.setIcon(R.drawable.ic_filter_alt_active);
                } catch (Exception e) {
                    // Fallback if setting icon fails
                    filterMenuItem.setIcon(R.drawable.ic_filter_alt);
                }
            }
        } else {
            // Remove badge
            if (filterBadge != null) {
                filterBadge.setVisible(false);
                filterBadge = null;
                filterMenuItem.setIcon(R.drawable.ic_filter_alt);
            }
        }
    }

    private void showFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter_tasks, null);

        // Get references to dialog views
        ChipGroup chipGroupStatus = dialogView.findViewById(R.id.chip_group_status);
        Chip chipAll = dialogView.findViewById(R.id.chip_all);
        Chip chipActive = dialogView.findViewById(R.id.chip_active);
        Chip chipCompleted = dialogView.findViewById(R.id.chip_completed);

        ChipGroup chipGroupPriority = dialogView.findViewById(R.id.chip_group_priority);
        Chip chipLow = dialogView.findViewById(R.id.chip_low);
        Chip chipMedium = dialogView.findViewById(R.id.chip_medium);
        Chip chipHigh = dialogView.findViewById(R.id.chip_high);

        AutoCompleteTextView dropdownCategory = dialogView.findViewById(R.id.dropdown_category);
        RadioGroup radioGroupDueDate = dialogView.findViewById(R.id.radio_group_due_date);

        Button buttonReset = dialogView.findViewById(R.id.button_reset);
        Button buttonApply = dialogView.findViewById(R.id.button_apply);

        // Set up current filter values
        // Status
        switch (filterSettings.getStatusFilter()) {
            case TaskFilterSettings.STATUS_ALL:
                chipAll.setChecked(true);
                break;
            case TaskFilterSettings.STATUS_ACTIVE:
                chipActive.setChecked(true);
                break;
            case TaskFilterSettings.STATUS_COMPLETED:
                chipCompleted.setChecked(true);
                break;
        }

        // Priority
        int[] priorities = filterSettings.getPriorityFilter();
        boolean hasLow = false, hasMedium = false, hasHigh = false;
        for (int priority : priorities) {
            if (priority == 1) hasLow = true;
            if (priority == 2) hasMedium = true;
            if (priority == 3) hasHigh = true;
        }
        chipLow.setChecked(hasLow);
        chipMedium.setChecked(hasMedium);
        chipHigh.setChecked(hasHigh);

        // Category dropdown
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add(getString(R.string.all_categories));

        if (categories != null) {
            for (Category category : categories) {
                categoryNames.add(category.getName());
            }

            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    categoryNames
            );
            dropdownCategory.setAdapter(categoryAdapter);

            // Set selected category
            int categoryPosition = 0; // Default to "All Categories"
            int categoryFilter = filterSettings.getCategoryFilter();
            if (categoryFilter > 0) {
                // Find position of category with the same id
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).getId() == categoryFilter) {
                        categoryPosition = i + 1; // +1 for "All Categories" option
                        break;
                    }
                }
                dropdownCategory.setText(categoryNames.get(categoryPosition));
            } else {
                dropdownCategory.setText(categoryNames.get(0));
            }
        }

        // Due date
        switch (filterSettings.getDueDateFilter()) {
            case TaskFilterSettings.DATE_ALL:
                radioGroupDueDate.check(R.id.radio_all_dates);
                break;
            case TaskFilterSettings.DATE_TODAY:
                radioGroupDueDate.check(R.id.radio_today);
                break;
            case TaskFilterSettings.DATE_THIS_WEEK:
                radioGroupDueDate.check(R.id.radio_this_week);
                break;
            case TaskFilterSettings.DATE_OVERDUE:
                radioGroupDueDate.check(R.id.radio_overdue);
                break;
        }

        // Create and show the dialog
        Dialog dialog = new MaterialAlertDialogBuilder(getContext())
                .setView(dialogView)
                .create();

        dialog.show();

        // Set button click listeners
        buttonReset.setOnClickListener(v -> {
            filterSettings.reset();
            loadTasks();
            dialog.dismiss();
        });

        buttonApply.setOnClickListener(v -> {
            // Get status filter
            int statusFilter = TaskFilterSettings.STATUS_ALL;
            if (chipActive.isChecked()) {
                statusFilter = TaskFilterSettings.STATUS_ACTIVE;
            } else if (chipCompleted.isChecked()) {
                statusFilter = TaskFilterSettings.STATUS_COMPLETED;
            }
            filterSettings.setStatusFilter(statusFilter);

            // Get priority filter
            List<Integer> selectedPriorities = new ArrayList<>();
            if (chipLow.isChecked()) selectedPriorities.add(1);
            if (chipMedium.isChecked()) selectedPriorities.add(2);
            if (chipHigh.isChecked()) selectedPriorities.add(3);

            int[] priorityFilter = new int[selectedPriorities.size()];
            for (int i = 0; i < selectedPriorities.size(); i++) {
                priorityFilter[i] = selectedPriorities.get(i);
            }
            filterSettings.setPriorityFilter(priorityFilter);

            // Get category filter
            String selectedCategory = dropdownCategory.getText().toString();
            int categoryFilter = -1; // All categories

            if (!selectedCategory.equals(getString(R.string.all_categories)) && categories != null) {
                for (Category category : categories) {
                    if (category.getName().equals(selectedCategory)) {
                        categoryFilter = category.getId();
                        break;
                    }
                }
            }
            filterSettings.setCategoryFilter(categoryFilter);

            // Get due date filter
            int dueDateFilter;
            int dueDateId = radioGroupDueDate.getCheckedRadioButtonId();
            if (dueDateId == R.id.radio_today) {
                dueDateFilter = TaskFilterSettings.DATE_TODAY;
            } else if (dueDateId == R.id.radio_this_week) {
                dueDateFilter = TaskFilterSettings.DATE_THIS_WEEK;
            } else if (dueDateId == R.id.radio_overdue) {
                dueDateFilter = TaskFilterSettings.DATE_OVERDUE;
            } else {
                dueDateFilter = TaskFilterSettings.DATE_ALL;
            }
            filterSettings.setDueDateFilter(dueDateFilter);

            // Apply filters and close dialog
            loadTasks();
            dialog.dismiss();
        });
    }

    @Override
    public void onTaskClick(Task task) {
        // Launch TaskDetailActivity to edit the selected task
        Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        startActivity(intent);
    }

    @Override
    public void onTaskStatusChanged(Task task) {
        // Update the task status in the database
        executor.execute(() -> {
            int rowsAffected = dbHelper.updateTask(task);

            mainHandler.post(() -> {
                if (rowsAffected > 0) {
                    // If the task is marked as completed, show reward
                    if (task.isCompleted()) {
                        Intent intent = new Intent(getActivity(), RewardActivity.class);
                        intent.putExtra("TASK_TITLE", task.getTitle());
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Task marked as active", Toast.LENGTH_SHORT).show();
                    }

                    // Reload tasks to apply any filters
                    loadTasks();
                }
            });
        });
    }
}