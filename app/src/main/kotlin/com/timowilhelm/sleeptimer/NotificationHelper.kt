package com.timowilhelm.sleeptimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat


/**
 * Helper class to manage notification channels, and create notifications.
 */

internal class NotificationHelper(context: Context) : ContextWrapper(context) {

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }


    private val extendAction = NotificationCompat.Action.Builder(android.R.drawable.ic_menu_add, "Extend",
            PendingIntent.getService(this, 1,
                    Intent(this, SleepTimerService::class.java)
                            .setAction(SleepTimerService.ACTION_EXTEND_TIMER),
                    PendingIntent.FLAG_UPDATE_CURRENT)
    ).build()

    private val stopAction = NotificationCompat.Action.Builder(android.R.drawable.ic_menu_delete, "Stop",
            PendingIntent.getService(this, 2,
                    Intent(this, SleepTimerService::class.java)
                            .setAction(SleepTimerService.ACTION_STOP_TIMER),
                    PendingIntent.FLAG_UPDATE_CURRENT)
    ).build()

    private val contentIntent = PendingIntent.getActivity(this, 0,
            Intent(this, SleepTimerActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT)

    val notificationBuilder: NotificationCompat.Builder by lazy {
        NotificationCompat.Builder(this, "sleepTimer")
                .setSmallIcon(smallIcon)
                .setContentTitle("Sleep Timer")
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(contentIntent)
                .addAction(extendAction)
                .addAction(stopAction)
    }

    init {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            createNotificationChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val sleepTimerChannel = NotificationChannel(
                "sleepTimer",
                "Sleep Timer",
                NotificationManager.IMPORTANCE_LOW)

        // Configure the channel's initial settings
        sleepTimerChannel.lightColor = Color.GREEN

        // Submit the notification channel object to the notification manager
        notificationManager.createNotificationChannel(sleepTimerChannel)
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