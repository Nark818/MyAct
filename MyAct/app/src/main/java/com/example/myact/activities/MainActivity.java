package com.example.myact.activities;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myact.R;
import com.example.myact.utils.ThemeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before inflating the layout
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        boolean isDarkTheme = sharedPreferences.getBoolean("dark_theme", false);
        ThemeManager.applyTheme(isDarkTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup bottom navigation
        bottomNav = findViewById(R.id.bottom_nav);

        // FIXED: Use NavHostFragment to get NavController instead of direct Navigation.findNavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Setup AppBarConfiguration for the toolbar
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_tasks,
                    R.id.navigation_statistics,
                    R.id.navigation_settings
            ).build();

            // Connect toolbar with NavController
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

            // Connect BottomNavigationView with NavController
            NavigationUI.setupWithNavController(bottomNav, navController);

            // Update toolbar title based on destination
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destinationId = destination.getId();
                if (destinationId == R.id.navigation_tasks) {
                    toolbar.setTitle(R.string.title_tasks);
                } else if (destinationId == R.id.navigation_statistics) {
                    toolbar.setTitle(R.string.title_statistics);
                } else if (destinationId == R.id.navigation_settings) {
                    toolbar.setTitle(R.string.title_settings);
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }
}