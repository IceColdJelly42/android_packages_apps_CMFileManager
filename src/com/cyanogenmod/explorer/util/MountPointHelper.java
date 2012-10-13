/*
 * Copyright (C) 2012 The CyanogenMod Project
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
package com.cyanogenmod.explorer.util;

import android.util.Log;

import com.cyanogenmod.explorer.ExplorerApplication;
import com.cyanogenmod.explorer.commands.MountExecutable;
import com.cyanogenmod.explorer.console.Console;
import com.cyanogenmod.explorer.model.DiskUsage;
import com.cyanogenmod.explorer.model.FileSystemObject;
import com.cyanogenmod.explorer.model.MountPoint;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A helper class with useful methods for deal with mount points.
 */
public final class MountPointHelper {

    private static final String TAG = "MountPointHelper"; //$NON-NLS-1$

    private static final List<String> ALLOWED_FS_TYPE = Arrays.asList(new String[]{
                                                "rootfs", //$NON-NLS-1$
                                                "tmpfs",  //$NON-NLS-1$
                                                "vfat",   //$NON-NLS-1$
                                                "ext2",   //$NON-NLS-1$
                                                "ext3",   //$NON-NLS-1$
                                                "ext4"    //$NON-NLS-1$
                                                    });

    /**
     * Constructor of <code>MountPointHelper</code>.
     */
    private MountPointHelper() {
        super();
    }

    /**
     * Method that retrieve the mount point information for a directory.
     *
     * @param dir The directory of which recovers his mount point information
     * @return MountPoint The mount point information
     */
    public static MountPoint getMountPointFromDirectory(FileSystemObject dir) {
        return getMountPointFromDirectory(dir.getFullPath());
    }

    /**
     * Method that retrieve the mount point information for a directory.
     *
     * @param dir The directory of which recovers his mount point information
     * @return MountPoint The mount point information
     */
    public static MountPoint getMountPointFromDirectory(String dir) {
        try {
            return getMountPointFromDirectory(ExplorerApplication.getBackgroundConsole(), dir);
        } catch (Exception e) {
            Log.e(TAG, "Failed to retrieve mount point information.", e); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Method that retrieve the mount point information for a directory.
     *
     * @param console The console in which realize the operation
     * @param dir The directory of which recovers his mount point information
     * @return MountPoint The mount point information
     */
    public static MountPoint getMountPointFromDirectory(Console console, String dir) {
        try {
            //Retrieve the mount points
            List<MountPoint> mps =
                    CommandHelper.getMountPoints(null, console);

            //Sort mount points in reverse order, needed for avoid
            //found an incorrect that matches the name
            Collections.sort(mps, new Comparator<MountPoint>() {
                @Override
                public int compare(MountPoint lhs, MountPoint rhs) {
                    return lhs.compareTo(rhs) * -1;
                }
            });

            //Search for the mount point information
            int cc = mps.size();
            for (int i = 0; i < cc; i++) {
                MountPoint mp = mps.get(i);
                if (dir.startsWith(mp.getMountPoint())) {
                    return mp;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to retrieve the mount point information", e); //$NON-NLS-1$
        }

        //No mount point found
        return null;
    }

    /**
     * Method that retrieve information about the disk usage of the mount point.
     *
     * @param mp The mount point
     * @return DiskUsage The disk usage information
     */
    public static DiskUsage getMountPointDiskUsage(MountPoint mp) {
        return getMountPointDiskUsage(ExplorerApplication.getBackgroundConsole(), mp);
    }

    /**
     * Method that retrieve information about the disk usage of the mount point.
     *
     * @param console The console in which realize the operation
     * @param mp The mount point
     * @return DiskUsage The disk usage information
     */
    public static DiskUsage getMountPointDiskUsage(Console console, MountPoint mp) {
        try {
            //Retrieve the mount points
            return CommandHelper.getDiskUsage(null, mp.getMountPoint(), console);

        } catch (Exception e) {
            Log.e(TAG,
                    String.format("Fail to load disk usage of mount point: %s",  //$NON-NLS-1$
                            mp.getMountPoint()), e);
        }

        //No mount point found
        return null;
    }

    /**
     * Method that returns if the filesystem is mounted as readonly.
     *
     * @param mp The mount point to check
     * @return boolean If the mount point is mounted as readonly
     */
    public static boolean isReadOnly(MountPoint mp) {
        try {
            return mp.getOptions().startsWith(MountExecutable.READONLY);
        } catch (Exception e) {
            Log.e(TAG, "Method \"isReadOnly\" failed.", e); //$NON-NLS-1$
        }

        //On fail is more secure consider it as read-only
        return true;
    }

    /**
     * Method that returns if the filesystem is mounted as read-write.
     *
     * @param mp The mount point to check
     * @return boolean If the mount point is mounted as read-write
     */
    public static boolean isReadWrite(MountPoint mp) {
        try {
            return mp.getOptions().startsWith(MountExecutable.READWRITE);
        } catch (Exception e) {
            Log.e(TAG, "Method \"isReadWrite\" failed.", e); //$NON-NLS-1$
        }

        //On fail is more secure consider it as read-only
        return false;
    }

    /**
     * Method that returns if the filesystem can be mounted.
     *
     * @param mp The mount point to check
     * @return boolean If the mount point can be mounted
     */
    public static boolean isMountAllowed(MountPoint mp) {
        return ALLOWED_FS_TYPE.contains(mp.getType());
    }
}
