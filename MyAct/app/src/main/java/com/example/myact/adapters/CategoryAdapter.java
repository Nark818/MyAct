package com.example.myact.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myact.R;
import com.example.myact.api.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList = new ArrayList<>();
    private OnCategoryClickListener listener;
    private Context context;

    public CategoryAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category currentCategory = categoryList.get(position);
        holder.textViewCategoryName.setText(currentCategory.getName());

        try {
            holder.categoryColorIndicator.setBackgroundColor(Color.parseColor(currentCategory.getColor()));
        } catch (Exception e) {
            holder.categoryColorIndicator.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void setCategories(List<Category> categories) {
        this.categoryList = categories;
        notifyDataSetChanged();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewCategoryName;
        private View categoryColorIndicator;
        private CardView categoryCardView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategoryName = itemView.findViewById(R.id.text_view_category_name);
            categoryColorIndicator = itemView.findViewById(R.id.view_category_color);
            categoryCardView = itemView.findViewById(R.id.card_view_category);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onCategoryClick(categoryList.get(position));
                }
            });
        }
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }
}