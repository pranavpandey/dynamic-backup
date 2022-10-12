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

package com.pranavpandey.android.dynamic.backup.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;

import com.pranavpandey.android.dynamic.backup.Backup;
import com.pranavpandey.android.dynamic.backup.BackupConfig;
import com.pranavpandey.android.dynamic.backup.DynamicBackup;
import com.pranavpandey.android.dynamic.backup.R;
import com.pranavpandey.android.dynamic.backup.dialog.DynamicBackupDialog;
import com.pranavpandey.android.dynamic.backup.dialog.DynamicRestoreDialog;
import com.pranavpandey.android.dynamic.backup.task.BackupDeleteTask;
import com.pranavpandey.android.dynamic.backup.task.BackupRenameTask;
import com.pranavpandey.android.dynamic.backup.util.DynamicBackupUtils;
import com.pranavpandey.android.dynamic.preferences.DynamicPreferences;
import com.pranavpandey.android.dynamic.support.Dynamic;
import com.pranavpandey.android.dynamic.support.activity.DynamicActivity;
import com.pranavpandey.android.dynamic.support.dialog.DynamicDialog;
import com.pranavpandey.android.dynamic.support.dialog.fragment.DynamicDialogFragment;
import com.pranavpandey.android.dynamic.support.dialog.fragment.DynamicProgressDialog;
import com.pranavpandey.android.dynamic.support.fragment.DynamicFragment;
import com.pranavpandey.android.dynamic.support.model.DynamicTaskViewModel;
import com.pranavpandey.android.dynamic.support.permission.DynamicPermissions;
import com.pranavpandey.android.dynamic.support.util.DynamicPickerUtils;
import com.pranavpandey.android.dynamic.theme.Theme;
import com.pranavpandey.android.dynamic.util.DynamicFileUtils;
import com.pranavpandey.android.dynamic.util.DynamicIntentUtils;
import com.pranavpandey.android.dynamic.util.concurrent.DynamicResult;
import com.pranavpandey.android.dynamic.util.concurrent.task.FileWriteTask;

import java.io.File;

/**
 * A dynamic fragment to provide backup functionality.
 * <p>Extend it and implement the required methods accordingly.
 */
public abstract class DynamicBackupFragment extends DynamicFragment
        implements DynamicBackup, DialogInterface.OnDismissListener {

    /**
     * Constant to request the storage permission to create backup.
     */
    protected static final int REQUEST_PERMISSION_BACKUP = 0;

    /**
     * Constant to request the storage permission to restore backup.
     */
    protected static final int REQUEST_PERMISSION_RESTORE = 1;

    /**
     * Constant to request the backup file location.
     */
    protected static final int REQUEST_BACKUP_LOCATION = 2;

    /**
     * Constant to request the backup import from file.
     */
    protected static final int REQUEST_IMPORT_BACKUP = 3;

    /**
     * Backup file used by this fragment.
     */
    protected File mBackup;

    /**
     * Dialog fragment to show the backup options.
     */
    protected DynamicBackupDialog mBackupDialog;

    /**
     * Dialog fragment to show the progress.
     */
    protected DynamicDialogFragment mProgressDialog;

    /**
     * Checks whether the storage permission is granted.
     * <p>Currently, it will always return {@code true} as it is not required for
     * API 19 and above.
     *
     * @return {@code true} if storage permission is granted.
     */
    public boolean isStoragePermissionGranted() {
        return true;
    }

    /**
     * Request the storage permission.
     *
     * @param requestCode The request code for the result.
     *
     * @return {@code true} if the storage permission has been granted.
     */
    public boolean requestStoragePermission(int requestCode) {
        if (!isStoragePermissionGranted()) {
            DynamicPermissions.getInstance().isGranted(this,
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    true, requestCode);
        }

        return isStoragePermissionGranted();
    }

    @Override
    public @NonNull String getBackupImportMime() {
        return getBackupMime() + Theme.Key.SPLIT + DynamicFileUtils.MIME_APPLICATION;
    }

    @Override
    public void showBackupDialog(@Backup.Type int type) {
        mBackupDialog = DynamicBackupDialog.newInstance().setType(type).setDynamicBackup(this);
        mBackupDialog.setOnDismissListener(this).showDialog(requireActivity());
    }

    @Override
    public @Backup.Location int getBackupLocation() {
        return DynamicPreferences.getInstance().load(Backup.KEY_LOCATION, Backup.Location.APP);
    }

    @Override
    public void setBackupLocation(@Backup.Location int location) {
        DynamicPreferences.getInstance().save(Backup.KEY_LOCATION, location);
    }

    @Override
    public void onCreateBackup(@NonNull String backup, @Backup.Location int location) {
        if (getBackupTask(backup, location) == null) {
            onBackupError(null, location);

            return;
        }

        new ViewModelProvider(this).get(DynamicTaskViewModel.class)
                .execute(getBackupTask(backup, location));
    }

    @Override
    public void onSetProgress(@Nullable BackupConfig backupConfig, boolean visible) {
        if (mProgressDialog != null && mProgressDialog.isAdded()) {
            mProgressDialog.dismiss();
        }

        if (visible && backupConfig != null) {
            @StringRes int title;
            String name = null;

            switch (backupConfig.getType()) {
                case Backup.Type.BACKUP_FILE:
                    title = R.string.adb_backup;
                    name = backupConfig.getName();
                    break;
                case Backup.Type.DELETE_ALL:
                    title = R.string.adb_backup_option_delete;
                    if (backupConfig.isDelete()) {
                        name = getString(R.string.adb_backup_delete_all_title);
                    }
                    break;
                case Backup.Type.DELETE:
                    title = R.string.adb_backup_option_delete;
                    if (backupConfig.isDelete() && backupConfig.getFile() != null) {
                        name = backupConfig.getFile().getName();
                    }
                    break;
                case Backup.Type.RESTORE:
                    title = R.string.adb_backup_restore;
                    if (backupConfig.getFile() != null) {
                        name = backupConfig.getFile().getName();
                    }
                    break;
                case Backup.Type.RENAME:
                    title = R.string.adb_backup_option_rename;
                    if (backupConfig.getFile() != null) {
                        name = String.format(getString(R.string.ads_format_refactor),
                                DynamicFileUtils.getBaseName(backupConfig.getFile().getName()),
                                backupConfig.getName());
                    }
                    break;
                case Backup.Type.BACKUP:
                case Backup.Type.MODIFY:
                default:
                    title = backupConfig.getLocation() == Backup.Location.APP_MODIFY
                            ? R.string.adb_backup_modify : R.string.adb_backup;
                    name = backupConfig.getName();
                    break;
            }

            Dynamic.setAppBarProgressVisible(getActivity(), true);
            mProgressDialog = DynamicProgressDialog.newInstance().setName(name)
                    .setBuilder(new DynamicDialog.Builder(requireContext()).setTitle(title));
            mProgressDialog.showDialog(requireActivity());
        } else if (!visible) {
            Dynamic.setAppBarProgressVisible(getActivity(), false);
            mProgressDialog = null;
        }
    }

    @Override
    public void onBackupCreated(@Nullable File backup, @Backup.Location int location) {
        if (backup == null) {
            onBackupError(null, location);

            return;
        }
        
        mBackup = backup;

        if (backup.exists()) {
            onBackupSaved(backup, location);
        } else {
            onBackupError(backup, location);

            return;
        }

        if (location == Backup.Location.SHARE) {
            onShareBackup(backup);
        } else if (location == Backup.Location.STORAGE) {
            final File file;
            if ((file = DynamicPickerUtils.saveToFile(requireContext(), this,
                    backup, getBackupMime(), REQUEST_BACKUP_LOCATION,
                    true, backup.getName())) != null) {
                saveBackup(REQUEST_BACKUP_LOCATION,
                        DynamicFileUtils.getUriFromFile(requireContext(), file));
            } else if (!DynamicIntentUtils.isFilePicker(requireContext(), getBackupMime())) {
                onBackupError(backup, location);
            }
        }

        onRefresh();
    }

    @Override
    public void onBackupSaved(@Nullable File backup, @Backup.Location int location) {
        if (!(getActivity() instanceof DynamicActivity) || backup == null) {
            return;
        }

        switch (location) {
            case Backup.Location.APP:
                Dynamic.showSnackbar(getActivity(), DynamicBackupUtils.getBackupCreated(
                        requireContext(), DynamicFileUtils.getBaseName(backup.getName())));
                break;
            case Backup.Location.APP_MODIFY:
                Dynamic.showSnackbar(getActivity(), DynamicBackupUtils.getBackupModified(
                        requireContext(), DynamicFileUtils.getBaseName(backup.getName())));
                break;
            case Backup.Location.STORAGE:
            case Backup.Location.SHARE:
                break;
        }

        onRefresh();
    }

    @Override
    public void onBackupSaved(@Nullable Uri backup, @Backup.Location int location) {
        if (!(getActivity() instanceof DynamicActivity) || backup == null) {
            return;
        }

        if (location == Backup.Location.STORAGE) {
            Dynamic.showSnackbar(getActivity(), DynamicBackupUtils.getBackupSaved(requireContext(),
                    DynamicFileUtils.getFileNameFromUri(requireContext(), backup)));
        }

        onRefresh();
    }

    @Override
    public void onRenameBackup(@NonNull File backup, @NonNull String newName) {
        new ViewModelProvider(this).get(DynamicTaskViewModel.class)
                .execute(new BackupRenameTask(this,
                        new BackupConfig(Backup.Type.RENAME, backup, newName)));
    }

    @Override
    public void onBackupRenamed(@Nullable File backup, @Nullable String newName, boolean renamed) {
        onRefresh();
    }

    @Override
    public void onDeleteBackup(@Nullable File backup, boolean delete) {
        new ViewModelProvider(this).get(DynamicTaskViewModel.class)
                .execute(new BackupDeleteTask(this,
                        new BackupConfig(Backup.Type.DELETE, backup, delete)));
    }

    @Override
    public void onBackupDeleted(@Nullable String backup) {
        onRefresh();
    }

    @Override
    public void onDeleteAllBackups() {
        DynamicDialogFragment.newInstance().setBuilder(
                new DynamicDialog.Builder(requireContext())
                        .setTitle(getString(R.string.adb_backup_delete_all_title))
                        .setMessage(getString(R.string.adb_backup_delete_all_desc))
                        .setPositiveButton(getString(R.string.adb_backup_option_delete),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        onDeleteAllBackups(true);
                                    }
                                })
                        .setNegativeButton(getString(R.string.ads_cancel), null))
                .setOnDismissListener(this)
                .showDialog(requireActivity());
    }

    @Override
    public void onDeleteAllBackups(boolean delete) {
        if (delete) {
            new ViewModelProvider(this).get(DynamicTaskViewModel.class)
                    .execute(new BackupDeleteTask(this,
                            new BackupConfig(Backup.Type.DELETE_ALL, null, true)));
        }
    }

    @Override
    public void onAllBackupsDeleted(boolean deleted) {
        onRefresh();

        if (!(getActivity() instanceof DynamicActivity)) {
            return;
        }

        if (deleted) {
            Dynamic.showSnackbar(getActivity(), R.string.adb_backup_delete_all_done);
        }
    }

    @Override
    public void onRestoreBackup(@NonNull File backup) {
        DynamicRestoreDialog.newInstance().setBackup(backup).setDynamicBackup(this)
                .setOnDismissListener(this).showDialog(requireActivity());
    }

    @Override
    public void onRestoreBackup(@NonNull File backup, boolean delete) {
        if (getRestoreTask(backup, delete) == null) {
            onBackupError(null, Backup.Location.APP);

            return;
        }

        new ViewModelProvider(this).get(DynamicTaskViewModel.class)
                .execute(getRestoreTask(backup, delete));
    }

    @Override
    public void onImportBackup() {
        DynamicPickerUtils.selectFile(requireContext(), this,
                getBackupImportMime(), REQUEST_IMPORT_BACKUP);
    }

    @Override
    public void onImportBackup(@Nullable Uri uri) {
        DynamicRestoreDialog.newInstance().setBackupUri(uri).setDynamicBackup(this)
                .setOnDismissListener(this).showDialog(requireActivity());
    }

    @Override
    public void onRefresh() {
        if (mBackupDialog != null && mBackupDialog.isAdded()) {
            switch (mBackupDialog.getType()) {
                case Backup.Type.MODIFY:
                case Backup.Type.RESTORE:
                    mBackupDialog.setBackupsAdapter();
                    break;
            }
        }
    }

    @Override
    public void onBackupError(@Nullable File file, @Backup.Location int location) {
        Dynamic.showSnackbar(getActivity(), R.string.adb_backup_error_save);
    }

    @Override
    public void onRestoreError(@Nullable File file) {
        Dynamic.showSnackbar(getActivity(), R.string.adb_backup_restore_error);
    }

    /**
     * Save backup according to the supplied parameters.
     *
     * @param requestCode The request code to be used.
     * @param file The file URI to be used.
     */
    protected void saveBackup(int requestCode, @Nullable Uri file) {
        new ViewModelProvider(this).get(DynamicTaskViewModel.class).execute(
                new FileWriteTask(requireContext(), DynamicFileUtils
                        .getUriFromFile(requireContext(), mBackup), file) {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        onSetProgress(new BackupConfig(
                                DynamicFileUtils.getFileNameFromUri(requireContext(), file),
                                Backup.Location.STORAGE), true);
                    }

                    @Override
                    protected @Nullable Boolean doInBackground(@Nullable Void params) {
                        Boolean result = super.doInBackground(params);

                        if (mBackup != null) {
                            try {
                                boolean ignored = mBackup.delete();
                            } catch (Exception ignored) {
                            }
                        }

                        return result;
                    }

                    @Override
                    protected void onPostExecute(@Nullable DynamicResult<Boolean> result) {
                        super.onPostExecute(result);

                        onSetProgress(new BackupConfig(
                                DynamicFileUtils.getFileNameFromUri(requireContext(), file),
                                Backup.Location.STORAGE), false);

                        if (getBooleanResult(result)) {
                            onBackupSaved(file, Backup.Location.STORAGE);
                        } else {
                            onBackupError(mBackup, Backup.Location.STORAGE);
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        final Uri uri = data != null ? data.getData() : null;

        switch (requestCode) {
            case REQUEST_PERMISSION_BACKUP:
                showBackupDialog(Backup.Type.BACKUP);
                break;
            case REQUEST_PERMISSION_RESTORE:
                showBackupDialog(Backup.Type.RESTORE);
                break;
            case REQUEST_BACKUP_LOCATION:
                saveBackup(requestCode, uri);
                break;
            case REQUEST_IMPORT_BACKUP:
                onImportBackup(uri);
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        onRefresh();
    }
}
