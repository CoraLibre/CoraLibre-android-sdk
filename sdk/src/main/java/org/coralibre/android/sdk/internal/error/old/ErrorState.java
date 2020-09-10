package org.coralibre.android.sdk.internal.error.old;


import org.coralibre.android.sdk.R;

public enum ErrorState {

    MISSING_LOCATION_PERMISSION(R.string.ppcp_sdk_service_notification_error_location_permission),
    LOCATION_SERVICE_DISABLED(R.string.ppcp_sdk_service_notification_error_location_service),
    BLE_DISABLED(R.string.ppcp_sdk_service_notification_error_bluetooth_disabled),
    BLE_NOT_SUPPORTED(R.string.ppcp_sdk_service_notification_error_bluetooth_not_supported),
    BLE_INTERNAL_ERROR(R.string.ppcp_sdk_service_notification_error_bluetooth_internal_error),
    BLE_ADVERTISING_ERROR(R.string.ppcp_sdk_service_notification_error_bluetooth_advertising_error),
    BLE_SCANNER_ERROR(R.string.ppcp_sdk_service_notification_error_bluetooth_scanner_error),
    BATTERY_OPTIMIZER_ENABLED(R.string.ppcp_sdk_service_notification_error_battery_optimization),
    SYNC_ERROR_SERVER(R.string.ppcp_sdk_service_notification_error_sync_server),
    SYNC_ERROR_NETWORK(R.string.ppcp_sdk_service_notification_error_sync_network),
    SYNC_ERROR_DATABASE(R.string.ppcp_sdk_service_notification_error_sync_database),
    SYNC_ERROR_TIMING(R.string.ppcp_sdk_service_notification_error_sync_timing),
    SYNC_ERROR_SIGNATURE(R.string.ppcp_sdk_service_notification_error_sync_signature);

    private int errorString;

    ErrorState(int errorString) {
        this.errorString = errorString;
    }

    public int getErrorString() {
        return errorString;
    }

}
