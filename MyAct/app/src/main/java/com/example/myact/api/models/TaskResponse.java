package com.example.myact.api.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TaskResponse {
    private List<Task> tasks;
    private String status;
    private String message;

    public TaskResponse(JSONObject json) {
        try {
            tasks = new ArrayList<>();
            status = json.getString("status");
            message = json.getString("message");

            if (json.has("tasks")) {
                JSONArray tasksArray = json.getJSONArray("tasks");
                for (int i = 0; i < tasksArray.length(); i++) {
                    JSONObject taskJson = tasksArray.getJSONObject(i);
                    Task task = new Task();
                    task.setId(taskJson.getInt("id"));
                    task.setTitle(taskJson.getString("title"));
                    task.setDescription(taskJson.getString("description"));
                    task.setDueDate(taskJson.getString("due_date"));
                    task.setPriority(taskJson.getInt("priority"));
                    task.setCompleted(taskJson.getBoolean("is_completed"));
                    task.setCategoryId(taskJson.getInt("category_id"));
                    tasks.add(task);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}