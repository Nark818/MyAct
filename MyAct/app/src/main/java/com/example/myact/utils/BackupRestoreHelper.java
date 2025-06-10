package com.example.myact.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BackupRestoreHelper {

    public interface OperationCallback {
        void onComplete(boolean success);
    }

    private final Context context;
    private final Executor executor;
    private final Handler mainHandler;

    public BackupRestoreHelper(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void backupDatabase(Uri destinationUri, OperationCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            File dbFile = context.getDatabasePath("tasks.db");

            if (dbFile.exists()) {
                try (InputStream in = new FileInputStream(dbFile);
                     OutputStream out = context.getContentResolver().openOutputStream(destinationUri)) {

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            final boolean finalSuccess = success;
            mainHandler.post(() -> callback.onComplete(finalSuccess));
        });
    }

    public void restoreDatabase(Uri sourceUri, OperationCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            File dbFile = context.getDatabasePath("tasks.db");

            // Close any open database connections
            try {
                context.deleteDatabase("tasks.db");

                try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
                     OutputStream out = new FileOutputStream(dbFile)) {

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            final boolean finalSuccess = success;
            mainHandler.post(() -> callback.onComplete(finalSuccess));
        });
    }
}