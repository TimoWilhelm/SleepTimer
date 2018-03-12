package com.timowilhelm.sleeptimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon


/**
 * Helper class to manage notification channels, and create notifications.
 */
internal class NotificationHelper(context: Context) : ContextWrapper(context) {

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }


    private val extendAction = Notification.Action.Builder(Icon.createWithResource(this,
            android.R.drawable.ic_menu_add), "Extend",
            PendingIntent.getService(this, 1,
                    Intent(this, SleepTimerService::class.java)
                            .putExtra("action", "extend"),
                    PendingIntent.FLAG_UPDATE_CURRENT)
    ).build()

    private val stopAction = Notification.Action.Builder(Icon.createWithResource(this,
            android.R.drawable.ic_menu_delete), "Stop",
            PendingIntent.getService(this, 2,
                    Intent(this, SleepTimerService::class.java)
                            .putExtra("action", "stop"),
                    PendingIntent.FLAG_UPDATE_CURRENT)
    ).build()

    private val contentIntent = PendingIntent.getActivity(this, 0,
            Intent(this, SleepTimerActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT)

    private val notificationBuilder: Notification.Builder by lazy {
        Notification.Builder(this, "sleepTimer")
                .setSmallIcon(smallIcon)
                .setContentTitle("Sleep Timer")
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(contentIntent)
                .setActions(extendAction, stopAction)
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

    fun getNotification(): Notification {
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