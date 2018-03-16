package com.timowilhelm.sleeptimer

import android.annotation.SuppressLint
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.preference.PreferenceManager
import kotlin.math.roundToInt


class SleepTimerService : Service() {

    private val NOTIFICATION_ID = 15

    private lateinit var notificationHelper: NotificationHelper
    private var countDownTimer: CountDownTimer? = null
    var timeLeft = 0
    var running = false
    private var lowerMediaVolumeTask: LowerMediaVolumeTask? = null

    private val myBinder = MyLocalBinder()

    inner class MyLocalBinder : Binder() {
        fun getService(): SleepTimerService? {
            return this@SleepTimerService
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return myBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra("action")) {
            "extend" -> extendTimer()
            "stop" -> stopTimerService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        notificationHelper = NotificationHelper(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationHelper.cancel(1)
    }

    fun startTimer(timerValueInMinutes: Int) {
        running = true
        startForeground(NOTIFICATION_ID, notificationHelper.notificationBuilder.build())

        val timerValueInMs = (timerValueInMinutes * 60 * 1000).toLong()

        notificationHelper.notify(NOTIFICATION_ID,
                "Going to sleep in $timerValueInMinutes")

        countDownTimer = object : CountDownTimer(timerValueInMs, 60000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 60.0 / 1000.0).roundToInt()
                notificationHelper.notify(NOTIFICATION_ID,
                        "Going to sleep in $timeLeft minutes")

                sendTimerUpdateBroadcast()
            }

            override fun onFinish() {
                timeLeft = 0
                notificationHelper.notify(NOTIFICATION_ID, "Going to sleep now")
                lowerMediaVolumeTask = @SuppressLint("StaticFieldLeak")
                object: LowerMediaVolumeTask(baseContext) {
                    override fun onFinished() {
                        stopPlayback()
                        goToHomeScreen()
                        val turnOffScreen = PreferenceManager
                                .getDefaultSharedPreferences(this@SleepTimerService)
                                .getBoolean("turn_off_screen", false)
                        if (turnOffScreen) turnOffScreen()
                        stopTimerService()
                    }
                }
                lowerMediaVolumeTask?.execute()
            }
        }.start()
    }

    fun extendTimer() {
        lowerMediaVolumeTask?.cancel(true)
        val extendTime = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt("extend_time_pref", resources.getInteger(R.integer.extend_time_pref_default))
        var newTime = timeLeft + extendTime
        val maxTimerValue = resources.getInteger(R.integer.max_timer_value)
        if (newTime > maxTimerValue) newTime = maxTimerValue
        if (countDownTimer != null) countDownTimer!!.cancel()
        startTimer(newTime)
    }

    fun stopTimerService() {
        running = false
        if (countDownTimer != null) countDownTimer!!.cancel()
        stopForeground(true)
        sendTimerFinishedBroadcast()
        stopSelf()
    }

    private fun stopPlayback() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .build()
        val res = audioManager.requestAudioFocus(focusRequest)
        if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            //Todo: Throw Error
        }
    }

    private fun goToHomeScreen() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    private fun turnOffScreen() {
        val policyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminReceiver = ComponentName(this,
                SleepTimerAdminReceiver::class.java)
        val admin = policyManager.isAdminActive(adminReceiver)
        if (admin) {
            policyManager.lockNow()
        } else {
            //TODO: Throw Error
        }
    }

    private fun sendTimerUpdateBroadcast() {
        val timerUpdateBroadcast = Intent("BROADCAST_TIMER_CHANGED")
                .putExtra("state", "update")
                .putExtra("timeLeft", timeLeft)
        LocalBroadcastManager.getInstance(this@SleepTimerService)
                .sendBroadcast(timerUpdateBroadcast)
    }

    private fun sendTimerFinishedBroadcast() {
        val timerFinishedBroadcast = Intent("BROADCAST_TIMER_CHANGED")
                .putExtra("state", "finished")
        LocalBroadcastManager.getInstance(this@SleepTimerService)
                .sendBroadcast(timerFinishedBroadcast)
    }

}
