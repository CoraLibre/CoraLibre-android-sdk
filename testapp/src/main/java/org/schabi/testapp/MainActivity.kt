package org.schabi.testapp

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import org.coralibre.android.sdk.PPCP
import org.coralibre.android.sdk.internal.TracingService
import org.coralibre.android.sdk.internal.TracingService.*
import org.coralibre.android.sdk.internal.TracingServiceBroadcastReceiver

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0);

        findViewById<Button>(R.id.start_service_button).setOnClickListener {
            PPCP.init(this);
            val serviceIntent = Intent(this, TracingService::class.java).apply {
                action = ACTION_START
            }
            startService(serviceIntent);
        }

        findViewById<Button>(R.id.stop_service_button).setOnClickListener {
            val serviceIntent = Intent(this, TracingService::class.java).apply {
                action = ACTION_STOP
            }
            startService(serviceIntent);

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, TracingServiceBroadcastReceiver::class.java)
            intent.action = TracingService.ACTION_RESTART_SERVER
            alarmManager.cancel(PendingIntent.getBroadcast(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT))
            intent.action = ACTION_RESTART_CLIENT;
            alarmManager.cancel(PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT))
        }
    }
}
