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

import com.pranavpandey.android.dynamic.backup.BackupConfig;
import com.pranavpandey.android.dynamic.backup.DynamicBackup;
import com.pranavpandey.android.dynamic.util.concurrent.DynamicResult;

import java.io.File;

/**
 * A {@link DynamicBackupTask} to create backup.
 */
public abstract class BackupCreateTask extends DynamicBackupTask<File> {
    
    /**
     * Constructor to initialize an object of this class.
     *
     * @param dynamicBackup The dynamic backup to receive the callbacks.
     * @param backupConfig The backup configurations.
     */
    public BackupCreateTask(@Nullable DynamicBackup dynamicBackup,
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
    protected void onPostExecute(@Nullable DynamicResult<File> result) {
        super.onPostExecute(result);

        if (getDynamicBackup() == null || getBackupConfig() == null) {
            return;
        }

        getDynamicBackup().onSetProgress(getBackupConfig(), false);

        if (result instanceof DynamicResult.Success) {
            getDynamicBackup().onBackupCreated(result.getData(), getBackupConfig().getLocation());
        } else {
            getDynamicBackup().onBackupError(null, getBackupConfig().getLocation());
        }
    }
}
