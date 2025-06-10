package com.example.myact.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myact.R;
import com.example.myact.utils.BackupRestoreHelper;
import com.example.myact.utils.ThemeManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    private TextView textViewDate;
    private SwitchMaterial switchDarkTheme;
    private SwitchMaterial switchReminders;
    private Button buttonBackupData;
    private Button buttonRestoreData;
    private TextView textViewUserInfo;

    private SharedPreferences sharedPreferences;
    private BackupRestoreHelper backupRestoreHelper;

    private static final int REQUEST_CODE_BACKUP = 101;
    private static final int REQUEST_CODE_RESTORE = 102;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize views
        textViewDate = view.findViewById(R.id.text_view_date);
        switchDarkTheme = view.findViewById(R.id.switch_dark_theme);
        switchReminders = view.findViewById(R.id.switch_reminders);
        buttonBackupData = view.findViewById(R.id.button_backup_data);
        buttonRestoreData = view.findViewById(R.id.button_restore_data);

        //red 1
//        textViewUserInfo = view.findViewById(R.id.text_view_user_info);

        // Initialize shared preferences
        sharedPreferences = getActivity().getPreferences(AppCompatActivity.MODE_PRIVATE);
        backupRestoreHelper = new BackupRestoreHelper(getContext());

        // Display current date and time
        displayCurrentDateTime();

        // Set up current preferences
        setupPreferences();

        // Setup click listeners
        setupClickListeners();

        return view;
    }

    private void displayCurrentDateTime() {
        // Set the current datetime value (provided by user)
        textViewDate.setText("2025-06-10 14:35:49");
    }

    private void setupPreferences() {
        // Dark theme preference
        boolean isDarkTheme = sharedPreferences.getBoolean("dark_theme", false);
        switchDarkTheme.setChecked(isDarkTheme);

        // Reminders preference
        boolean remindersEnabled = sharedPreferences.getBoolean("reminders_enabled", true);
        switchReminders.setChecked(remindersEnabled);

        // Set user info
        if (textViewUserInfo != null) {
            textViewUserInfo.setText(getString(R.string.user_info, "Nark818"));
        }
    }

    private void setupClickListeners() {
        // Dark theme toggle
        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save preference
            sharedPreferences.edit().putBoolean("dark_theme", isChecked).apply();

            // Apply theme
            ThemeManager.applyTheme(isChecked);

            // Recreate activity to apply the new theme
            if (getActivity() != null) {
                getActivity().recreate();
            }
        });

        // Reminders toggle
        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save preference
            sharedPreferences.edit().putBoolean("reminders_enabled", isChecked).apply();

            // Show confirmation message
            String message = isChecked ?
                    getString(R.string.reminders_enabled) :
                    getString(R.string.reminders_disabled);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

            // In a real app, we would also update the notification scheduler here
        });

        // Backup button
        buttonBackupData.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/octet-stream");
            intent.putExtra(Intent.EXTRA_TITLE, "myact_backup_20250610.db");
            startActivityForResult(intent, REQUEST_CODE_BACKUP);
        });

        // Restore button
        buttonRestoreData.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_CODE_RESTORE);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                if (requestCode == REQUEST_CODE_BACKUP) {
                    backupRestoreHelper.backupDatabase(uri, success -> {
                        String message = success ?
                                getString(R.string.backup_success) :
                                getString(R.string.backup_failed);
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    });
                } else if (requestCode == REQUEST_CODE_RESTORE) {
                    backupRestoreHelper.restoreDatabase(uri, success -> {
                        String message = success ?
                                getString(R.string.restore_success) :
                                getString(R.string.restore_failed);
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

                        // Restart app to apply restored data
                        if (success && getActivity() != null) {
                            getActivity().recreate();
                        }
                    });
                }
            }
        }
    }
}