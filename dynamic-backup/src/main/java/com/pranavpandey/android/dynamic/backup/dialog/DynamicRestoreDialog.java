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

package com.pranavpandey.android.dynamic.backup.dialog;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pranavpandey.android.dynamic.backup.Backup;
import com.pranavpandey.android.dynamic.backup.DynamicBackup;
import com.pranavpandey.android.dynamic.backup.R;
import com.pranavpandey.android.dynamic.support.Dynamic;
import com.pranavpandey.android.dynamic.support.dialog.DynamicDialog;
import com.pranavpandey.android.dynamic.support.dialog.fragment.DynamicDialogFragment;
import com.pranavpandey.android.dynamic.support.model.DynamicTaskViewModel;
import com.pranavpandey.android.dynamic.util.DynamicFileUtils;
import com.pranavpandey.android.dynamic.util.concurrent.DynamicResult;
import com.pranavpandey.android.dynamic.util.concurrent.DynamicTask;

import java.io.File;

/**
 * A {@link DynamicDialogFragment} to provide the backup restore functionality.
 */
public class DynamicRestoreDialog extends DynamicDialogFragment {

    /**
     * Tag for this dialog fragment.
     */
    public static final String TAG = "DynamicRestoreDialog";

    /**
     * State key to save the ready backup.
     */
    private static final String STATE_READY_BACKUP = "state_ready_backup";

    /**
     * Interface to dispatch various backup events.
     */
    private DynamicBackup mDynamicBackup;

    /**
     * Backup file used by this dialog.
     */
    private File mBackup;

    /**
     * Backup file URI used by this dialog.
     */
    private Uri mBackupUri;

    /**
     * {@code true} to delete the backup file after performing the restore operation.
     */
    private boolean mDeleteBackup;

    /**
     * {@code true} if the backup is valid.
     */
    private boolean mValidBackup;

    /**
     * {@code true} if the backup is ready to restored.
     */
    private boolean mReadyBackup;

    /**
     * Text view to show the optional message.
     */
    private TextView mMessage;

    /**
     * Text view to show the optional description.
     */
    private TextView mDescription;

    /**
     * Progress bar used by this dialog.
     */
    private ProgressBar mProgressBar;

    /**
     * Initialize the new instance of this fragment.
     *
     * @return An instance of {@link DynamicRestoreDialog}.
     */
    public static @NonNull DynamicRestoreDialog newInstance() {
        return new DynamicRestoreDialog();
    }

    @Override
    protected @NonNull DynamicDialog.Builder onCustomiseBuilder(
            @NonNull DynamicDialog.Builder dialogBuilder, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(
                R.layout.adb_dialog_restore, new LinearLayout(requireContext()), false);

        mMessage = view.findViewById(R.id.adb_dialog_restore_message);
        mDescription = view.findViewById(R.id.adb_dialog_restore_desc);
        mProgressBar = view.findViewById(R.id.adb_dialog_restore_progress_bar);

        mDeleteBackup = mBackupUri != null;

        if (savedInstanceState != null) {
            mReadyBackup = savedInstanceState.getBoolean(STATE_READY_BACKUP);
        }

        dialogBuilder.setTitle(R.string.adb_backup_restore_backup)
                .setPositiveButton(R.string.adb_backup_restore,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (getDynamicBackup() == null) {
                            return;
                        }

                        if (getBackup() != null && isValidBackup()) {
                            getDynamicBackup().onRestoreBackup(getBackup(), isDeleteBackup());
                        } else {
                            getDynamicBackup().showBackupDialog(Backup.Type.RESTORE);
                        }
                    }
                })
                .setNegativeButton(R.string.ads_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (isDeleteBackup() && getBackup() != null && getBackup().exists()) {
                            getBackup().delete();
                        }
                    }
                })
                .setView(view)
                .setViewRoot(view.findViewById(R.id.adb_dialog_restore_root));

        setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                verifyBackup();
            }
        });

        return dialogBuilder;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_READY_BACKUP, mReadyBackup);
    }

    @Override
    public void showDialog(@NonNull FragmentActivity fragmentActivity) {
        showDialog(fragmentActivity, TAG);
    }

    private void verifyBackup() {
        if (isReadyBackup()) {
            hideProgress(DynamicDialog.BUTTON_POSITIVE);

            return;
        }

        new ViewModelProvider(this).get(DynamicTaskViewModel.class).execute(
                new DynamicTask<Void, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        showProgress(DynamicDialog.BUTTON_POSITIVE);

                        mMessage.setText(R.string.adb_backup_restore_backup_verify);
                        mDescription.setText(R.string.adb_backup_restore_backup_verify_desc);
                    }

                    @Override
                    protected @Nullable Void doInBackground(@Nullable Void params) {
                        onVerifyBackup();

                        return null;
                    }

                    @Override
                    protected void onPostExecute(@Nullable DynamicResult<Void> result) {
                        super.onPostExecute(result);

                        hideProgress(DynamicDialog.BUTTON_POSITIVE);
                    }
                });
    }

    /**
     * This method will be called to verify the backup.
     */
    protected void onVerifyBackup() {
        if (getDynamicBackup() == null) {
            return;
        }

        if (isDeleteBackup()) {
            String baseFileName = DynamicFileUtils.getFileNameFromUri(
                    requireContext(), getBackupUri());
            mValidBackup = getDynamicBackup().onVerifyBackup(getBackupUri());

            if (baseFileName != null && isValidBackup()) {
                String fileName = File.separator + baseFileName;
                mBackup = new File(requireContext().getCacheDir() + fileName);
                DynamicFileUtils.writeToFile(requireContext(), getBackupUri(),
                        DynamicFileUtils.getUriFromFile(requireContext(), getBackup()));
            }
        }

        if (getBackup() != null) {
            mValidBackup = getDynamicBackup().onVerifyBackup(getBackup());
        }

        mReadyBackup = true;
    }

    /**
     * Show progress for this dialog.
     *
     * @param button The optional dialog button to be disabled.
     */
    public void showProgress(int button) {
        Dynamic.setVisibility(mProgressBar, View.VISIBLE);

        if (getDynamicDialog() != null) {
            Dynamic.setEnabled(getDynamicDialog().getButton(button), false);
        }

        mMessage.setText(R.string.adb_backup_restore_backup_verify);
        mDescription.setText(R.string.adb_backup_restore_backup_verify_desc);
    }

    /**
     * Hide progress for this dialog.
     *
     * @param button The optional button to be enabled.
     */
    public void hideProgress(int button) {
        Dynamic.setVisibility(mProgressBar, View.GONE);

        if (getDynamicDialog() != null) {
            Dynamic.setEnabled(getDynamicDialog().getButton(button), true);
        }

        if (getBackup() != null && isValidBackup()) {
            mMessage.setText(isDeleteBackup() ? getBackup().getName()
                    : DynamicFileUtils.getBaseName(getBackup().getName()));
            mDescription.setText(R.string.adb_backup_restore_backup_desc_alt);

            if (getDynamicDialog() != null) {
                getDynamicDialog().getButton(DynamicDialog.BUTTON_POSITIVE)
                        .setText(R.string.adb_backup_restore);
            }
        } else {
            mMessage.setText(R.string.adb_backup_invalid);
            mDescription.setText(R.string.adb_backup_restore_backup_verify_error);

            if (getDynamicDialog() != null) {
                getDynamicDialog().getButton(DynamicDialog.BUTTON_POSITIVE)
                        .setText(R.string.adb_backup_select);
            }
        }
    }

    /**
     * Get the backup listener used by this restore dialog.
     *
     * @return The backup listener used by this restore dialog.
     */
    public @Nullable DynamicBackup getDynamicBackup() {
        return mDynamicBackup;
    }

    /**
     * Set the backup listener for this restore dialog.
     *
     * @param dynamicBackup The backup listener to be set.
     *
     * @return The {@link DynamicRestoreDialog} object to allow for chaining of calls to set
     *         methods.
     */
    public @NonNull DynamicRestoreDialog setDynamicBackup(@Nullable DynamicBackup dynamicBackup) {
        this.mDynamicBackup = dynamicBackup;

        return this;
    }

    /**
     * Get the backup file used by this dialog.
     *
     * @return The backup file used by this dialog.
     */
    public @Nullable File getBackup() {
        return mBackup;
    }

    /**
     * Set the backup file for this dialog.
     *
     * @param backup The backup file to be set.
     *
     * @return The {@link DynamicRestoreDialog} object to allow for chaining of calls to set
     *         methods.
     */
    public @NonNull DynamicRestoreDialog setBackup(@NonNull File backup) {
        this.mBackup = backup;

        return this;
    }

    /**
     * Get the backup file URI used by this dialog.
     *
     * @return The backup file URI used by this dialog.
     */
    public @Nullable Uri getBackupUri() {
        return mBackupUri;
    }

    /**
     * Set the backup file URI for this dialog.
     *
     * @param backupUri The backup file URI to be set.
     *
     * @return The {@link DynamicRestoreDialog} object to allow for chaining of calls to set
     *         methods.
     */
    public @NonNull DynamicRestoreDialog setBackupUri(@Nullable Uri backupUri) {
        this.mBackupUri = backupUri;

        return this;
    }

    /**
     * Returns whether to delete the backup file after performing the restore operation.
     *
     * @return {@code true} to delete the backup file after performing the restore operation.
     */
    public boolean isDeleteBackup() {
        return mDeleteBackup;
    }

    /**
     * Set whether to delete the backup file after performing the restore operation.
     *
     * @param deleteBackup {@code true} to delete the backup file after performing the
     *                     restore operation.
     */
    public void setDeleteBackup(boolean deleteBackup) {
        this.mDeleteBackup = deleteBackup;
    }

    /**
     * Returns whether the backup is valid.
     *
     * @return {@code true} if the backup is valid.
     */
    public boolean isValidBackup() {
        return mValidBackup;
    }

    /**
     * Returns whether the backup is ready to be restored.
     *
     * @return {@code true} if the backup is ready to be restored.
     */
    public boolean isReadyBackup() {
        return mReadyBackup;
    }
}
