/*
 * Copyright (C) 2017 CypherOS
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
package com.aoscp.hardware;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Range;

import com.android.internal.annotations.VisibleForTesting;

import com.aoscp.param.ContextConstants;

import java.io.UnsupportedEncodingException;
import java.lang.IllegalArgumentException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * Manages access to CypherOS hardware extensions
 *
 *  <p>
 *  This manager requires the HARDWARE_ABSTRACTION_ACCESS permission.
 *  <p>
 *  To get the instance of this class, utilize HardwareManager#getInstance(Context context)
 */
public final class HardwareManager {
    private static final String TAG = "HardwareManager";

    private static IHardwareService sService;

    private Context mContext;

    /* The VisibleForTesting annotation is to ensure Proguard doesn't remove these
     * fields, as they might be used via reflection. When the @Keep annotation in
     * the support library is properly handled in the platform, we should change this.
     */

    /**
     * Adaptive backlight support (this refers to technologies like NVIDIA SmartDimmer,
     * QCOM CABL or Samsung CABC)
     */
    @VisibleForTesting
    public static final int FEATURE_ADAPTIVE_BACKLIGHT = 0x1;

    /**
     * Hardware navigation key disablement
     */
    @VisibleForTesting
    public static final int FEATURE_KEY_DISABLE = 0x20;

    /**
     * Double-tap the touch panel to wake up the device
     */
    @VisibleForTesting
    public static final int FEATURE_TAP_TO_WAKE = 0x200;

    private static final List<Integer> BOOLEAN_FEATURES = Arrays.asList(
        FEATURE_KEY_DISABLE,
        FEATURE_TAP_TO_WAKE
    );

    private static HardwareManager sHardwareManagerInstance;

    /**
     * @hide to prevent subclassing from outside of the framework
     */
    private HardwareManager(Context context) {
        Context appContext = context.getApplicationContext();
        if (appContext != null) {
            mContext = appContext;
        } else {
            mContext = context;
        }
        sService = getService();

        if (context.getPackageManager().hasSystemFeature(
                ContextConstants.Features.HARDWARE_ABSTRACTION) && !checkService()) {
            Log.wtf(TAG, "Unable to get HardwareService. The service either" +
                    " crashed, was not started, or the interface has been called to early in" +
                    " SystemServer init");
        }
    }

    /**
     * Get or create an instance of the {@link com.aoscp.utils.hardware.HardwareManager}
     * @param context
     * @return {@link HardwareManager}
     */
    public static HardwareManager getInstance(Context context) {
        if (sHardwareManagerInstance == null) {
            sHardwareManagerInstance = new HardwareManager(context);
        }
        return sHardwareManagerInstance;
    }

    /** @hide */
    public static IHardwareService getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService(ContextConstants.AOSCP_HARDWARE_SERVICE);
        if (b != null) {
            sService = IHardwareService.Stub.asInterface(b);
            return sService;
        }
        return null;
    }

    /**
     * @return the supported features bitmask
     */
    public int getSupportedFeatures() {
        try {
            if (checkService()) {
                return sService.getSupportedFeatures();
            }
        } catch (RemoteException e) {
        }
        return 0;
    }

    /**
     * Determine if a CypherOS Hardware feature is supported on this device
     *
     * @param feature The CypherOS Hardware feature to query
     *
     * @return true if the feature is supported, false otherwise.
     */
    public boolean isSupported(int feature) {
        return feature == (getSupportedFeatures() & feature);
    }

    /**
     * String version for preference constraints
     *
     * @hide
     */
    public boolean isSupported(String feature) {
        if (!feature.startsWith("FEATURE_")) {
            return false;
        }
        try {
            Field f = getClass().getField(feature);
            if (f != null) {
                return isSupported((int) f.get(null));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.d(TAG, e.getMessage(), e);
        }

        return false;
    }
    /**
     * Determine if the given feature is enabled or disabled.
     *
     * Only used for features which have simple enable/disable controls.
     *
     * @param feature the CypherOS Hardware feature to query
     *
     * @return true if the feature is enabled, false otherwise.
     */
    public boolean get(int feature) {
        if (!BOOLEAN_FEATURES.contains(feature)) {
            throw new IllegalArgumentException(feature + " is not a boolean");
        }

        try {
            if (checkService()) {
                return sService.get(feature);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    /**
     * Enable or disable the given feature
     *
     * Only used for features which have simple enable/disable controls.
     *
     * @param feature the CypherOS Hardware feature to set
     * @param enable true to enable, false to disale
     *
     * @return true if the feature is enabled, false otherwise.
     */
    public boolean set(int feature, boolean enable) {
        if (!BOOLEAN_FEATURES.contains(feature)) {
            throw new IllegalArgumentException(feature + " is not a boolean");
        }

        try {
            if (checkService()) {
                return sService.set(feature, enable);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    private int getArrayValue(int[] arr, int idx, int defaultValue) {
        if (arr == null || arr.length <= idx) {
            return defaultValue;
        }

        return arr[idx];
    }

    /**
     * Write a string to persistent storage, which persists thru factory reset
     *
     * @param key String identifier for this item. Must not exceed 64 characters.
     * @param value The UTF-8 encoded string to store of at least 1 character. null deletes the key/value pair.
     * @return true on success
     */
    public boolean writePersistentString(String key, String value) {
        try {
            if (checkService()) {
                return sService.writePersistentBytes(key,
                        value == null ? null : value.getBytes("UTF-8"));
            }
        } catch (RemoteException e) {
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Write an integer to persistent storage, which persists thru factory reset
     *
     * @param key String identifier for this item. Must not exceed 64 characters.
     * @param value The integer to store
     * @return true on success
     */
    public boolean writePersistentInt(String key, int value) {
        try {
            if (checkService()) {
                return sService.writePersistentBytes(key,
                        ByteBuffer.allocate(4).putInt(value).array());
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    /**
     * Write a byte array to persistent storage, which persists thru factory reset
     *
     * @param key String identifier for this item. Must not exceed 64 characters.
     * @param value The byte array to store, must be 1-4096 bytes. null deletes the key/value pair.
     * @return true on success
     */
    public boolean writePersistentBytes(String key, byte[] value) {
        try {
            if (checkService()) {
                return sService.writePersistentBytes(key, value);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    /**
     * Read a string from persistent storage
     *
     * @param key String identifier for this item. Must not exceed 64 characters.
     * @return the stored UTF-8 encoded string, null if not found
     */
    public String readPersistentString(String key) {
        try {
            if (checkService()) {
                byte[] bytes = sService.readPersistentBytes(key);
                if (bytes != null) {
                    return new String(bytes, "UTF-8");
                }
            }
        } catch (RemoteException e) {
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Read an integer from persistent storage
     *
     * @param key String identifier for this item. Must not exceed 64 characters.
     * @return the stored integer, zero if not found
     */
    public int readPersistentInt(String key) {
        try {
            if (checkService()) {
                byte[] bytes = sService.readPersistentBytes(key);
                if (bytes != null) {
                    return ByteBuffer.wrap(bytes).getInt();
                }
            }
        } catch (RemoteException e) {
        }
        return 0;
    }

    /**
     * Read a byte array from persistent storage
     *
     * @param key String identifier for this item. Must not exceed 64 characters.
     * @return the stored byte array, null if not found
     */
    public byte[] readPersistentBytes(String key) {
        try {
            if (checkService()) {
                return sService.readPersistentBytes(key);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    /** Delete an object from persistent storage
     *
     * @param key String identifier for this item
     * @return true if an item was deleted
     */
    public boolean deletePersistentObject(String key) {
        try {
            if (checkService()) {
                return sService.writePersistentBytes(key, null);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    /**
     * @return true if service is valid
     */
    private boolean checkService() {
        if (sService == null) {
            Log.w(TAG, "not connected to HardwareManagerService");
            return false;
        }
        return true;
    }
}