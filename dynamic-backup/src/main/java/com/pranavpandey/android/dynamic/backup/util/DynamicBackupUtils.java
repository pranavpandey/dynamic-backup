/*
 * Copyright 2022 Pranav Pandey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pranavpandey.android.dynamic.backup.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.pranavpandey.android.dynamic.backup.R;
import com.pranavpandey.android.dynamic.preferences.DynamicPreferences;
import com.pranavpandey.android.dynamic.support.util.DynamicResourceUtils;
import com.pranavpandey.android.dynamic.util.DynamicDeviceUtils;
import com.pranavpandey.android.dynamic.util.DynamicFileUtils;
import com.pranavpandey.android.dynamic.util.DynamicPackageUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Helper class to perform backup operations.
 */
public class DynamicBackupUtils {

    /**
     * XML file extension.
     */
    public static final String EXTENSION_XML = "xml";

    /**
     * Database file extension.
     */
    public static final String EXTENSION_DB = "db";

    /**
     * Default directory for the app backup.
     */
    public static final String DIR_BACKUP = "backup";

    /**
     * Returns the default backup directory for the app.
     *
     * @param context The context to be used.
     *
     * @return The default backup directory for the app.
     *
     * @see #DIR_BACKUP
     * @see DynamicFileUtils#getExternalDir(Context, String)
     */
    public static @Nullable String getBackupDir(@Nullable Context context) {
        return DynamicFileUtils.getExternalDir(context, DIR_BACKUP);
    }

    /**
     * Returns the formatted backup directory according to the supplied path.
     *
     * @param context The context to be used.
     * @param path The path to be used.
     *
     * @return The formatted backup directory according to the supplied path.
     */
    public static @Nullable String getBackupDirString(
            @Nullable Context context, @Nullable String path) {
        if (context == null) {
            return null;
        }

        if (path != null) {
            return String.format(context.getString(R.string.adb_backup_format_location), path);
        } else {
            return context.getString(R.string.adb_backup_error);
        }
    }

    /**
     * Returns the backup name according to the time in milliseconds.
     *
     * @return The backup name according to the time in milliseconds.
     */
    public static @NonNull String getBackupName() {
        return DynamicDeviceUtils.getDateWithSeparator(System.currentTimeMillis());
    }

    /**
     * Returns the info about the last backup in the supplied directory.
     *
     * @param context The context to be used.
     * @param dir The backup location to be used.
     *
     * @return The info about the last backup in the supplied directory.
     */
    public static @Nullable String getLastBackup(@Nullable Context context, @Nullable String dir) {
        if (context == null || dir == null) {
            return null;
        }

        try {
            File[] backups;
            if ((backups = sortBackups(new File(dir).listFiles())) != null) {
                return String.format(context.getString(R.string.adb_backup_format_last_storage),
                        DynamicDeviceUtils.getDate(context, backups[0].lastModified()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return context.getString(R.string.adb_backup_not_found);
    }

    /**
     * Sort backups in ascending order according to the last modified date.
     *
     * @param backups The backups to be sorted.
     *
     * @return The sorted backups in ascending order according to the last modified date.
     */
    public static @Nullable File[] sortBackups(@Nullable File[] backups) {
        if (backups == null) {
            return null;
        }

        Arrays.sort(backups, Collections.reverseOrder(new Comparator<File>() {
            public int compare(File file1, File file2) {
                return Long.compare(file1.lastModified(), file2.lastModified());
            }
        }));

        return backups;
    }

    /**
     * Returns the last modified info for the supplied backup.
     *
     * @param context The context to be used.
     * @param backup The backup file to be used.
     *
     * @return The last modified info for the supplied backup.
     */
    public static @Nullable String getLastModified(
            @Nullable Context context, @Nullable File backup) {
        if (context == null || backup == null || !backup.exists()) {
            return null;
        }

        return DynamicDeviceUtils.getDate(context, backup.lastModified());
    }

    /**
     * Returns the formatted string with the backup created info.
     *
     * @param context The context to be used.
     * @param backupName The backup name to be used.
     *
     * @return The formatted string with the backup created info.
     */
    public static @Nullable String getBackupCreated(
            @Nullable Context context, @Nullable String backupName) {
        if (context == null) {
            return null;
        }

        return String.format(context.getString(R.string.adb_backup_format_created), backupName);
    }

    /**
     * Returns the formatted string with the backup modified info.
     *
     * @param context The context to be used.
     * @param backupName The backup name to be used.
     *
     * @return The formatted string with the backup modified info.
     */
    public static @Nullable String getBackupModified(
            @Nullable Context context, @Nullable String backupName) {
        if (context == null) {
            return null;
        }

        return String.format(context.getString(R.string.adb_backup_format_modified), backupName);
    }

    /**
     * Returns the formatted string with the backup saved info.
     *
     * @param context The context to be used.
     * @param backupName The backup name to be used.
     *
     * @return The formatted string with the backup saved info.
     */
    public static @Nullable String getBackupSaved(
            @Nullable Context context, @Nullable String backupName) {
        if (context == null) {
            return null;
        }

        return String.format(context.getString(R.string.adb_backup_format_saved), backupName);
    }

    /**
     * Returns the formatted string with the backup renamed info.
     *
     * @param context The context to be used.
     * @param backupName The backup name to be used.
     *
     * @return The formatted string with the backup renamed info.
     */
    public static @Nullable String getBackupRenamed(
            @Nullable Context context, @Nullable String backupName) {
        if (context == null) {
            return null;
        }

        return String.format(context.getString(R.string.adb_backup_format_renamed), backupName);
    }

    /**
     * Returns the formatted string with the backup deleted info.
     *
     * @param context The context to be used.
     * @param backupName The backup name to be used.
     *
     * @return The formatted string with the backup deleted info.
     */
    public static @Nullable String getBackupDeleted(
            @Nullable Context context, @Nullable String backupName) {
        if (context == null) {
            return null;
        }

        return String.format(context.getString(R.string.adb_backup_format_deleted), backupName);
    }

    /**
     * Returns the formatted string with the backup restored info.
     *
     * @param context The context to be used.
     * @param backupName The backup name to be used.
     *
     * @return The formatted string with the backup restored info.
     */
    public static @Nullable String getBackupRestored(
            @Nullable Context context, @Nullable String backupName) {
        if (context == null) {
            return null;
        }

        return String.format(context.getString(R.string.adb_backup_format_restored), backupName);
    }

    /**
     * Returns the hint to notify if no backups are available to restore.
     *
     * @param context The context to be used.
     *
     * @return The hint to notify if no backups are available to restore.
     */
    public static @Nullable String getNoBackupHint(@Nullable Context context) {
        if (context == null) {
            return null;
        }

        return String.format(context.getString(R.string.adu_format_blank_space),
                context.getString(R.string.adb_backup_not_found),
                context.getString(R.string.adb_backup_create_new_info));
    }

    /**
     * Returns the hint to notify if no backups are available to restore along with
     * the import info.
     *
     * @param context The context to be used.
     *
     * @return The hint to notify if no backups are available to restore along with
     *         the import info.
     */
    public static @Nullable String getNoBackupHintImport(@Nullable Context context) {
        if (context == null) {
            return null;
        }

        return String.format(context.getString(R.string.adu_format_blank_space),
                context.getString(R.string.adb_backup_not_found),
                context.getString(R.string.adb_backup_import_info));
    }

    /**
     * Returns available the backup options.
     *
     * @param context The context to be used.
     *
     * @return The available backup options.
     */
    public static @Nullable String[] getBackupOptions(@Nullable Context context) {
        if (context == null) {
            return null;
        }

        return context.getResources().getStringArray(R.array.adb_backup_options);
    }

    /**
     * Returns the icons for the backup options.
     *
     * @param context The context to be used.
     *
     * @return The icons for the backup options.
     */
    public static @Nullable int[] getBackupOptionsIcons(@Nullable Context context) {
        if (context == null) {
            return null;
        }

        return DynamicResourceUtils.convertToDrawableResArray(
                context, R.array.adb_backup_options_icons);
    }

    /**
     * Try to create the backup info at the supplied destination.
     *
     * @param context The context to be used.
     * @param destination The destination to be used.
     *
     * @return {@code true} if the backup info is created successfully.
     */
    public static boolean backupInfo(@Nullable Context context, @Nullable File destination) {
        if (context == null || destination == null) {
            return false;
        }

        try {
            File infoFile = new File(destination
                    + File.separator + context.getPackageName());
            DynamicFileUtils.verifyFile(infoFile.getParentFile());

            return DynamicFileUtils.writeStringToFile(context,
                    DynamicPackageUtils.getVersionCode(context),
                    DynamicFileUtils.getUriFromFile(context, infoFile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Try to backup the supplied shared preferences.
     *
     * @param context The context to be used.
     * @param destination The destination to be used.
     * @param preferences The shared preferences to be used.
     *
     * @return {@code true} if the backup is created successfully.
     */
    public static boolean backupSharedPreferences(@Nullable Context context,
            @Nullable File destination, @Nullable String preferences) {
        if (context == null || destination == null || preferences == null) {
            return false;
        }

        SharedPreferences pref;
        boolean success = false;
        ObjectOutputStream output = null;

        try {
            if (DynamicFileUtils.verifyFile(destination)) {
                pref = context.getSharedPreferences(preferences, Context.MODE_PRIVATE);
                output = new ObjectOutputStream(new FileOutputStream(
                        destination + File.separator + preferences + ".xml"));

                output.writeObject(pref.getAll());
                success = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException io) {
                io.printStackTrace();
            }
        }

        return success;
    }

    /**
     * Try to restore the supplied shared preferences file.
     *
     * @param context The context to be used.
     * @param file The file to be restored.
     *
     * @return {@code true} if the shared preferences are restored successfully.
     */
    @SuppressWarnings("unchecked")
    public static boolean restoreSharedPreferences(
            @Nullable Context context, @Nullable File file) {
        if (context == null || file == null) {
            return false;
        }

        boolean success = false;
        SharedPreferences.Editor editor;
        ObjectInputStream input = null;

        try {
            input = new ObjectInputStream(new FileInputStream(file));
            if (DynamicPreferences.getInstance().getDefaultSharedPreferencesName()
                    .equals(DynamicFileUtils.getBaseName(file.getName()))) {
                editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            } else {
                editor = context.getSharedPreferences(DynamicFileUtils.getBaseName(
                        file.getName()), Context.MODE_PRIVATE).edit();
            }
            editor.clear();

            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (!TextUtils.isEmpty(key)) {
                    if (value instanceof Boolean) {
                        editor.putBoolean(key, (Boolean) value);
                    } else if (value instanceof Float) {
                        editor.putFloat(key, (Float) value);
                    } else if (value instanceof Integer) {
                        editor.putInt(key, (Integer) value);
                    } else if (value instanceof Long) {
                        editor.putLong(key, (Long) value);
                    } else if (value instanceof String) {
                        editor.putString(key, ((String) value));
                    } else if (value instanceof HashSet) {
                        editor.putStringSet(key, ((Set<String>) value));
                    }
                }
            }

            editor.apply();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException io) {
                io.printStackTrace();
            }
        }

        return success;
    }

    /**
     * Try to backup the supplied database.
     *
     * @param context The context to be used.
     * @param destination The destination to be used.
     * @param database The database to be used.
     *
     * @return {@code true} if the backup is created successfully.
     */
    public static boolean backupDatabase(@Nullable Context context,
            @Nullable File destination, @Nullable SQLiteOpenHelper database) {
        if (context == null || destination == null || database == null
                || database.getDatabaseName() == null) {
            return false;
        }

        return DynamicFileUtils.writeToFile(new File(context.getDatabasePath(
                database.getDatabaseName()).toString()), destination,
                database.getDatabaseName());
    }

    /**
     * Try to backup the supplied database.
     *
     * @param context The context to be used.
     * @param destination The destination to be used.
     * @param database The database to be used.
     *
     * @return {@code true} if the backup is created successfully.
     */
    public static boolean backupDatabase(@Nullable Context context,
            @Nullable File destination, @Nullable SupportSQLiteOpenHelper database) {
        if (context == null || destination == null || database == null
                || database.getDatabaseName() == null) {
            return false;
        } else if (!new File(context.getDatabasePath(
                database.getDatabaseName()).toString()).exists()) {
            return true;
        }

        return DynamicFileUtils.writeToFile(new File(context.getDatabasePath(
                database.getDatabaseName()).toString()), destination,
                database.getDatabaseName());
    }

    /**
     * Try to restore the supplied database file.
     *
     * @param context The context to be used.
     * @param file The file to be restored.
     *
     * @return {@code true} if the file is restored successfully.
     */
    public static boolean restoreDatabase(@Nullable Context context, @Nullable File file) {
        if (context == null || file == null) {
            return false;
        }

        boolean success = false;
        String outFileName = context.getDatabasePath(file.getName()).getPath();

        try {
            FileInputStream fis = new FileInputStream(file);
            OutputStream output = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            fis.close();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return success;
    }

    /**
     * Try to restore the shared preferences and database inside the supplied directory.
     *
     * @param context The context to be used.
     * @param dir The directory to be restored.
     *
     * @see #restoreSharedPreferences(Context, File)
     * @see #restoreDatabase(Context, File)
     *
     * @return {@code true} if the directory is restored successfully.
     */
    public static boolean restoreFiles(@Nullable Context context, @Nullable File dir) {
        if (context == null || dir == null) {
            return false;
        }

        boolean success = false;

        if (dir.isDirectory()) {
            File[] backupFiles = dir.listFiles();

            if (backupFiles != null) {
                for (File file : backupFiles) {
                    final String extension = DynamicFileUtils.getExtension(file);

                    if (extension != null) {
                        switch (extension) {
                            case EXTENSION_XML:
                                restoreSharedPreferences(context, file);
                                break;
                            case EXTENSION_DB:
                                restoreDatabase(context, file);
                                break;
                        }
                    }
                }
            }

            success = true;
        }

        return success;
    }
}
