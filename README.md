# MyAct - Task Management Application
## Overview

MyAct is a modern Android task management application designed to help users track and organize their daily tasks. With an intuitive interface, customizable categories, and powerful filtering capabilities, MyAct makes task organization simple and efficient.

## Features

### Task Management
- Create, edit, and delete tasks
- Set task priorities (Low, Medium, High)
- Assign categories with custom colors
- Set due dates for time-sensitive tasks
- Mark tasks as completed

### Organization
- Filter tasks by status (Active/Completed)
- Filter tasks by priority
- Filter tasks by category
- Filter tasks by due date (Today, This Week, Overdue)
- Search functionality for quick task lookup

### Statistics and Insights
- Visual overview of task distribution
- Task completion percentage
- Task count by priority level
- Task count by category
- Active vs. completed task breakdown

### User Experience
- Material Design 3 UI components
- Customizable themes (Light/Dark mode)
- Swipe-to-refresh for task list updates
- Modern bottom navigation
- Celebration animations upon task completion

### Data Management
- SQLite database for local storage
- Backup and restore functionality
- Default categories to get started quickly

## Architecture

MyAct follows modern Android development practices:

### Project Structure
- **activities**: Contains all activity classes
- **adapters**: Contains RecyclerView adapters
- **api.models**: Data model classes
- **database**: Database helpers and contract classes
- **fragments**: UI fragments
- **utils**: Utility classes

### Database Schema

#### Tasks Table
| Column         | Type    | Description                            |
|----------------|---------|----------------------------------------|
| id             | INTEGER | Primary key                            |
| title          | TEXT    | Task title                             |
| description    | TEXT    | Task description                       |
| due_date       | TEXT    | Due date in YYYY-MM-DD format          |
| priority       | INTEGER | Priority level (1=Low, 2=Med, 3=High)  |
| category_id    | INTEGER | Foreign key to categories table        |
| is_completed   | INTEGER | Task completion status (0/1)           |

#### Categories Table
| Column  | Type    | Description                   |
|---------|---------|-------------------------------|
| id      | INTEGER | Primary key                   |
| name    | TEXT    | Category name                 |
| color   | TEXT    | Hex color code for category   |

## Key Components

### Activities
- **MainActivity**: Main application interface with bottom navigation
- **SplashActivity**: Initial loading screen with motivational quotes
- **TaskDetailActivity**: Create/edit task details
- **RewardActivity**: Celebration screen shown when completing tasks

### Fragments
- **TaskListFragment**: Shows the list of tasks with filtering options
- **StatisticsFragment**: Displays task statistics and insights
- **SettingsFragment**: User preferences and app settings

### Utilities
- **ThemeManager**: Handles app theme switching
- **NetworkUtils**: Network connectivity checking
- **BackupRestoreHelper**: Database backup and restore operations
- **TaskFilterSettings**: Manages task filtering preferences

## Getting Started

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or newer
- Minimum SDK: Android 5.0 (API level 21)
- Target SDK: Android 12 (API level 31)

### Setup Instructions
1. Clone the repository or download the source code
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the application on an emulator or physical device

### Configuration
- The app uses Material Components theme
- Default categories are defined in TaskContract.DEFAULT_CATEGORIES
- App preferences are stored using SharedPreferences

## Best Practices Implemented

- Singleton pattern for database access
- Executor pattern for background operations
- Proper database transaction management
- Separation of concerns with contract classes
- Responsive UI design
- Efficient RecyclerView adapters
- Material Design guidelines adherence
- Background processing on separate threads
- Proper resource organization

## Future Enhancements
- Cloud synchronization
- Task sharing
- Custom notifications
- Calendar integration
- Widgets for home screen
- Subtasks and checklists
- Recurring tasks
- Time tracking

## Troubleshooting

### Common Issues
- **Database Errors**: Try clearing app data or reinstalling
- **Missing Categories**: Check database initialization in TaskDbHelper
- **UI Glitches**: Ensure Material Components theme is properly applied
- **Data Loss**: Use the backup functionality regularly

### Debugging Tips
- Enable developer options for detailed logging
- Check Logcat for database operation errors
- Verify theme attributes in styles.xml
- Test on multiple device sizes for responsive design

