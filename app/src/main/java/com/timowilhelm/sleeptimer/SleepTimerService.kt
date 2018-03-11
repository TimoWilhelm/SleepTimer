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
import kotlin.math.roundToInt
import android.os.SystemClock


class SleepTimerService : Service() {

    private val EXTEND_TIME_IN_MINUTES = 5
    private val VOLUME_ADJUST_SPEED_IN_MS = 2000L

    private val NOTIFICATION_ID = 15

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
        running = true
        startForeground(NOTIFICATION_ID, notificationHelper.getNotification())

        val timerValueInMs = (timerValueInMinutes * 60 * 1000).toLong()

        notificationHelper.notify(NOTIFICATION_ID, "Going to sleep in $timerValueInMinutes")

        countDownTimer = object : CountDownTimer(timerValueInMs, 60000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 60.0 / 1000.0).roundToInt()
                notificationHelper.notify(NOTIFICATION_ID, "Going to sleep in $timeLeft minutes")

                sendTimerUpdateBroadcast()
            }

            override fun onFinish() {
                notificationHelper.notify(NOTIFICATION_ID, "Going to sleep now")
                val originalVolumeIndex = getMediaVolume()
                lowerMediaVolumeGradually()
                stopPlayback()
                goToHomeScreen()
                setMediaVolume(originalVolumeIndex)
                stopTimerService()
            }
        }.start()
    }

    private fun extendTimer(){
        if (countDownTimer != null) countDownTimer!!.cancel()
        startTimer(timeLeft + EXTEND_TIME_IN_MINUTES)
    }

    fun stopTimerService(){
        running = false
        if (countDownTimer != null) countDownTimer!!.cancel()
        stopForeground(true)
        sendTimerFinishedBroadcast()
        stopSelf()
    }

    fun lowerMediaVolumeGradually(){
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        while (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0){
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
            SystemClock.sleep(VOLUME_ADJUST_SPEED_IN_MS)
        }
    }

    fun getMediaVolume(): Int{
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    fun setMediaVolume(volumeIndex: Int){
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeIndex,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
    }

    fun stopPlayback(){
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
    }

    fun goToHomeScreen(){
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    fun sendTimerUpdateBroadcast(){
        val timerUpdateBroadcast = Intent("BROADCAST_TIMER_CHANGED")
                .putExtra("state", "update")
                .putExtra("timeLeft", timeLeft)
        LocalBroadcastManager.getInstance(this@SleepTimerService)
                .sendBroadcast(timerUpdateBroadcast)
    }

    private fun sendTimerFinishedBroadcast(){
        val timerFinishedBroadcast = Intent("BROADCAST_TIMER_CHANGED")
                .putExtra("state", "finished")
        LocalBroadcastManager.getInstance(this@SleepTimerService)
                .sendBroadcast(timerFinishedBroadcast)
    }
}
