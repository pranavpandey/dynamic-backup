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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Constant values for the backup.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Backup {

    /**
     * Key for the backup location preference.
     */
    String KEY_LOCATION = "adb_pref_backup_location";

    /**
     * Default mime type for the backup file.
     */
    String MIME = "application/octet-stream";

    /**
     * Zip mime type for the backup file.
     */
    String MIME_ZIP = "application/zip";

    /**
     * Default name for the backup file.
     */
    String NAME = "backup";

    /**
     * Constant values for the backup type.
     */
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {

        /**
         * Constant value for deleting all the backups.
         */
        int DELETE_ALL = -2;

        /**
         * Constant value for deleting the backup.
         */
        int DELETE = -1;

        /**
         * Constant value for creating the backup.
         */
        int BACKUP = 0;

        /**
         * Constant value for creating the backup file.
         */
        int BACKUP_FILE = 1;

        /**
         * Constant value for restoring the backup.
         */
        int RESTORE = 5;

        /**
         * Constant value for modifying the backup.
         */
        int MODIFY = 10;

        /**
         * Constant value for renaming the backup.
         */
        int RENAME = 15;
    }

    /**
     * Constant values for the backup storage location.
     */
    @Retention(RetentionPolicy.SOURCE)
    @interface Location {

        /**
         * Constant value for the app storage.
         */
        int APP = 0;

        /**
         * Constant value for the device storage.
         */
        int STORAGE = 1;

        /**
         * Constant value to share the backup.
         */
        int SHARE = 2;

        /**
         * Constant value to overwrite the backup.
         */
        int APP_MODIFY = 3;
    }

    /**
     * Constant values for the backup storage location.
     */
    @Retention(RetentionPolicy.SOURCE)
    @interface Option {

        /**
         * Constant value to rename the backup.
         */
        int RENAME = 0;

        /**
         * Constant value to share the backup.
         */
        int SHARE = 1;

        /**
         * Constant value to delete the backup.
         */
        int DELETE = 2;
    }
}
