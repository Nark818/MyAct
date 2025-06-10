package com.example.myact.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myact.R;
import com.example.myact.api.models.Category;
import com.example.myact.api.models.Task;
import com.example.myact.database.TaskDbHelper;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks = new ArrayList<>();
    private Context context;
    private OnTaskClickListener listener;
    private TaskDbHelper dbHelper;
    private SimpleDateFormat inputDateFormat;
    private SimpleDateFormat outputDateFormat;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskStatusChanged(Task task);
    }

    public TaskAdapter(Context context) {
        this.context = context;
        dbHelper = TaskDbHelper.getInstance(context);
        inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        outputDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = tasks.get(position);

        // Set task title
        holder.textViewTaskTitle.setText(currentTask.getTitle());

        // Set checkbox state
        holder.checkboxCompleted.setChecked(currentTask.isCompleted());

        // Apply strikethrough if task is completed
        if (currentTask.isCompleted()) {
            holder.textViewTaskTitle.setPaintFlags(
                    holder.textViewTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.textViewTaskTitle.setPaintFlags(
                    holder.textViewTaskTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        // Set due date
        String dueDate = currentTask.getDueDate();
        if (dueDate != null && !dueDate.isEmpty()) {
            try {
                Date date = inputDateFormat.parse(dueDate);
                holder.textViewDueDate.setText(outputDateFormat.format(date));

                // Highlight overdue tasks
                Date today = new Date();
                if (!currentTask.isCompleted() && date.before(today)) {
                    holder.textViewDueDate.setTextColor(context.getResources().getColor(R.color.overdue));
                } else {
                    holder.textViewDueDate.setTextColor(context.getResources().getColor(R.color.text_secondary_light));
                }
            } catch (ParseException e) {
                holder.textViewDueDate.setText(dueDate);
            }
        } else {
            holder.textViewDueDate.setText(R.string.no_due_date);
        }

        // Set category
        Category category = dbHelper.getCategoryById(currentTask.getCategoryId());
        if (category != null) {
            holder.textViewCategory.setText(category.getName());
            try {
                holder.textViewCategory.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor(category.getColor())));
            } catch (Exception e) {
                holder.textViewCategory.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(context.getResources().getColor(R.color.colorAccent)));
            }
            holder.textViewCategory.setVisibility(View.VISIBLE);
        } else {
            holder.textViewCategory.setVisibility(View.GONE);
        }

        // Set priority indicator color
        int priorityColor;
        switch (currentTask.getPriority()) {
            case 3: // High
                priorityColor = context.getResources().getColor(R.color.priority_high);
                break;
            case 2: // Medium
                priorityColor = context.getResources().getColor(R.color.priority_medium);
                break;
            case 1: // Low
                priorityColor = context.getResources().getColor(R.color.priority_low);
                break;
            default:
                priorityColor = context.getResources().getColor(R.color.colorAccent);
                break;
        }
        holder.viewPriorityIndicator.setBackgroundColor(priorityColor);

        // Set click listeners
        holder.cardViewTask.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(currentTask);
            }
        });

        holder.checkboxCompleted.setOnClickListener(v -> {
            // Toggle completed status
            currentTask.setCompleted(holder.checkboxCompleted.isChecked());

            // Notify listener
            if (listener != null) {
                listener.onTaskStatusChanged(currentTask);
            }

            // Update UI
            if (holder.checkboxCompleted.isChecked()) {
                holder.textViewTaskTitle.setPaintFlags(
                        holder.textViewTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.textViewTaskTitle.setPaintFlags(
                        holder.textViewTaskTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardViewTask;
        View viewPriorityIndicator;
        CheckBox checkboxCompleted;
        TextView textViewTaskTitle;
        TextView textViewDueDate;
        TextView textViewCategory;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewTask = (MaterialCardView) itemView;
            viewPriorityIndicator = itemView.findViewById(R.id.view_priority_indicator);
            checkboxCompleted = itemView.findViewById(R.id.checkbox_completed);
            textViewTaskTitle = itemView.findViewById(R.id.text_view_task_title);
            textViewDueDate = itemView.findViewById(R.id.text_view_due_date);
            textViewCategory = itemView.findViewById(R.id.text_view_category);
        }
    }
}