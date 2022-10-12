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

import androidx.annotation.Nullable;

import java.io.File;

import static com.pranavpandey.android.dynamic.backup.Backup.Location.APP;
import static com.pranavpandey.android.dynamic.backup.Backup.Type.BACKUP;

/**
 * Backup configuration to create or restore a backup.
 */
public class BackupConfig {

    /**
     * Name for the backup.
     */
    private final String name;

    /**
     * Type for the backup.
     */
    private final @Backup.Type int type;

    /**
     * location for the backup.
     */
    private final @Backup.Location int location;

    /**
     * File for the backup.
     */
    private final File file;

    /**
     * {@code true} to delete the backup file after performing the restore operation.
     */
    private final boolean delete;

    /**
     * Constructor to initialize an object of this class.
     *
     * @param name The name for the backup.
     * @param location The location for the backup.
     */
    public BackupConfig(@Nullable String name, @Backup.Location int location) {
        this(name, BACKUP, location, null, false);
    }

    /**
     * Constructor to initialize an object of this class.
     *
     * @param type The type for the backup.
     * @param file The file for the backup.
     * @param delete {@code true} to delete the backup file after performing the
     *               restore operation.
     */
    public BackupConfig(@Backup.Type int type, @Nullable File file, boolean delete) {
        this(null, type, APP, file, delete);
    }

    /**
     * Constructor to initialize an object of this class.
     *
     * @param type The type for the backup.
     * @param file The file for the backup.
     * @param name The name for the backup.
     */
    public BackupConfig(@Backup.Type int type, @Nullable File file, @Nullable String name) {
        this(name, type, APP, file, false);
    }

    /**
     * Constructor to initialize an object of this class.
     *
     * @param name The name for the backup.
     * @param type The type for the backup.
     * @param location The location for the backup.
     * @param file The file for the backup.
     * @param delete {@code true} to delete the backup file after performing the
     *               restore operation.
     */
    public BackupConfig(@Nullable String name, @Backup.Type int type,
            @Backup.Location int location, @Nullable File file, boolean delete) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.file = file;
        this.delete = delete;
    }

    /**
     * Get the name for the backup.
     *
     * @return The name for the backup.
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * Get the type for the backup.
     *
     * @return The type for the backup.
     */
    public @Backup.Type int getType() {
        return type;
    }

    /**
     * Get the location for the backup.
     *
     * @return The location for the backup.
     */
    public @Backup.Location int getLocation() {
        return location;
    }

    /**
     * Get the file for the backup.
     *
     * @return The file for the backup.
     */
    public @Nullable File getFile() {
        return file;
    }

    /**
     * Returns whether to delete the backup file after performing the restore operation.
     *
     * @return {@code true} to delete the backup file after performing the restore operation.
     */
    public boolean isDelete() {
        return delete;
    }
}
