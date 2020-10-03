package org.coralibre.android.sdk.internal.device_info;

import static org.coralibre.android.sdk.internal.device_info.ConfidenceLevel.getConfidenceLevel;

public class DeviceInfo implements Comparable<DeviceInfo> {

    private String manufacturer;
    private String device;
    private String model;
    private int rssiCorrection;
    private int tx;
    private ConfidenceLevel calibrationConfidence;

    public DeviceInfo(String manufacturer,
                      String device,
                      String model,
                      int rssiCorrection,
                      int tx,
                      ConfidenceLevel calibrationConfidence) {
        this.manufacturer = manufacturer;
        this.device = device;
        this.model = model;
        this.rssiCorrection = rssiCorrection;
        this.tx = tx;
        this.calibrationConfidence = calibrationConfidence;
    }

    public DeviceInfo(String rawString) {
        String[] row = rawString.split(",");
        this.manufacturer = row[0];
        this.device = row[1];
        this.model = row[2];
        this.rssiCorrection = Integer.parseInt(row[3]);
        this.tx = Integer.parseInt(row[4]);
        try {
            this.calibrationConfidence = getConfidenceLevel(Integer.parseInt(row[5]));
        } catch (NumberFormatException e) {
            this.calibrationConfidence = getConfidenceLevel(row[5]);
        }
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getDevice() {
        return device;
    }

    public String getModel() {
        return model;
    }

    public int getRssiCorrection() {
        return rssiCorrection;
    }

    public int getTx() {
        return tx;
    }

    public ConfidenceLevel getCalibrationConfidence() {
        return calibrationConfidence;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%d,%d,%s",
                manufacturer, device, model, rssiCorrection, tx, calibrationConfidence);
    }

    @Override
    public boolean equals(Object otherInfo) {
        if (otherInfo.getClass() != getClass()) return false;
        DeviceInfo otherDeviceInfo = (DeviceInfo) otherInfo;
        return otherDeviceInfo.getManufacturer().equalsIgnoreCase(manufacturer)
                && otherDeviceInfo.getDevice().equalsIgnoreCase(device)
                && otherDeviceInfo.getModel().equalsIgnoreCase(model)
                && otherDeviceInfo.getRssiCorrection() == rssiCorrection
                && otherDeviceInfo.getTx() == tx
                && otherDeviceInfo.getCalibrationConfidence() == calibrationConfidence;
    }

    @Override
    public int compareTo(DeviceInfo otherInfo) {
        if (manufacturer.compareToIgnoreCase(otherInfo.manufacturer) != 0) {
            return manufacturer.compareToIgnoreCase(otherInfo.manufacturer);
        }
        if (model.compareToIgnoreCase(otherInfo.model) != 0) {
            return model.compareToIgnoreCase(otherInfo.model);
        }
        if (device.compareToIgnoreCase(otherInfo.device) != 0) {
            return device.compareToIgnoreCase(otherInfo.device);
        }
        return 0;
    }
}
