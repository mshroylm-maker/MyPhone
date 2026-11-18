package com.myphone.app

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder

class ScreenCaptureService : Service() {

    private var projection: MediaProjection? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val notification = Notification.Builder(this, "myphone_screen")
            .setContentTitle("MyPhone")
            .setContentText("مشاركة الشاشة مفعّلة")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(
                10,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(10, notification)
        }

        val resultCode = intent?.getIntExtra("resultCode", 0) ?: 0

        @Suppress("DEPRECATION")
        val data = intent?.getParcelableExtra<Intent>("projectionData")

        if (data == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val manager = getSystemService(
            MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

        projection = manager.getMediaProjection(resultCode, data)

        if (projection == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                "myphone_screen",
                "مشاركة الشاشة",
                NotificationManager.IMPORTANCE_LOW
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        projection?.stop()
        projection = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}