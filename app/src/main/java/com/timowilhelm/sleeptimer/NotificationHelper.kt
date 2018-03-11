package com.timowilhelm.sleeptimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.NotificationCompat


/**
 * Helper class to manage notification channels, and create notifications.
 */
internal class NotificationHelper (context: Context) : ContextWrapper(context) {

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val extendIntent = Intent(this, SleepTimerService::class.java)
            .putExtra("action", "extend")
    private val pendingExtendIntent = PendingIntent.getService(this, 1, extendIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    private val stopIntent = Intent(this, SleepTimerService::class.java)
            .putExtra("action", "stop")
    private val pendingStopIntent = PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    private val contentIntent = PendingIntent.getActivity(this, 0,
            Intent(this, SleepTimerActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

    private val notificationBuilder: NotificationCompat.Builder by lazy {
        NotificationCompat.Builder(this, "sleepTimer")
                .setSmallIcon(smallIcon)
                .setContentTitle("Sleep Timer")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setDefaults(0)
                .setSound(null)
                .setContentIntent(contentIntent)
                .addAction(android.R.drawable.btn_plus, "Extend", pendingExtendIntent)
                .addAction(android.R.drawable.ic_delete, "Stop", pendingStopIntent)
    }

    init {
        val sleepTimerChannel = NotificationChannel(
                "sleepTimer",
                "Sleep Timer",
                NotificationManager.IMPORTANCE_LOW)

        // Configure the channel's initial settings
        sleepTimerChannel.lightColor = Color.GREEN

        // Submit the notification channel object to the notification manager
         notificationManager.createNotificationChannel(sleepTimerChannel)
    }

    fun getNotification():Notification{
        return this.notificationBuilder.build()
    }

    fun notify(id: Int, contentText: String) {
        this.notificationBuilder.setContentText(contentText)
        notificationManager.notify(id, this.notificationBuilder.build())
    }

    fun cancel(id: Int) {
        notificationManager.cancel(id)
    }

    private val smallIcon: Int
        get() = R.drawable.ic_stat_sleeptimer


}