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

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.pranavpandey.android.dynamic.backup.Backup;
import com.pranavpandey.android.dynamic.backup.DynamicBackup;
import com.pranavpandey.android.dynamic.backup.R;
import com.pranavpandey.android.dynamic.backup.util.DynamicBackupUtils;
import com.pranavpandey.android.dynamic.support.Dynamic;
import com.pranavpandey.android.dynamic.support.dialog.DynamicDialog;
import com.pranavpandey.android.dynamic.support.dialog.fragment.DynamicDialogFragment;
import com.pranavpandey.android.dynamic.support.dialog.fragment.DynamicRenameDialog;
import com.pranavpandey.android.dynamic.support.model.DynamicMenu;
import com.pranavpandey.android.dynamic.support.popup.DynamicMenuPopup;
import com.pranavpandey.android.dynamic.support.popup.base.DynamicPopup;
import com.pranavpandey.android.dynamic.support.util.DynamicInputUtils;
import com.pranavpandey.android.dynamic.support.util.DynamicResourceUtils;
import com.pranavpandey.android.dynamic.support.view.base.DynamicItemView;
import com.pranavpandey.android.dynamic.util.DynamicFileUtils;
import com.pranavpandey.android.dynamic.util.DynamicIntentUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link DynamicDialogFragment} to perform backup related operations.
 */
public class DynamicBackupDialog extends DynamicDialogFragment {

    /**
     * Tag for this dialog fragment.
     */
    public static final String TAG = "DynamicBackupDialog";

    /**
     * State key to save the backup dialog type.
     */
    private static final String STATE_DIALOG_TYPE = "state_dialog_type";

    /**
     * State key to save the root view scroll position
     */
    private static final String STATE_VIEW_SCROLL_Y = "state_view_scroll_y";

    /**
     * State key to save the edit text string.
     */
    private static final String STATE_EDIT_TEXT_STRING = "state_edit_text_string";

    /**
     * State key to save the default backup name.
     */
    private static final String STATE_BACKUP_NAME_DEFAULT = "state_backup_name_default";

    /**
     * Backup type for this dialog.
     */
    private @Backup.Type int mType;

    /**
     * Root view scroll position.
     */
    private int mViewScrollY;

    /**
     * Backup location for this dialog.
     */
    private @Backup.Location int mBackupLocation;

    /**
     * Interface to dispatch various backup events.
     */
    private DynamicBackup mDynamicBackup;

    /**
     * Backups locations for the create dialog.
     */
    private List<Integer> mBackupLocations;

    /**
     * Default backup name for the create dialog.
     */
    private String mBackupNameDefault;

    /**
     * Backups adapter used by the restore dialog.
     */
    private BackupsAdapter mBackupsAdapter;

    /**
     * Backups array for the restore dialog.
     */
    private File[] mBackups;

    /**
     * Root view used by this dialog.
     */
    private NestedScrollView mViewRoot;

    /**
     * Text view to show the optional message.
     */
    private TextView mMessage;

    /**
     * Parent view to create new backup.
     */
    private ViewGroup mCreateView;

    /**
     * Spinner to select the backup location.
     */
    private DynamicItemView mSpinner;

    /**
     * Text input layout to show the edit text.
     */
    private TextInputLayout mTextInputLayout;

    /**
     * Edit text to get the backup name.
     */
    private EditText mEditText;

    /**
     * List view to show the backups created in the app storage.
     */
    private ListView mListView;

    /**
     * {@code true} if the file can be written to the sd card.
     */
    private boolean mFilePicker;

    /**
     * {@code true} if the file picker is available in the system.
     */
    private boolean mFilePickerStrict;

    /**
     * Initialize the new instance of this dialog fragment.
     *
     * @return An instance of {@link DynamicBackupDialog}.
     */
    public static @NonNull DynamicBackupDialog newInstance() {
        return new DynamicBackupDialog();
    }

    @Override
    protected @NonNull DynamicDialog.Builder onCustomiseBuilder(
            @NonNull DynamicDialog.Builder dialogBuilder,
            final @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(
                R.layout.adb_dialog_backup, new LinearLayout(requireContext()), false);

        mViewRoot = view.findViewById(R.id.adb_dialog_backup_root);
        mMessage = view.findViewById(R.id.adb_dialog_backup_message);
        mCreateView = view.findViewById(R.id.adb_backup_create);
        mSpinner = view.findViewById(R.id.adb_dialog_backup_spinner);
        mTextInputLayout = view.findViewById(R.id.adb_dialog_backup_input_layout);
        mEditText = view.findViewById(R.id.adb_backup_edit_text);
        mListView = view.findViewById(R.id.adb_dialog_backup_list);

        mBackupLocations = new ArrayList<>();
        mFilePicker = DynamicIntentUtils.isFilePicker(
                getContext(), getDynamicBackup() != null ? getDynamicBackup().getBackupMime()
                        : DynamicFileUtils.MIME_APPLICATION, true);
        mFilePickerStrict = DynamicIntentUtils.isFilePicker(
                getContext(), getDynamicBackup() != null ? getDynamicBackup().getBackupMime()
                        : DynamicFileUtils.MIME_APPLICATION, false);

        List<DynamicMenu> backupOptions = new ArrayList<>();
        if (mDynamicBackup.getBackupDir() != null) {
            backupOptions.add(new DynamicMenu(
                    DynamicResourceUtils.getDrawable(requireContext(), R.drawable.ads_ic_android),
                    getString(R.string.adb_backup_storage_app)));
            mBackupLocations.add(Backup.Location.APP);
        }
        if (isFilePicker()) {
            backupOptions.add(new DynamicMenu(
                    DynamicResourceUtils.getDrawable(requireContext(), R.drawable.ads_ic_storage),
                    getString(R.string.adb_backup_storage_device)));
            mBackupLocations.add(Backup.Location.STORAGE);
        }
        backupOptions.add(new DynamicMenu(
                DynamicResourceUtils.getDrawable(requireContext(), R.drawable.ads_ic_share),
                getString(R.string.adb_backup_storage_share)));
        mBackupLocations.add(Backup.Location.SHARE);

        mBackupNameDefault = DynamicBackupUtils.getBackupName();
        mBackupLocation = mDynamicBackup.getBackupLocation();
        if (!mBackupLocations.contains(mBackupLocation)) {
            mBackupLocation = mBackupLocations.get(0);
        }

        mSpinner.setIcon(backupOptions.get(mBackupLocations.indexOf(mBackupLocation)).getIcon());
        mSpinner.setTitle(backupOptions.get(mBackupLocations.indexOf(mBackupLocation)).getTitle());
        Dynamic.setOnClickListener(mSpinner, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DynamicPopup popup = new DynamicMenuPopup(v, backupOptions,
                        mBackupLocations.indexOf(mBackupLocation),
                        new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent,
                            View view, int position, long id) {
                        mBackupLocation = mBackupLocations.get(position);
                        saveBackupLocation();

                        if (mSpinner != null) {
                            mSpinner.setIcon(backupOptions.get(position).getIcon());
                            mSpinner.setTitle(backupOptions.get(position).getTitle());
                        }
                    }
                }).build();

                popup.show();

                if (popup.getPopupWindow() != null) {
                    popup.getPopupWindow().setOnDismissListener(
                            new PopupWindow.OnDismissListener() {
                                @Override
                                public void onDismiss() {
                                    if (mSpinner != null) {
                                        mSpinner.setColor();
                                    }
                                }
                            });
                }
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (getDynamicDialog() == null) {
                    return;
                }

                getDynamicDialog().getButton(DynamicDialog.BUTTON_POSITIVE)
                        .setEnabled(!TextUtils.isEmpty(s));
            }
        });

        if (savedInstanceState != null) {
            mType = savedInstanceState.getInt(STATE_DIALOG_TYPE);
            mBackupNameDefault = savedInstanceState.getString(STATE_BACKUP_NAME_DEFAULT);
            mViewScrollY = savedInstanceState.getInt(STATE_VIEW_SCROLL_Y, 0);
        }

        if (getType() == Backup.Type.RESTORE) {
            dialogBuilder.setTitle(R.string.adb_backup_restore);
            dialogBuilder.setNeutralButton(R.string.adb_backup_delete_all,
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (isBackupAvailable()) {
                        mDynamicBackup.onDeleteAllBackups();
                    } else if (isFilePickerStrict()) {
                        mDynamicBackup.onImportBackup();
                    } else {
                        mDynamicBackup.showBackupDialog(Backup.Type.BACKUP);
                    }
                }
            });
        } else {
            dialogBuilder.setTitle(R.string.adb_backup)
                    .setPositiveButton(R.string.adb_backup_create,
                        new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) { }
                    })
                    .setNeutralButton(R.string.adb_backup_modify,
                        new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) { }
                    });
        }

        dialogBuilder.setNegativeButton(R.string.ads_cancel, null)
                .setView(view).setViewRoot(mViewRoot);

        setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                if (savedInstanceState != null) {
                    mEditText.setText(savedInstanceState.getString(STATE_EDIT_TEXT_STRING));
                    mEditText.setSelection(mEditText.getText().length());
                } else {
                    mEditText.setText(mBackupNameDefault);
                }

                if (getType() == Backup.Type.RESTORE) {
                    restoreBackup();
                } else {
                    if (getType() == Backup.Type.MODIFY) {
                        modifyBackup();
                    } else {
                        newBackup();
                    }
                }

                if (getType() != Backup.Type.RESTORE) {
                    if (getDynamicDialog() == null) {
                        return;
                    }

                    getDynamicDialog().getButton(DynamicDialog.BUTTON_POSITIVE)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mDynamicBackup.isBackupExist(
                                            mEditText.getText().toString(), mBackupLocation)) {
                                        if (mTextInputLayout != null) {
                                            mTextInputLayout.setError(requireContext()
                                                    .getString(R.string.adb_backup_exists));
                                            return;
                                        } else {
                                            mDynamicBackup.onBackupError(null, mBackupLocation);
                                        }
                                    } else {
                                        mDynamicBackup.onCreateBackup(
                                                mEditText.getText().toString(), mBackupLocation);
                                    }

                                    getDynamicDialog().dismiss();
                                }
                            });

                    if (getDynamicDialog() == null) {
                        return;
                    }

                    getDynamicDialog().getButton(DynamicDialog.BUTTON_NEUTRAL)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (getType() == Backup.Type.BACKUP) {
                                        modifyBackup();
                                    } else {
                                        newBackup();
                                    }
                                }
                            });
                } else {
                    setRestoreOptions();
                }
            }
        });

        return dialogBuilder;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_DIALOG_TYPE, mType);
        outState.putString(STATE_EDIT_TEXT_STRING, mEditText.getText().toString());
        outState.putString(STATE_BACKUP_NAME_DEFAULT, mBackupNameDefault);
        outState.putInt(STATE_VIEW_SCROLL_Y, mViewRoot.getScrollY());
    }

    @Override
    public void showDialog(@NonNull FragmentActivity fragmentActivity) {
        showDialog(fragmentActivity, TAG);
    }

    /**
     * Get the backup type used by this dialog.
     *
     * @return The backup type used by this dialog.
     */
    public @Backup.Type int getType() {
        return mType;
    }

    /**
     * Set the backup type for this dialog.
     *
     * @param type The backup type to be set.
     *
     * @return The {@link DynamicBackupDialog} object to allow for chaining of calls to set
     *         methods.
     */
    public @NonNull DynamicBackupDialog setType(@Backup.Type int type) {
        this.mType = type;

        return this;
    }

    /**
     * Get the backup listener used by this dialog.
     *
     * @return The backup listener used by this dialog.
     */
    public @Nullable DynamicBackup getDynamicBackup() {
        return mDynamicBackup;
    }

    /**
     * Set the backup listener for this dialog.
     *
     * @param dynamicBackup The backup listener to be set.
     *
     * @return The {@link DynamicBackupDialog} object to allow for chaining of calls to set
     *         methods.
     */
    public @NonNull DynamicBackupDialog setDynamicBackup(@Nullable DynamicBackup dynamicBackup) {
        this.mDynamicBackup = dynamicBackup;

        return this;
    }

    /**
     * Update the dialog layout to create new backup.
     */
    private void newBackup() {
        setType(Backup.Type.BACKUP);
        Dynamic.setVisibility(mMessage, View.GONE);
        Dynamic.setVisibility(mListView, View.GONE);
        Dynamic.setVisibility(mCreateView, View.VISIBLE);

        if (getDynamicDialog() != null) {
            getDynamicDialog().getButton(DynamicDialog.BUTTON_NEUTRAL)
                    .setText(R.string.adb_backup_modify);
            Dynamic.setVisibility(getDynamicDialog().getButton(
                    DynamicDialog.BUTTON_POSITIVE), View.VISIBLE);
        }

        if (mBackupNameDefault.equals(mEditText.getText().toString())) {
            mEditText.selectAll();
            DynamicInputUtils.showSoftInput(mEditText);
        }
    }

    /**
     * Update the dialog layout to modify a backup.
     */
    private void modifyBackup() {
        setType(Backup.Type.MODIFY);
        Dynamic.setVisibility(mCreateView, View.GONE);
        Dynamic.setVisibility(mMessage, View.VISIBLE);
        DynamicInputUtils.hideSoftInput(mEditText);

        if (getDynamicDialog() != null) {
            getDynamicDialog().getButton(DynamicDialog.BUTTON_NEUTRAL)
                    .setText(R.string.adb_backup_new);
            Dynamic.setVisibility(getDynamicDialog().getButton(
                    DynamicDialog.BUTTON_POSITIVE), View.GONE);
        }

        setBackupsAdapter();
    }

    /**
     * Set the backup adapter to show a list of backups.
     */
    public void setBackupsAdapter() {
        mBackupsAdapter = new BackupsAdapter(requireContext());
        File backupsDir = null;

        if (mDynamicBackup.getBackupDir() != null) {
            backupsDir = new File(mDynamicBackup.getBackupDir());
        }

        if (backupsDir != null && backupsDir.exists()) {
            mBackups = backupsDir.listFiles();
        }

        if (mBackups != null && mBackups.length > 0) {
            mBackupsAdapter.addAll(DynamicBackupUtils.sortBackups(mBackups));
            mListView.setAdapter(mBackupsAdapter);

            Dynamic.setVisibility(mListView, View.VISIBLE);
            if (getType() == Backup.Type.MODIFY) {
                mMessage.setText(R.string.adb_backup_modify_desc);
            } else {
                mMessage.setText(R.string.adb_backup_restore_desc);
            }

            mViewRoot.post(new Runnable() {
                @Override
                public void run() {
                    mViewRoot.scrollTo(0, mViewScrollY);
                }
            });
        } else {
            Dynamic.setVisibility(mListView, View.GONE);
            mMessage.setText(getType() == Backup.Type.MODIFY
                    ? DynamicBackupUtils.getNoBackupHint(requireContext())
                    : DynamicBackupUtils.getNoBackupHintImport(requireContext()));
        }

        setRestoreOptions();
    }

    /**
     * Set the backup restore options for this dialog.
     */
    private void setRestoreOptions() {
        if (getType() == Backup.Type.RESTORE) {
            if (getDynamicDialog() != null) {
                Button button = getDynamicDialog().getButton(DynamicDialog.BUTTON_NEUTRAL);
                button.setText(isBackupAvailable()
                        ? R.string.adb_backup_delete_all : R.string.adb_backup_import);

                if (!isFilePickerStrict() && !isBackupAvailable()) {
                    mMessage.setText(DynamicBackupUtils.getNoBackupHint(requireContext()));
                    button.setText(R.string.adb_backup_create);
                }
            }
        }
    }

    /**
     * Checks whether the previously saved backups are available.
     *
     * @return {@code true} if previously saved backups are available.
     */
    private boolean isBackupAvailable() {
        return mBackups != null && mBackups.length > 0;
    }

    /**
     * Set the backup restore layout for this dialog.
     */
    private void restoreBackup() {
        setType(Backup.Type.RESTORE);
        Dynamic.setVisibility(mCreateView, View.GONE);
        Dynamic.setVisibility(mMessage, View.VISIBLE);
        DynamicInputUtils.hideSoftInput(mEditText);

        setBackupsAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        saveBackupLocation();
    }

    /**
     * Save the backup location.
     */
    public void saveBackupLocation() {
        if (mSpinner != null && !mBackupLocations.isEmpty()) {
            mDynamicBackup.setBackupLocation(mBackupLocation);
        }
    }

    /**
     * Returns the backups adapter used by the restore dialog.
     *
     * @return The backups adapter used by the restore dialog.
     */
    public @Nullable BackupsAdapter getBackupsAdapter() {
        return mBackupsAdapter;
    }

    /**
     * Checks whether the file can be written to the sd card.
     *
     * @return {@code true} if the file can be written to the sd card.
     */
    public boolean isFilePicker() {
        return mFilePicker;
    }

    /**
     * Checks whether a file picker is available in the system.
     *
     * @return {@code true} if a file picker is available in the system.
     */
    public boolean isFilePickerStrict() {
        return mFilePickerStrict;
    }

    /**
     * Array adapter to show the list of backups.
     */
    class BackupsAdapter extends ArrayAdapter<File> {

        BackupsAdapter(@NonNull Context context) {
            super(context, 0);
        }

        @Override
        public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(requireContext()).inflate(
                        R.layout.ads_layout_row_item_dialog, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final File file = getItem(position);
            if (file != null) {
                final String backupName = DynamicFileUtils.getBaseName(file.getName());

                holder.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getType() == Backup.Type.MODIFY) {
                            if (backupName != null) {
                                mDynamicBackup.onCreateBackup(
                                        backupName, Backup.Location.APP_MODIFY);
                            } else {
                                mDynamicBackup.onBackupError(file, Backup.Location.APP_MODIFY);
                            }
                        } else {
                            mDynamicBackup.onRestoreBackup(file);
                        }

                        dismiss();
                    }
                });

                Dynamic.set(holder.getTitle(), backupName);
                Dynamic.set(holder.getSubtitle(),
                        DynamicBackupUtils.getLastModified(requireContext(), file));

                if (getType() == Backup.Type.RESTORE) {
                    Dynamic.setVisibility(holder.getOptions(), View.VISIBLE);
                    Dynamic.setOnClickListener(holder.getOptions(),
                            new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DynamicMenuPopup popup = new DynamicMenuPopup(view,
                                    DynamicBackupUtils.getBackupOptionsIcons(requireContext()),
                                    DynamicBackupUtils.getBackupOptions(requireContext()),
                                    new boolean[] { false, false, true },
                                    new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent,
                                        View view, int position, long id) {
                                    switch (position) {
                                        case Backup.Option.RENAME:
                                            showRenameDialog(file, backupName);
                                            break;
                                        case Backup.Option.SHARE:
                                            mDynamicBackup.onShareBackup(file);
                                            DynamicBackupDialog.this.dismiss();
                                            break;
                                        case Backup.Option.DELETE:
                                            deleteBackup(holder.getOptions(), file, backupName);
                                            break;
                                    }
                                }
                            });

                            popup.setTitle(DynamicFileUtils.getBaseName(file.getName()));
                            popup.build().show();
                        }
                    });
                } else {
                    Dynamic.setVisibility(holder.getOptions(), View.GONE);
                }
            }

            return convertView;
        }

        /**
         * Show popup to delete the backup.
         *
         * @param anchor The anchor view for the popup.
         * @param file The backup file to be deleted.
         * @param name The backup name.
         */
        private void deleteBackup(@Nullable View anchor,
                @Nullable File file, @Nullable String name) {
            if (anchor == null || file == null || name == null) {
                if (getDynamicBackup() != null) {
                    getDynamicBackup().onBackupError(file, getDynamicBackup().getBackupLocation());
                }

                return;
            }

            DynamicMenuPopup popup = new DynamicMenuPopup(anchor,
                    DynamicResourceUtils.convertToDrawableResArray(
                            requireContext(), R.array.ads_confirm_icons),
                    getResources().getStringArray(R.array.ads_popup_delete),
                    new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    switch (position) {
                        case 0:
                            break;
                        case 1:
                            mDynamicBackup.onDeleteBackup(file, true);
                            setBackupsAdapter();
                            break;
                    }
                }
            });

            popup.setTitle(name);
            popup.setViewType(DynamicPopup.Type.LIST);
            popup.build().show();
        }
    }

    /**
     * Show a dialog to rename the backup.
     *
     * @param file The backup file to be renamed.
     * @param name The current backup name.
     */
    private void showRenameDialog(@Nullable File file, @Nullable String name) {
        if (file == null || name == null) {
            if (mDynamicBackup != null) {
                mDynamicBackup.onBackupError(file, mDynamicBackup.getBackupLocation());
            }

            return;
        }

        DynamicRenameDialog.newInstance().setName(name)
                .setHelperText(getString(R.string.adb_backup_replace))
                .setRenameDialogListener(new DynamicRenameDialog.RenameListener() {
                    @Override
                    public void onRename(@NonNull String newName) {
                        mDynamicBackup.onRenameBackup(file, newName);
                        setBackupsAdapter();
                    }
                })
                .setBuilder(new DynamicDialog.Builder(requireContext())
                        .setTitle(R.string.adb_backup))
                .showDialog(requireActivity());
    }

    /**
     * View holder class to hold the backup view.
     */
    static class ViewHolder {

        /**
         * Backup view root.
         */
        private final ViewGroup root;

        /**
         * Text view to show the backup title.
         */
        private final TextView title;

        /**
         * Text view to show the backup subtitle.
         */
        private final TextView subtitle;

        /**
         * Image view to show the backup options.
         */
        private final ImageView options;

        /**
         * Constructor to initialize views from the supplied layout.
         *
         * @param view The view for this view holder.
         */
        ViewHolder(@NonNull View view) {
            root = view.findViewById(R.id.ads_item_root);
            title = view.findViewById(R.id.ads_item_title);
            subtitle = view.findViewById(R.id.ads_item_subtitle);
            options = view.findViewById(R.id.ads_item_options);
        }

        /**
         * Get the backup view root.
         *
         * @return The backup view root.
         */
        @NonNull ViewGroup getRoot() {
            return root;
        }

        /**
         * Get the text view to show the backup title.
         *
         * @return The text view to show the backup title.
         */
        @Nullable TextView getTitle() {
            return title;
        }

        /**
         * Get the text view to show the backup subtitle.
         *
         * @return The text view to show the backup subtitle.
         */
        @Nullable TextView getSubtitle() {
            return subtitle;
        }

        /**
         * Get the image view to show the backup options.
         *
         * @return The text view to show the backup options.
         */
        @Nullable ImageView getOptions() {
            return options;
        }
    }
}
