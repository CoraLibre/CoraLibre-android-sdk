package org.coralibre.android.sdk.internal.device_info;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import org.coralibre.android.sdk.BuildConfig;
import org.coralibre.android.sdk.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.coralibre.android.sdk.internal.device_info.ConfidenceLevel.LOW;
import static org.coralibre.android.sdk.internal.device_info.ConfidenceLevel.NONE;

public class DeviceList {
    private static final String TAG = DeviceInfo.class.toString();

    public static final String DEVICE_INFO_KEY = "DeviceList_device_info_key";

    private static final int LIST_FILE_ID = R.raw.en_calibration_2020_08_12;
    private static final int LIST_LINE_COUNT = 11810;
    public static final DeviceInfo DEFAULT_INFO = new DeviceInfo(
            "unknown", "unknown", "unknown", 4, -25, NONE);

    final DeviceInfo[] deviceList;
    final SharedPreferences sharedPreferences;

    public class FindingsResult {
        public final List<DeviceInfo> manufacturerFindings;
        public final List<DeviceInfo> oemDeviceFindings;
        public final List<DeviceInfo> oemModelFindings;

        public FindingsResult(List<DeviceInfo> manufacturerFindings,
                              List<DeviceInfo> oemDeviceFindings,
                              List<DeviceInfo> oemModelFindings) {
            this.manufacturerFindings = Collections.unmodifiableList(manufacturerFindings);
            this.oemDeviceFindings = Collections.unmodifiableList(oemDeviceFindings);
            this.oemModelFindings = Collections.unmodifiableList(oemModelFindings);
        }
    }

    public DeviceList(Context context, SharedPreferences sharedPrefs) throws IOException {
        deviceList = loadDeviceInfoTable(context);
        sharedPreferences = sharedPrefs;
    }

    public static DeviceInfo[] loadDeviceInfoTable(Context context) throws IOException {
        BufferedReader infoCVS = new BufferedReader(new InputStreamReader(
                context.getResources().openRawResource(LIST_FILE_ID)));
        DeviceInfo[] deviceList = new DeviceInfo[LIST_LINE_COUNT - 1];
        String line = infoCVS.readLine(); // discard the first line as it just contains the header
        int i = 0;
        while ((line = infoCVS.readLine()) != null) {
            deviceList[i] = new DeviceInfo(line);
            i++;
        }
        infoCVS.close();
        return deviceList;
    }

    /**
     * Tries to find the exact device in the list based on the given parameters. If the device
     * can not be found it will return the default device info, which is an average over the whole list.
     * @param manufacturer oem/manufacturer of the device
     * @param device name of the device
     * @param model name of the specific model
     * @return the exact device info from the list or the default device info
     */
    public DeviceInfo getExactInfo(final String manufacturer, final String device, final String model) {
        int index = Arrays.binarySearch(deviceList, new DeviceInfo(manufacturer, device, model, 0, 0, NONE));
        if (0 < index && deviceList[index].getModel().equals(model)) {
            return deviceList[index];
        } else {
            return new DeviceInfo(manufacturer, device, model,
                    DEFAULT_INFO.getRssiCorrection(),
                    DEFAULT_INFO.getTx(),
                    DEFAULT_INFO.getCalibrationConfidence());
        }
    }

    private FindingsResult findingsInList(final String manufacturer, final String device, final String model) {
        ArrayList<DeviceInfo> manufacturerFindings = new ArrayList<>();
        ArrayList<DeviceInfo> oemDeviceFindings = new ArrayList<>();
        ArrayList<DeviceInfo> oemModelFindings = new ArrayList<>();

        for (DeviceInfo i : deviceList) {
            if (manufacturer.toLowerCase().equals(i.getManufacturer().toLowerCase())) {
                manufacturerFindings.add(i);

                if (device.toLowerCase().equals(i.getDevice().toLowerCase())) {
                    oemDeviceFindings.add(i);
                }
                if (model.toLowerCase().equals(i.getModel().toLowerCase())) {
                    oemModelFindings.add(i);
                }
            }
        }
        return new FindingsResult(manufacturerFindings, oemDeviceFindings, oemModelFindings);
    }

    private static DeviceInfo getAverageOfFindings(List<DeviceInfo> findings) {
        boolean allConfidenceGreaterLow = true;
        int rssiSum = 0;
        int txSum = 0;
        for (DeviceInfo i : findings) {
            rssiSum += i.getRssiCorrection();
            txSum += i.getTx();
            if (i.getCalibrationConfidence() == LOW) allConfidenceGreaterLow = false;
        }

        return findings.size() == 0
                ? new DeviceInfo(Build.MANUFACTURER, Build.DEVICE, Build.MODEL, 0, 0, NONE)
                : new DeviceInfo(Build.MANUFACTURER, Build.DEVICE, Build.MODEL,
                rssiSum / findings.size(),
                txSum / findings.size(),
                allConfidenceGreaterLow ? LOW : NONE);
    }

    /**
     * Tries to calculate an accurate average based on similar manufacturer device or model.
     * This method tries to accomplish this by going through several steps. If one step
     * did not yield useful information the next step will be taken. The further down these
     * steps go the less accurate the result will be:
     * - If there is a full match on oem/device/model, take that
     * - If there is exactly one match with same oem/device or same oem/model, take that
     * - If there matches with same oem/device or same oem/model and confidence > LOW, take the average of them and set confidence to LOW
     * - If there are matches with same oem and confidence > LOW, take the average of them and set confidence to LOW
     * - Fallback should better not be zero, but more like industry average: RSSI: -3, TX: -25 (and confidence to LOWEST)
     *
     * Before using this method the getExactDevice() method should be called.
     * @param manufacturer oem/manufacturer of the device
     * @param device name of the device
     * @param model name of the specific model
     * @return a DeviceInfo calculated by the steps described earlier
     */
    private DeviceInfo getBestFittingInfoFromList(String manufacturer,
                                                  String device,
                                                  String model) {
        //try to find oem/device oem/model match

        FindingsResult findings = findingsInList(manufacturer, device, model);
        if (findings.oemDeviceFindings.size() == 1) {
            return findings.oemDeviceFindings.get(0);
        }
        if (findings.oemModelFindings.size() == 1) {
            return findings.oemModelFindings.get(0);
        }

        if (findings.oemDeviceFindings.size() > 1) {
            DeviceInfo info = getAverageOfFindings(findings.oemDeviceFindings);
            if (info.getCalibrationConfidence() == LOW) {
                return info;
            }
        }

        if (findings.oemModelFindings.size() > 1) {
            DeviceInfo info = getAverageOfFindings(findings.oemModelFindings);
            if (info.getCalibrationConfidence() == LOW) {
                return info;
            }
        }

        if (findings.manufacturerFindings.size() > 1) {
            return getAverageOfFindings(findings.manufacturerFindings);
        }

        return new DeviceInfo(manufacturer, device, model,
                DEFAULT_INFO.getRssiCorrection(), DEFAULT_INFO.getTx(), NONE);
    }

    private DeviceInfo getOwnDeviceInfo(String manufacturer,
                                        String device,
                                        String model) {
        if (sharedPreferences.contains(DEVICE_INFO_KEY)) {
            final DeviceInfo deviceInfo = new DeviceInfo(sharedPreferences.getString(DEVICE_INFO_KEY, DEFAULT_INFO.toString()));
            if (BuildConfig.DEBUG)
                Log.d(TAG, "get from shared prefs: " +
                        deviceInfo.toString());
            return deviceInfo;
        }
        //
        DeviceInfo deviceInfo = getExactInfo(manufacturer, device, model);
        if (deviceInfo.getCalibrationConfidence() == NONE) {
            deviceInfo = getBestFittingInfoFromList(manufacturer, device, model);
        }

        sharedPreferences.edit().putString(DEVICE_INFO_KEY, deviceInfo.toString()).apply();
        if (BuildConfig.DEBUG) Log.d(TAG, "get from list: " + deviceInfo.toString());
        return deviceInfo;
    }

    public static DeviceInfo getOwnDeviceInfo(Context context) throws IOException {
        return new DeviceList(context, PreferenceManager.getDefaultSharedPreferences(context))
                .getOwnDeviceInfo(Build.MANUFACTURER, Build.DEVICE, Build.MODEL);
    }
}
