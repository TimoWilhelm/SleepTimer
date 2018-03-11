package com.timowilhelm.sleeptimer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import kotlin.concurrent.timer
import kotlin.math.roundToInt

class SleepTimerService : Service() {

    val EXTEND_TIME = 5

    private lateinit var notificationHelper : NotificationHelper
    private var countDownTimer: CountDownTimer? = null
    var timeLeft = 0
    var running = false

    private val myBinder = MyLocalBinder()

    inner class MyLocalBinder : Binder() {
        fun getService() : SleepTimerService? {
            return this@SleepTimerService
        }

    }
    override fun onBind(intent: Intent): IBinder? {
        return myBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra("action")){
            "extend" -> extendTimer()
            "stop" -> stopTimerService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate(){
        notificationHelper = NotificationHelper(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationHelper.cancel(1)
    }

    fun startTimer(timerValueInMinutes: Int){
        startForeground(1, notificationHelper.getNotification())
        var timerValueInMs = (timerValueInMinutes * 60 * 1000).toLong()
        notificationHelper.notify(1, "Going to sleep in $timerValueInMinutes")
        countDownTimer = object : CountDownTimer(timerValueInMs, 60000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 60.0 / 1000.0).roundToInt()
                notificationHelper.notify(1, "Going to sleep in $timeLeft minutes")

                val timerUpdateBroadcast = Intent("BROADCAST_TIMER_CHANGED")
                        .putExtra("state", "update")
                        .putExtra("timeLeft", timeLeft)
                LocalBroadcastManager.getInstance(this@SleepTimerService)
                        .sendBroadcast(timerUpdateBroadcast)
            }
            override fun onFinish() {
                // Stop Playback
                val audioManager  = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val playbackAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(playbackAttributes)
                        .build()
                val res = audioManager.requestAudioFocus(focusRequest)
                if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                    //ERROR
                } else if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    //Good
                }
                // Go to home screen
                val startMain = Intent(Intent.ACTION_MAIN)
                startMain.addCategory(Intent.CATEGORY_HOME)
                startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(startMain)

                stopTimerService()
            }
        }.start()
        running = true
    }

    fun extendTimer(){
        if (countDownTimer != null) countDownTimer!!.cancel()
        startTimer(timeLeft + EXTEND_TIME)
    }

    fun stopTimerService(){
        if (countDownTimer != null) countDownTimer!!.cancel()
        running = false

        stopForeground(true)

        val timerFinishedBroadcast = Intent("BROADCAST_TIMER_CHANGED")
                .putExtra("state", "finished")
        LocalBroadcastManager.getInstance(this@SleepTimerService)
                .sendBroadcast(timerFinishedBroadcast)

        stopSelf()
    }
}
