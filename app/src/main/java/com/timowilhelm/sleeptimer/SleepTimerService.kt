package com.timowilhelm.sleeptimer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.CountDownTimer
import android.os.IBinder
import kotlin.math.roundToInt

class SleepTimerService : Service() {

    private lateinit var notificationHelper : NotificationHelper
    private var countDownTimer: CountDownTimer? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate(){
        notificationHelper = NotificationHelper(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(1, notificationHelper.getNotification())
        startTimer(intent.getIntExtra("time", 1))
        return START_NOT_STICKY
    }


    fun startTimer(timerValueInMinutes: Int){
        var timerValueInMs = (timerValueInMinutes * 60 * 1000).toLong()
        notificationHelper.notify(1, "Going to sleep in $timerValueInMinutes")
        countDownTimer = object : CountDownTimer(timerValueInMs, 60000) {
            override fun onTick(millisUntilFinished: Long) {
                var timeLeft = (millisUntilFinished / 60.0 / 1000.0).roundToInt()
                notificationHelper.notify(1, "Going to sleep in $timeLeft minutes")
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

                notificationHelper.cancel(1)
                stopForeground(true)
                stopSelf()
            }
        }.start()
    }

//    fun stopTimer(){
//        if (countDownTimer != null) countDownTimer!!.cancel()
//        notificationHelper.cancel(1)
//    }
}
