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

package com.pranavpandey.android.dynamic.backup;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pranavpandey.android.dynamic.util.concurrent.DynamicTask;

import java.io.File;

/**
 * Interface to listen various backup events.
 */
public interface DynamicBackup {

    /**
     * Returns the mime type for the backup file.
     *
     * @return The mime type for the backup file.
     */
    @NonNull String getBackupMime();

    /**
     * Returns the mime type to import the backup file.
     *
     * @return The mime type to import the backup file.
     */
    @NonNull String getBackupImportMime();

    /**
     * Returns the extension for the backup file.
     *
     * @return The extension for the backup file.
     */
    @NonNull String getBackupExtension();

    /**
     * Returns the backup directory for the app storage.
     *
     * @return The backup directory for the app storage.
     */
    @Nullable String getBackupDir();

    /**
     * This method will be called to show the backup dialog.
     *
     * @param type The dialog type to be set.
     */
    void showBackupDialog(@Backup.Type int type);

    /**
     * This method will be called to get the selected backup location.
     *
     * @return The selected backup location.
     */
    @Backup.Location int getBackupLocation();

    /**
     * This method will be called to save the selected backup location.
     *
     * @param location The backup location to be set.
     */
    void setBackupLocation(@Backup.Location int location);

    /**
     * This method will be called to check if the backup exits for the supplied name.
     *
     * @param backupName The backup name to be checked.
     * @param location The backup location to be used.
     *
     * @return {@code true} if the backup exits for the supplied name.
     */
    boolean isBackupExist(@Nullable String backupName, @Backup.Location int location);

    /**
     * This method will be called on creating the backup.
     *
     * @param backup The requested backup name.
     * @param location The requested backup location.
     */
    void onCreateBackup(@NonNull String backup, @Backup.Location int location);

    /**
     * This method will be called to get the backup task.
     *
     * @param backup The requested backup name.
     * @param location The requested backup location.
     *
     * @return The {@link DynamicTask} to create the backup.
     */
    @Nullable DynamicTask<?, ?, File> getBackupTask(
            @NonNull String backup, @Backup.Location int location);

    /**
     * This method will be called when a backup is created.
     *
     * @param backup The created backup file.
     * @param location The requested backup location.
     */
    void onBackupCreated(@Nullable File backup, @Backup.Location int location);

    /**
     * This method will be called to set the backup progress.
     *
     * @param backupConfig The backup configurations.
     * @param visible {@code true} to make the progress visible.
     */
    void onSetProgress(@Nullable BackupConfig backupConfig, boolean visible);

    /**
     * This method will be called when a backup is saved.
     *
     * @param backup The saved backup file.
     * @param location The requested backup location.
     */
    void onBackupSaved(@Nullable File backup, @Backup.Location int location);

    /**
     * This method will be called when a backup is saved.
     *
     * @param backup The saved backup file URI.
     * @param location The requested backup location.
     */
    void onBackupSaved(@Nullable Uri backup, @Backup.Location int location);

    /**
     * This method will be called on sharing the backup.
     *
     * @param backup The created backup file.
     */
    void onShareBackup(@Nullable File backup);

    /**
     * This method will be called on renaming the backup.
     *
     * @param backup The backup file to be renamed.
     * @param newName The new name for the backup file.
     */
    void onRenameBackup(@NonNull File backup, @NonNull String newName);

    /**
     * This method will be called after renaming the backup.
     *
     * @param backup The renamed backup file to.
     * @param newName The new name of the backup file.
     * @param renamed {@code true} if the backup is renamed successfully.
     */
    void onBackupRenamed(@Nullable File backup, @Nullable String newName, boolean renamed);

    /**
     * This method will be called on deleting the backup.
     *
     * @param backup The backup file to be deleted.
     * @param delete {@code true} to delete the backup.
     */
    void onDeleteBackup(@Nullable File backup, boolean delete);

    /**
     * This method will be called after deleting a backup.
     *
     * @param backup The deleted backup name.
     */
    void onBackupDeleted(@Nullable String backup);

    /**
     * This method will be called on deleting all the backups stored in the app storage.
     */
    void onDeleteAllBackups();

    /**
     * This method will be called on deleting all the backups stored in the app storage.
     *
     * @param delete {@code true} to delete all the backups.
     */
    void onDeleteAllBackups(boolean delete);

    /**
     * This method will be called after deleting all the backups stored in the app storage.
     *
     * @param deleted {@code true} if the operation has been completed successfully.
     */
    void onAllBackupsDeleted(boolean deleted);

    /**
     * This method will be to verify the selected backup.
     *
     * @param backup The backup URI to be verified.
     *
     * @return {@code true} if the backup has been verified successfully.
     */
    boolean onVerifyBackup(@NonNull Uri backup);

    /**
     * This method will be to verify the selected backup.
     *
     * @param backup The backup file to be verified.
     *
     * @return {@code true} if the backup has been verified successfully.
     */
    boolean onVerifyBackup(@NonNull File backup);

    /**
     * This method will be called on restoring a backup.
     *
     * @param backup The backup file to be restored.
     */
    void onRestoreBackup(@NonNull File backup);

    /**
     * This method will be called on restoring a backup.
     *
     * @param backup The backup file to be restored.
     * @param delete {@code true} to delete the backup file after performing the restore
     *               operation.
     */
    void onRestoreBackup(@NonNull File backup, boolean delete);

    /**
     * This method will be called to get the restore task.
     *
     * @param backup The backup file to be restored.
     * @param delete {@code true} to delete the backup file after performing the restore
     *               operation.
     *
     * @return The {@link DynamicTask} to create the backup.
     */
    @Nullable DynamicTask<?, ?, Boolean> getRestoreTask(@NonNull File backup, boolean delete);

    /**
     * This method will be called after completing the restore process.
     *
     * @param backup The restored backup file.
     */
    void onRestoreComplete(@NonNull File backup);

    /**
     * This method will be called on requesting the backup from device storage.
     */
    void onImportBackup();

    /**
     * This method will be called on importing the backup from device storage.
     *
     * @param uri The backup file URI to be imported.
     */
    void onImportBackup(@Nullable Uri uri);

    /**
     * This method will be called if there is any error while doing the backup operations.
     *
     * @param file The erroneous backup file.
     * @param location The requested backup location.
     */
    void onBackupError(@Nullable File file, @Backup.Location int location);

    /**
     * This method will be called if there is any error while doing the restore operations.
     *
     * @param file The erroneous restore file.
     */
    void onRestoreError(@Nullable File file);

    /**
     * This method will be called on refreshing the backup state.
     */
    void onRefresh();
}
