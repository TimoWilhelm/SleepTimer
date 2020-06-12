package com.timowilhelm.sleeptimer

import android.annotation.SuppressLint
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import androidx.media.AudioManagerCompat.requestAudioFocus
import androidx.preference.PreferenceManager
import java.text.MessageFormat
import kotlin.math.roundToInt


class SleepTimerService : Service() {

    companion object Actions {
        const val ACTION_EXTEND_TIMER = "com.timowilhelm.sleeptimer.ACTION_EXTEND_TIMER"
        const val ACTION_STOP_TIMER = "com.timowilhelm.sleeptimer.ACTION_STOP_TIMER"
    }

    private val NOTIFICATION_ID = 15
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var wakeLock: PowerManager.WakeLock
    private var countDownTimer: CountDownTimer? = null
    private var lowerMediaVolumeTask: LowerMediaVolumeTask? = null
    private val myBinder = LocalBinder()

    var timeLeft = 0
    var running = false

    inner class LocalBinder : Binder() {
        fun getService(): SleepTimerService? {
            return this@SleepTimerService
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return myBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_EXTEND_TIMER -> extendTimer()
            ACTION_STOP_TIMER -> stopTimerService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.timowilhelm.sleeptimer::WakeLock")
        }
        notificationHelper = NotificationHelper(this)
    }

    override fun onDestroy() {
        notificationHelper.cancel(1)
        wakeLock.release()
        super.onDestroy()
    }

    fun startTimer(timerValueInMinutes: Int) {
        running = true
        this.wakeLock.acquire(timerValueInMinutes * 60 * 1000L)
        startForeground(NOTIFICATION_ID, notificationHelper.notificationBuilder.build())

        val timerValueInMs = (timerValueInMinutes * 60 * 1000).toLong()

        countDownTimer = object : CountDownTimer(timerValueInMs, 60000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 60.0 / 1000.0).roundToInt()
                notificationHelper.notify(NOTIFICATION_ID,
                        MessageFormat.format(getString(R.string.notification_message_sleeptimer_update), timeLeft))

                sendTimerUpdateBroadcast()
            }

            override fun onFinish() {
                timeLeft = 0
                notificationHelper.notify(NOTIFICATION_ID, getString(R.string.notification_message_sleeptimer_finished))
                lowerMediaVolumeTask = @SuppressLint("StaticFieldLeak")
                object : LowerMediaVolumeTask(baseContext) {
                    override fun onFinished() {
                        stopPlayback()
                        goToHomeScreen()
                        val turnOffScreen = PreferenceManager
                                .getDefaultSharedPreferences(this@SleepTimerService)
                                .getBoolean(getString(R.string.preference_turn_off_screen_key), false)
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
                .getInt(getString(R.string.preference_extend_time_key), resources.getInteger(R.integer.preference_extend_time_default))
        var newTime = timeLeft + extendTime
        val maxTimerValue = resources.getInteger(R.integer.timer_value_max)
        if (newTime > maxTimerValue) newTime = maxTimerValue
        countDownTimer?.cancel()
        startTimer(newTime)
    }

    fun stopTimerService() {
        lowerMediaVolumeTask?.cancel(true)
        running = false
        countDownTimer?.cancel()
        stopForeground(true)
        sendTimerFinishedBroadcast()
        stopSelf()
        wakeLock.release()
    }

    private fun stopPlayback() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val playbackAttributes = AudioAttributesCompat.Builder()
                .setUsage(AudioAttributesCompat.USAGE_MEDIA)
                .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
                .build()

        val focusRequest = AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes ?: return)
                .setWillPauseWhenDucked(false)
                .setOnAudioFocusChangeListener { _ ->
                    {}
                }
                .build()

        if (requestAudioFocus(audioManager, focusRequest) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Todo: Throw Error
        }
    }

    private fun goToHomeScreen() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        applicationContext.startActivity(startMain)
    }

    private fun turnOffScreen(): Boolean {
        val policyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminReceiver = ComponentName(this,
                SleepTimerAdminReceiver::class.java)
        val admin = policyManager.isAdminActive(adminReceiver)
        return if (admin) {
            policyManager.lockNow()
            true
        } else {
            false
        }
    }

    private fun sendTimerUpdateBroadcast() {
        val timerUpdateBroadcast = Intent(SleepTimerActivity.ACTION_TIMER_UPDATE)
                .putExtra("timeLeft", timeLeft)
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(timerUpdateBroadcast)
    }

    private fun sendTimerFinishedBroadcast() {
        val timerFinishedBroadcast = Intent(SleepTimerActivity.ACTION_TIMER_FINISH)
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(timerFinishedBroadcast)
    }
}
