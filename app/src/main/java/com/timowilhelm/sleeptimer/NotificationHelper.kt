package com.timowilhelm.sleeptimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.support.v4.app.NotificationCompat

/**
 * Created by DeChill on 10.03.2018.
 */
/**
 * Helper class to manage notification channels, and create notifications.
 */
internal class NotificationHelper (context: Context) : ContextWrapper(context) {

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val notificationBuilder: NotificationCompat.Builder by lazy {
        NotificationCompat.Builder(this, "sleepTimer")
                .setSmallIcon(smallIcon)
                .setContentTitle("Sleep Timer")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
    }

    init {
        val sleepTimerChannel = NotificationChannel(
                "sleepTimer",
                "Sleep Timer",
                NotificationManager.IMPORTANCE_DEFAULT)

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
        get() = android.R.drawable.stat_notify_chat


}