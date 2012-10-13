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

package com.cyanogenmod.explorer.tasks;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.model.DiskUsage;
import com.cyanogenmod.explorer.model.MountPoint;
import com.cyanogenmod.explorer.util.MountPointHelper;

/**
 * A class for recovery information about filesystem status (mount point, disk usage, ...).
 */
public class FilesystemAsyncTask extends AsyncTask<String, Integer, Boolean> {

    private static final String TAG = "FilesystemAsyncTask"; //$NON-NLS-1$

    /**
     * @hide
     */
    final ImageView mMountPointInfo;
    /**
     * @hide
     */
    final ProgressBar mDiskUsageInfo;
    /**
     * @hide
     */
    final int mFreeDiskSpaceWarningLevel;
    private boolean mRunning;

    /**
     * @hide
     */
    static int sColorFilterNormal;
    /**
     * @hide
     */
    static int sColorFilterWarning;

    /**
     * Constructor of <code>FilesystemAsyncTask</code>.
     *
     * @param context The current context
     * @param mountPointInfo The mount point info view
     * @param diskUsageInfo The mount point info view
     * @param freeDiskSpaceWarningLevel The free disk space warning level
     */
    public FilesystemAsyncTask(
            Context context, ImageView mountPointInfo,
            ProgressBar diskUsageInfo, int freeDiskSpaceWarningLevel) {
        super();
        this.mMountPointInfo = mountPointInfo;
        this.mDiskUsageInfo = diskUsageInfo;
        this.mFreeDiskSpaceWarningLevel = freeDiskSpaceWarningLevel;
        this.mRunning = false;

        if (sColorFilterNormal == 0 || sColorFilterWarning == 0) {
            Resources res = context.getResources();
            sColorFilterNormal = res.getColor(R.color.disk_usage_color_filter_normal);
            sColorFilterWarning = res.getColor(R.color.disk_usage_color_filter_warning);
        }

    }

    /**
     * Method that returns if there is a task running.
     *
     * @return boolean If there is a task running
     */
    public boolean isRunning() {
        return this.mRunning;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean doInBackground(String... params) {
        //Running
        this.mRunning = true;

        //Extract the directory from arguments
        String dir = params[0];

        //Extract filesystem mount point from directory
        if (isCancelled()) {
            return Boolean.TRUE;
        }
        final MountPoint mp = MountPointHelper.getMountPointFromDirectory(dir);
        if (mp == null) {
            //There is no information about
            if (isCancelled()) {
                return Boolean.TRUE;
            }
            this.mMountPointInfo.post(new Runnable() {
                @Override
                public void run() {
                    FilesystemAsyncTask.this.mMountPointInfo.setImageResource(
                            R.drawable.ic_holo_light_fs_warning);
                    FilesystemAsyncTask.this.mMountPointInfo.setTag(null);
                }
            });
        } else {
            //Set image icon an save the mount point info
            if (isCancelled()) {
                return Boolean.TRUE;
            }
            this.mMountPointInfo.post(new Runnable() {
                @Override
                public void run() {
                    FilesystemAsyncTask.this.mMountPointInfo.setImageResource(
                            MountPointHelper.isReadOnly(mp)
                            ? R.drawable.ic_holo_light_fs_locked
                            : R.drawable.ic_holo_light_fs_unlocked);
                    FilesystemAsyncTask.this.mMountPointInfo.setTag(mp);
                }
            });

            //Load information about disk usage
            if (isCancelled()) {
                return Boolean.TRUE;
            }
            this.mDiskUsageInfo.post(new Runnable() {
                @Override
                public void run() {
                    DiskUsage du = null;
                    try {
                         du = MountPointHelper.getMountPointDiskUsage(mp);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to retrieve disk usage information", e); //$NON-NLS-1$
                        du = new DiskUsage(
                                mp.getMountPoint(), 0, 0, 0);
                    }
                    int usage = 0;
                    if (du != null && du.getTotal() != 0) {
                        usage = (int)(du.getUsed() * 100 / du.getTotal());
                        FilesystemAsyncTask.this.mDiskUsageInfo.setProgress(usage);
                        FilesystemAsyncTask.this.mDiskUsageInfo.setTag(du);
                    } else {
                        usage = du == null ? 0 : 100;
                        FilesystemAsyncTask.this.mDiskUsageInfo.setProgress(usage);
                        FilesystemAsyncTask.this.mDiskUsageInfo.setTag(null);
                    }

                    // Advise about diskusage (>=mFreeDiskSpaceWarningLevel) with other color
                    int filter =
                            usage >= FilesystemAsyncTask.this.mFreeDiskSpaceWarningLevel ?
                            sColorFilterWarning :
                            sColorFilterNormal;
                    FilesystemAsyncTask.this.mDiskUsageInfo.
                                getProgressDrawable().setColorFilter(
                                        new PorterDuffColorFilter(filter, Mode.MULTIPLY));
                }
            });
        }
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(Boolean result) {
        this.mRunning = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCancelled(Boolean result) {
        this.mRunning = false;
        super.onCancelled(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCancelled() {
        this.mRunning = false;
        super.onCancelled();
    }

}
