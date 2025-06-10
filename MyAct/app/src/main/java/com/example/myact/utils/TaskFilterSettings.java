package com.example.myact.utils;

import java.util.Arrays;

public class TaskFilterSettings {
    // Status filters
    public static final int STATUS_ALL = 0;
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_COMPLETED = 2;

    // Due date filters
    public static final int DATE_ALL = 0;
    public static final int DATE_TODAY = 1;
    public static final int DATE_THIS_WEEK = 2;
    public static final int DATE_OVERDUE = 3;

    // Filter settings
    private int statusFilter = STATUS_ALL;
    private int[] priorityFilter = {1, 2, 3}; // All priorities by default
    private int categoryFilter = -1; // All categories by default
    private int dueDateFilter = DATE_ALL;

    // Search query
    private String searchQuery = "";

    // Constructor
    public TaskFilterSettings() {
        // Default constructor
    }

    // Reset all filters to default
    public void reset() {
        statusFilter = STATUS_ALL;
        priorityFilter = new int[]{1, 2, 3};
        categoryFilter = -1;
        dueDateFilter = DATE_ALL;
        searchQuery = "";
    }

    // Getters and setters
    public int getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(int statusFilter) {
        this.statusFilter = statusFilter;
    }

    public int[] getPriorityFilter() {
        return priorityFilter;
    }

    public void setPriorityFilter(int[] priorityFilter) {
        this.priorityFilter = priorityFilter;
    }

    public int getCategoryFilter() {
        return categoryFilter;
    }

    public void setCategoryFilter(int categoryFilter) {
        this.categoryFilter = categoryFilter;
    }

    public int getDueDateFilter() {
        return dueDateFilter;
    }

    public void setDueDateFilter(int dueDateFilter) {
        this.dueDateFilter = dueDateFilter;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery != null ? searchQuery : "";
    }

    public boolean hasActiveFilters() {
        return statusFilter != STATUS_ALL ||
                !Arrays.equals(priorityFilter, new int[]{1, 2, 3}) ||
                categoryFilter != -1 ||
                dueDateFilter != DATE_ALL ||
                !searchQuery.isEmpty();
    }
}