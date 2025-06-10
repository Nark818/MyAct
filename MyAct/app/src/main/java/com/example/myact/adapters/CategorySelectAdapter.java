package com.example.myact.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myact.R;
import com.example.myact.api.models.Category;

import java.util.List;

public class CategorySelectAdapter extends RecyclerView.Adapter<CategorySelectAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private Context context;
    private int selectedCategoryId;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategorySelectAdapter(Context context, List<Category> categories, int selectedCategoryId) {
        this.context = context;
        this.categories = categories;
        this.selectedCategoryId = selectedCategoryId;
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_select, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);

        holder.textViewCategoryName.setText(category.getName());

        // Set color indicator
        try {
            holder.viewCategoryColor.setBackgroundColor(Color.parseColor(category.getColor()));
        } catch (Exception e) {
            holder.viewCategoryColor.setBackgroundColor(Color.GRAY);
        }

        // Set radio button state
        holder.radioButton.setChecked(category.getId() == selectedCategoryId);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            selectedCategoryId = category.getId();

            if (listener != null) {
                listener.onCategoryClick(category);
            }

            // Update all radio buttons
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        View viewCategoryColor;
        TextView textViewCategoryName;
        RadioButton radioButton;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCategoryColor = itemView.findViewById(R.id.view_category_color);
            textViewCategoryName = itemView.findViewById(R.id.text_view_category_name);
            radioButton = itemView.findViewById(R.id.radio_button);
        }
    }
}