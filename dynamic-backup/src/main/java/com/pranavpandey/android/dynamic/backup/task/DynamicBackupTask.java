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
import com.pranavpandey.android.dynamic.util.concurrent.DynamicTask;

/**
 * A {@link DynamicTask} to create backup.
 */
public abstract class DynamicBackupTask<T> extends DynamicTask<Void, Void, T> {

    /**
     * Dynamic backup to receive the callbacks.
     */
    private final DynamicBackup mDynamicBackup;

    /**
     * Backup configurations to perform operations accordingly.
     */
    private final BackupConfig mBackupConfig;

    /**
     * Constructor to initialize an object of this class.
     *
     * @param dynamicBackup The dynamic backup to receive the callbacks.
     * @param backupConfig The backup configurations.
     */
    public DynamicBackupTask(@Nullable DynamicBackup dynamicBackup,
            @Nullable BackupConfig backupConfig) {
        this.mDynamicBackup = dynamicBackup;
        this.mBackupConfig = backupConfig;
    }

    /**
     * Get the dynamic backup used by this task.
     *
     * @return The dynamic backup used by this task.
     */
    public @Nullable DynamicBackup getDynamicBackup() {
        return mDynamicBackup;
    }

    /**
     * Get the dynamic backup configurations used by this task.
     *
     * @return The dynamic backup configurations used by this task.
     */
    public @Nullable BackupConfig getBackupConfig() {
        return mBackupConfig;
    }
}
