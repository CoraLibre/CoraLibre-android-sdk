/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.coralibre.android.sdk.internal;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.coralibre.android.sdk.BuildConfig;
import org.coralibre.android.sdk.R;
import org.coralibre.android.sdk.internal.bluetooth.BleClient;
import org.coralibre.android.sdk.internal.bluetooth.BleServer;
import org.coralibre.android.sdk.internal.bluetooth.BluetoothServiceStatus;
import org.coralibre.android.sdk.internal.bluetooth.BluetoothState;

public class TracingService extends Service {

    private static final String TAG = "TracingService";

    public static final String ACTION_START = TracingService.class.getCanonicalName() + ".ACTION_START";
    public static final String ACTION_RESTART_CLIENT = TracingService.class.getCanonicalName() + ".ACTION_RESTART_CLIENT";
    public static final String ACTION_RESTART_SERVER = TracingService.class.getCanonicalName() + ".ACTION_RESTART_SERVER";
    public static final String ACTION_STOP = TracingService.class.getCanonicalName() + ".ACTION_STOP";

    public static final String EXTRA_ADVERTISE = TracingService.class.getCanonicalName() + ".EXTRA_ADVERTISE";
    public static final String EXTRA_RECEIVE = TracingService.class.getCanonicalName() + ".EXTRA_RECEIVE";

    private static final String NOTIFICATION_CHANNEL_ID = "ppcp_tracing_service";
    private static final int NOTIFICATION_ID = 1829;


    // The source of these Values is this a description on Googles API description:
    // https://developers.google.com/android/exposure-notifications/ble-attenuation-overview
    public static final long SCAN_DURATION = 4 * 1000; // unit in milliseconds
    public static final long SCAN_INTERVAL = 5 * 60 * 1000; // unit in milliseconds
    public static final long MAC_ROTATION_PERIOD = 11 * 60 * 1000; // unit in milliseconds

    private Handler handler;
    private PowerManager.WakeLock wl;

    private BleServer bleServer;
    private BleClient bleClient;

    private final BroadcastReceiver bluetoothStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_ON) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, BluetoothAdapter.ACTION_STATE_CHANGED);
                    }
                    BluetoothServiceStatus.resetInstance();
                    BroadcastHelper.sendErrorUpdateBroadcast(context);
                }
            }
        }
    };

    private final BroadcastReceiver locationServiceStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocationManager.MODE_CHANGED_ACTION.equals(intent.getAction())) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, LocationManager.MODE_CHANGED_ACTION);
                }
                BroadcastHelper.sendErrorUpdateBroadcast(context);
            }
        }
    };

    private final BroadcastReceiver errorsUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BroadcastHelper.ACTION_UPDATE_ERRORS.equals(intent.getAction())) {
                invalidateForegroundNotification();
            }
        }
    };

    private boolean startAdvertising;
    private boolean startReceiving;

    private boolean isFinishing;

    public TracingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate()");
        }

        isFinishing = false;

        IntentFilter bluetoothFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateChangeReceiver, bluetoothFilter);

        IntentFilter locationServiceFilter = new IntentFilter(LocationManager.MODE_CHANGED_ACTION);
        registerReceiver(locationServiceStateChangeReceiver, locationServiceFilter);

        IntentFilter errorsUpdateFilter = new IntentFilter(BroadcastHelper.ACTION_UPDATE_ERRORS);
        registerReceiver(errorsUpdateReceiver, errorsUpdateFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            Log.e(TAG, "Service called but without action or intent");
            stopSelf();
            return START_NOT_STICKY;
        }

        if (wl == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            assert pm != null;
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getPackageName() + ":TracingServiceWakeLock");
            //TODO: insert wakelock timeout
            // Remember google wrote on its website that the scan duration is ~4sec:
            // https://developers.google.com/android/exposure-notifications/ble-attenuation-overview
            // maybe the wake lock should least a little longer that that
            wl.acquire();
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onStartCommand() with " + intent.getAction());
        }

        startAdvertising = intent.getBooleanExtra(EXTRA_ADVERTISE, true);
        startReceiving = intent.getBooleanExtra(EXTRA_RECEIVE, true);

        if (ACTION_START.equals(intent.getAction())) {
            startForeground(NOTIFICATION_ID, createForegroundNotification());
            start();
        } else if (ACTION_RESTART_CLIENT.equals(intent.getAction())) {
            startForeground(NOTIFICATION_ID, createForegroundNotification());
            ensureStarted();
            restartClient();
        } else if (ACTION_RESTART_SERVER.equals(intent.getAction())) {
            startForeground(NOTIFICATION_ID, createForegroundNotification());
            ensureStarted();
            restartServer();
        } else if (ACTION_STOP.equals(intent.getAction())) {
            stopForegroundService();
        }

        return START_REDELIVER_INTENT;
    }

    private Notification createForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent contentIntent = null;
        if (launchIntent != null) {
            contentIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_coralibre)
                .setContentIntent(contentIntent);

        String text = getString(R.string.ppcp_sdk_service_notification_text);
        builder.setContentTitle(getString(R.string.ppcp_sdk_service_notification_title))
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        return builder.build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelName = getString(R.string.ppcp_sdk_service_notification_channel);
        NotificationChannel channel =
                new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }

    private void invalidateForegroundNotification() {
        if (isFinishing) {
            return;
        }

        Notification notification = createForegroundNotification();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert mNotificationManager != null;
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void start() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        handler = new Handler();

        invalidateForegroundNotification();
        restartClient();
        restartServer();
    }

    private void ensureStarted() {
        if (handler == null) {
            handler = new Handler();
        }
        invalidateForegroundNotification();
    }

    private void restartClient() {
        BluetoothState bluetoothState = startClient();
        if (bluetoothState == BluetoothState.NOT_SUPPORTED) {
            Log.e(TAG, "bluetooth not supported");
            return;
        }

        handler.postDelayed(() -> {
            stopScanning();
            scheduleNextClientRestart(this, SCAN_INTERVAL);
        }, SCAN_DURATION);
    }

    private void restartServer() {
        BluetoothState bluetoothState = startServer();
        if (bluetoothState == BluetoothState.NOT_SUPPORTED) {
            Log.e(TAG, "bluetooth not supported");
            return;
        }

        scheduleNextServerRestart(this);
    }

    public static void scheduleNextClientRestart(Context context, long scanInterval) {
        final long now = System.currentTimeMillis();
        final long delay = scanInterval - (now % scanInterval);
        final long nextScan = now + delay;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TracingServiceBroadcastReceiver.class);
        intent.setAction(ACTION_RESTART_CLIENT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        assert alarmManager != null;
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextScan, pendingIntent);
    }

    public static void scheduleNextServerRestart(Context context) {
        final long now = System.currentTimeMillis();
        final long delay = MAC_ROTATION_PERIOD - (now % MAC_ROTATION_PERIOD);
        final long nextAdvertiseChange = now + delay;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TracingServiceBroadcastReceiver.class);
        intent.setAction(ACTION_RESTART_SERVER);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        assert alarmManager != null;
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAdvertiseChange, pendingIntent);
    }

    private void stopForegroundService() {
        isFinishing = true;
        stopClient();
        stopServer();
        BluetoothServiceStatus.resetInstance();
        stopForeground(true);
        wl.release();
        stopSelf();
    }

    private BluetoothState startServer() {
        stopServer();
        if (startAdvertising) {
            bleServer = new BleServer(this);

            return bleServer.startAdvertising();
        }
        return null;
    }

    private void stopServer() {
        if (bleServer != null) {
            bleServer.stop();
            bleServer = null;
        }
    }

    private BluetoothState startClient() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "request to restart client");
        }
        stopClient();

        if (startReceiving) {
            bleClient = new BleClient(this);
            return bleClient.start();
        }
        return null;
    }

    private void stopScanning() {
        if (bleClient != null) {
            bleClient.stop();
        }
    }

    private void stopClient() {
        if (bleClient != null) {
            bleClient.stop();
            bleClient = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onDestroy()");
        }

        unregisterReceiver(errorsUpdateReceiver);
        unregisterReceiver(bluetoothStateChangeReceiver);
        unregisterReceiver(locationServiceStateChangeReceiver);

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

}
