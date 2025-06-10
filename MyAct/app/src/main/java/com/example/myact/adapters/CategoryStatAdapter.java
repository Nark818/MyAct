package com.example.myact.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myact.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryStatAdapter extends RecyclerView.Adapter<CategoryStatAdapter.CategoryStatViewHolder> {

    private List<CategoryStat> categoryStats = new ArrayList<>();
    private Context context;

    // Class to hold category statistics data
    public static class CategoryStat {
        private String name;
        private String color;
        private int count;

        public CategoryStat(String name, String color, int count) {
            this.name = name;
            this.color = color;
            this.count = count;
        }

        public String getName() {
            return name;
        }

        public String getColor() {
            return color;
        }

        public int getCount() {
            return count;
        }
    }

    public CategoryStatAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public CategoryStatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_stat, parent, false);
        return new CategoryStatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryStatViewHolder holder, int position) {
        CategoryStat stat = categoryStats.get(position);
        holder.textViewCategoryName.setText(stat.getName());
        holder.textViewTaskCount.setText(String.valueOf(stat.getCount()));

        // Set color indicator
        try {
            holder.viewCategoryColor.setBackgroundColor(Color.parseColor(stat.getColor()));
        } catch (Exception e) {
            holder.viewCategoryColor.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() {
        return categoryStats.size();
    }

    public void setCategoryStats(List<CategoryStat> stats) {
        this.categoryStats = stats;
        notifyDataSetChanged();
    }

    public static class CategoryStatViewHolder extends RecyclerView.ViewHolder {
        View viewCategoryColor;
        TextView textViewCategoryName;
        TextView textViewTaskCount;

        public CategoryStatViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCategoryColor = itemView.findViewById(R.id.view_category_color);
            textViewCategoryName = itemView.findViewById(R.id.text_view_category_name);
            textViewTaskCount = itemView.findViewById(R.id.text_view_task_count);
        }
    }
}