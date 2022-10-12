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

package com.pranavpandey.android.dynamic.backup.task;

import androidx.annotation.Nullable;

import com.pranavpandey.android.dynamic.backup.Backup;
import com.pranavpandey.android.dynamic.backup.BackupConfig;
import com.pranavpandey.android.dynamic.backup.DynamicBackup;
import com.pranavpandey.android.dynamic.util.DynamicFileUtils;
import com.pranavpandey.android.dynamic.util.concurrent.DynamicResult;

import java.io.File;

/**
 * A {@link DynamicBackupTask} to delete all the backups stored in the app storage.
 */
public class BackupDeleteTask extends DynamicBackupTask<Boolean> {

    /**
     * Constructor to initialize an object of this class.
     *
     * @param dynamicBackup The dynamic backup to receive the callbacks.
     * @param backupConfig The backup configurations.
     */
    public BackupDeleteTask(@Nullable DynamicBackup dynamicBackup,
            @Nullable BackupConfig backupConfig) {
        super(dynamicBackup, backupConfig);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (getDynamicBackup() == null || getBackupConfig() == null) {
            return;
        }

        getDynamicBackup().onSetProgress(getBackupConfig(), true);
    }

    @Override
    protected @Nullable Boolean doInBackground(@Nullable Void params) {
        if (getDynamicBackup() == null || getBackupConfig() == null
                || !getBackupConfig().isDelete()) {
            return false;
        }

        if (getBackupConfig().getType() == Backup.Type.DELETE
                && getBackupConfig().getFile() != null) {
            return getBackupConfig().getFile().delete();
        } else if (getBackupConfig().getType() == Backup.Type.DELETE_ALL
                && getDynamicBackup().getBackupDir() != null) {
            return DynamicFileUtils.deleteDirectory(new File(getDynamicBackup().getBackupDir()));
        } else {
            return false;
        }
    }

    @Override
    protected void onPostExecute(@Nullable DynamicResult<Boolean> result) {
        super.onPostExecute(result);

        if (getDynamicBackup() == null || getBackupConfig() == null) {
            return;
        }

        getDynamicBackup().onSetProgress(getBackupConfig(), false);

        if (getBackupConfig().getType() == Backup.Type.DELETE
                && getBackupConfig().getFile() != null
                && result instanceof DynamicResult.Success && getBooleanResult(result)) {
            getDynamicBackup().onBackupDeleted(DynamicFileUtils.getBaseName(
                    getBackupConfig().getFile().getName()));
        } else if (getBackupConfig().getType() == Backup.Type.DELETE_ALL) {
            getDynamicBackup().onAllBackupsDeleted(result instanceof DynamicResult.Success
                    && getBooleanResult(result));
        }
    }
}
