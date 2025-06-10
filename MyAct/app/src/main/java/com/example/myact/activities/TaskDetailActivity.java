package com.example.myact.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myact.R;
import com.example.myact.adapters.CategorySelectAdapter;
import com.example.myact.api.models.Category;
import com.example.myact.api.models.Task;
import com.example.myact.database.TaskDbHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskDetailActivity extends AppCompatActivity implements CategorySelectAdapter.OnCategoryClickListener {

    private EditText editTextTitle;
    private EditText editTextDescription;
    private TextView textViewDueDate;
    private TextView textViewCategory;
    private Chip chipLow, chipMedium, chipHigh;
    private Button buttonSave, buttonDelete;
    private LinearLayout layoutDueDate, layoutCategory;

    private TaskDbHelper dbHelper;
    private Executor executor;
    private Handler mainHandler;

    private Task currentTask;
    private int selectedCategoryId = -1;
    private int selectedPriority = 2; // Default to medium
    private String selectedDueDate = null;

    private SimpleDateFormat dateFormat;
    private SimpleDateFormat displayDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        textViewDueDate = findViewById(R.id.text_view_due_date);
        textViewCategory = findViewById(R.id.text_view_category);
        chipLow = findViewById(R.id.chip_low);
        chipMedium = findViewById(R.id.chip_medium);
        chipHigh = findViewById(R.id.chip_high);
        buttonSave = findViewById(R.id.button_save);
        buttonDelete = findViewById(R.id.button_delete);
        layoutDueDate = findViewById(R.id.layout_due_date);
        layoutCategory = findViewById(R.id.layout_category);

        // Initialize database helper, executor and handler
        dbHelper = TaskDbHelper.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize date formatters
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        displayDateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

        // Get task ID from intent
        int taskId = getIntent().getIntExtra("TASK_ID", -1);

        if (taskId != -1) {
            // Edit existing task
            getSupportActionBar().setTitle(R.string.edit_task);
            buttonDelete.setVisibility(View.VISIBLE);
            loadTask(taskId);
        } else {
            // Create new task
            getSupportActionBar().setTitle(R.string.new_task);
            buttonDelete.setVisibility(View.GONE);
            currentTask = new Task();

            // Set default due date to today
            Calendar calendar = Calendar.getInstance();
            selectedDueDate = dateFormat.format(calendar.getTime());
            textViewDueDate.setText(displayDateFormat.format(calendar.getTime()));

            // Set default category
            loadDefaultCategory();
        }

        // Set up click listeners
        setupClickListeners();
    }

    private void loadTask(int taskId) {
        executor.execute(() -> {
            final Task task = dbHelper.getTaskById(taskId);

            if (task != null) {
                mainHandler.post(() -> {
                    currentTask = task;

                    // Populate form fields
                    editTextTitle.setText(task.getTitle());
                    editTextDescription.setText(task.getDescription());

                    // Set due date
                    String dueDate = task.getDueDate();
                    if (dueDate != null && !dueDate.isEmpty()) {
                        selectedDueDate = dueDate;
                        try {
                            Date date = dateFormat.parse(dueDate);
                            textViewDueDate.setText(displayDateFormat.format(date));
                        } catch (ParseException e) {
                            textViewDueDate.setText(dueDate);
                        }
                    }

                    // Set priority
                    int priority = task.getPriority();
                    selectedPriority = priority;
                    chipLow.setChecked(priority == 1);
                    chipMedium.setChecked(priority == 2);
                    chipHigh.setChecked(priority == 3);

                    // Set category
                    selectedCategoryId = task.getCategoryId();
                    loadCategoryName(selectedCategoryId);
                });
            }
        });
    }

    private void loadDefaultCategory() {
        executor.execute(() -> {
            List<Category> categories = dbHelper.getAllCategories();

            if (categories != null && !categories.isEmpty()) {
                Category defaultCategory = categories.get(0);
                selectedCategoryId = defaultCategory.getId();

                mainHandler.post(() -> {
                    textViewCategory.setText(defaultCategory.getName());
                });
            }
        });
    }

    private void loadCategoryName(int categoryId) {
        executor.execute(() -> {
            Category category = dbHelper.getCategoryById(categoryId);

            if (category != null) {
                mainHandler.post(() -> {
                    textViewCategory.setText(category.getName());
                });
            }
        });
    }

    private void setupClickListeners() {
        // Priority chips
        chipLow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedPriority = 1;
                chipMedium.setChecked(false);
                chipHigh.setChecked(false);
            } else if (!chipMedium.isChecked() && !chipHigh.isChecked()) {
                // Ensure at least one chip is selected
                chipLow.setChecked(true);
            }
        });

        chipMedium.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedPriority = 2;
                chipLow.setChecked(false);
                chipHigh.setChecked(false);
            } else if (!chipLow.isChecked() && !chipHigh.isChecked()) {
                chipMedium.setChecked(true);
            }
        });

        chipHigh.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedPriority = 3;
                chipLow.setChecked(false);
                chipMedium.setChecked(false);
            } else if (!chipLow.isChecked() && !chipMedium.isChecked()) {
                chipHigh.setChecked(true);
            }
        });

        // Due date
        layoutDueDate.setOnClickListener(v -> showDatePicker());

        // Category
        layoutCategory.setOnClickListener(v -> showCategorySelectionDialog());

        // Save button
        buttonSave.setOnClickListener(v -> saveTask());

        // Delete button
        buttonDelete.setOnClickListener(v -> confirmDelete());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        try {
            if (selectedDueDate != null) {
                Date date = dateFormat.parse(selectedDueDate);
                calendar.setTime(date);
            }
        } catch (ParseException e) {
            // Use current date if parsing fails
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    selectedDueDate = dateFormat.format(calendar.getTime());
                    textViewDueDate.setText(displayDateFormat.format(calendar.getTime()));
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void showCategorySelectionDialog() {
        executor.execute(() -> {
            final List<Category> categories = dbHelper.getAllCategories();

            mainHandler.post(() -> {
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_category, null);
                RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_view_categories);
                Button buttonAddCategory = dialogView.findViewById(R.id.button_add_category);
                Button buttonCancel = dialogView.findViewById(R.id.button_cancel);
                Button buttonOk = dialogView.findViewById(R.id.button_ok);

                // Setup RecyclerView
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                CategorySelectAdapter adapter = new CategorySelectAdapter(this, categories, selectedCategoryId);
                adapter.setOnCategoryClickListener(this);
                recyclerView.setAdapter(adapter);

                // Create dialog
                Dialog dialog = new MaterialAlertDialogBuilder(this)
                        .setView(dialogView)
                        .create();

                // Set button click listeners
                buttonAddCategory.setOnClickListener(v -> {
                    showAddCategoryDialog(dialog);
                });

                buttonCancel.setOnClickListener(v -> dialog.dismiss());

                buttonOk.setOnClickListener(v -> {
                    dialog.dismiss();
                    loadCategoryName(selectedCategoryId);
                });

                dialog.show();
            });
        });
    }

    private void showAddCategoryDialog(Dialog parentDialog) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);

        // Get references to views
        TextInputLayout tilCategoryName = dialogView.findViewById(R.id.til_category_name);
        ChipGroup chipGroupColors = dialogView.findViewById(R.id.chip_group_colors);
        View viewSelectedColor = dialogView.findViewById(R.id.view_selected_color);
        TextView textViewSelectedColor = dialogView.findViewById(R.id.text_view_selected_color);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);
        Button buttonSave = dialogView.findViewById(R.id.button_save);

        // Create dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        // Use an array to store the selected color (effectively final)
        final String[] selectedColorRef = {"#2196F3"}; // Default color

        // Setup color selection
        for (int i = 0; i < chipGroupColors.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupColors.getChildAt(i);
            final String color = chip.getTag().toString();

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Update the color preview
                    viewSelectedColor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
                    textViewSelectedColor.setText(color);

                    // Store selected color in the array
                    selectedColorRef[0] = color;
                }
            });
        }

        // Check the first chip by default
        ((Chip) chipGroupColors.getChildAt(0)).setChecked(true);

        // Set button click listeners
        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonSave.setOnClickListener(v -> {
            String categoryName = tilCategoryName.getEditText().getText().toString().trim();

            if (categoryName.isEmpty()) {
                tilCategoryName.setError(getString(R.string.category_name_required));
                return;
            }

            // Get the selected color from the array
            final String finalSelectedColor = selectedColorRef[0];

            // Save category to database
            executor.execute(() -> {
                Category newCategory = new Category();
                newCategory.setName(categoryName);
                newCategory.setColor(finalSelectedColor);

                long categoryId = dbHelper.addCategory(newCategory);
                selectedCategoryId = (int) categoryId;

                // Refresh the parent dialog with the new category
                mainHandler.post(() -> {
                    dialog.dismiss();
                    parentDialog.dismiss();
                    loadCategoryName(selectedCategoryId);
                });
            });
        });

        dialog.show();
    }

    private void saveTask() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (title.isEmpty()) {
            ((TextInputLayout) findViewById(R.id.til_title)).setError(getString(R.string.title_required));
            return;
        }

        // Update task object
        currentTask.setTitle(title);
        currentTask.setDescription(description);
        currentTask.setDueDate(selectedDueDate);
        currentTask.setPriority(selectedPriority);
        currentTask.setCategoryId(selectedCategoryId);

        // Save to database
        executor.execute(() -> {
            boolean success = false;

            if (currentTask.getId() > 0) {
                // Update existing task
                success = dbHelper.updateTask(currentTask) > 0;
            } else {
                // Add new task
                long taskId = dbHelper.addTask(currentTask);
                success = taskId > 0;
            }

            final boolean finalSuccess = success;
            mainHandler.post(() -> {
                if (finalSuccess) {
                    Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, R.string.error_saving_task, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.delete_task_confirmation)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteTask())
                .show();
    }

    private void deleteTask() {
        executor.execute(() -> {
            boolean success = dbHelper.deleteTask(currentTask.getId()) > 0;

            mainHandler.post(() -> {
                if (success) {
                    Toast.makeText(this, R.string.task_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, R.string.error_deleting_task, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCategoryClick(Category category) {
        selectedCategoryId = category.getId();
    }
}