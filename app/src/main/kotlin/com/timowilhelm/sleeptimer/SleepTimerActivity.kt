package com.timowilhelm.sleeptimer

import android.content.*
import android.os.Bundle
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.triggertrap.seekarc.SeekArc
import com.triggertrap.seekarc.SeekArc.OnSeekArcChangeListener


class SleepTimerActivity : AppCompatActivity() {

    companion object Actions {
        const val ACTION_TIMER_FINISH = "com.timowilhelm.sleeptimer.ACTION_TIMER_FINISH"
        const val ACTION_TIMER_UPDATE = "com.timowilhelm.sleeptimer.ACTION_TIMER_UPDATE"
    }

    private var sleepTimerService: SleepTimerService? = null

    private lateinit var seekArc: SeekArc
    private lateinit var seekArcProgress: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var extendButton: Button

    private var lastUsedTimePreference
        get() = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(getString(R.string.preference_last_used_time_key), resources.getInteger(R.integer.default_time))
        set(value) = PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putInt(getString(R.string.preference_last_used_time_key), value)
                .apply()


    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_TIMER_FINISH -> handleTimerFinished()
                ACTION_TIMER_UPDATE -> handleTimerUpdate(
                        intent.getIntExtra("timeLeft", 0))
            }
        }
    }

    private val sleepTimerServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            val binder = service as SleepTimerService.LocalBinder
            val boundSleepTimerService = binder.getService()
            if(boundSleepTimerService != null) {
                sleepTimerService = boundSleepTimerService;
                updateUiAfterBinding(boundSleepTimerService)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            sleepTimerService = null;
        }
    }

    private fun updateUiAfterBinding(sleepTimerService: SleepTimerService) {
        if (sleepTimerService.running) {
            handleTimerUpdate(sleepTimerService.timeLeft)
            updateUiTimerRunning()
        } else {
            seekArc.progress = lastUsedTimePreference
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_timer)
        val serviceToStart = Intent(this, SleepTimerService::class.java)
        bindService(serviceToStart, sleepTimerServiceConnection, Context.BIND_AUTO_CREATE)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        extendButton = findViewById(R.id.extendButton)
        seekArc = findViewById(R.id.seekArc)
        seekArcProgress = findViewById(R.id.seekArcProgress)
        seekArc.setOnSeekArcChangeListener(object : OnSeekArcChangeListener {

            override fun onStopTrackingTouch(seekArc: SeekArc) {}
            override fun onStartTrackingTouch(seekArc: SeekArc) {}
            override fun onProgressChanged(seekArc: SeekArc, progress: Int,
                                           fromUser: Boolean) {
                seekArcProgress.text = String.format(getString(R.string.progress_text), progress)
            }
        })
        val intentFilter = IntentFilter(ACTION_TIMER_FINISH)
        intentFilter.addAction(ACTION_TIMER_UPDATE)

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadCastReceiver, IntentFilter(intentFilter))
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(broadCastReceiver)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun startTimer(view: View) {
        val timerValueInMinutes = seekArc.progress
        lastUsedTimePreference = timerValueInMinutes
        val serviceToStart = Intent(this, SleepTimerService::class.java)
        startService(serviceToStart)
        sleepTimerService?.startTimer(timerValueInMinutes)
        updateUiTimerRunning()
    }

    fun extendTimer(view: View) {
        sleepTimerService?.extendTimer()
    }

    fun stopTimer(view: View) {
        sleepTimerService?.stopTimerService()
        updateUiTimerStopped()
    }

    private fun handleTimerUpdate(timeLeft: Int) {
        seekArc.progress = timeLeft
    }

    private fun handleTimerFinished() {
        updateUiTimerStopped()
        seekArc.progress = lastUsedTimePreference
    }

    private fun updateUiTimerRunning() {
        startButton.visibility = View.GONE
        stopButton.visibility = View.VISIBLE
        extendButton.visibility = View.VISIBLE
        seekArc.isEnabled = false
    }

    private fun updateUiTimerStopped() {
        startButton.visibility = View.VISIBLE
        stopButton.visibility = View.GONE
        extendButton.visibility = View.GONE
        seekArc.isEnabled = true
    }
}

