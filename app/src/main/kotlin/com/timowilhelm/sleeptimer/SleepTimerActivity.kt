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

    private var myService: SleepTimerService? = null
    private var isBound = false

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

    private val myConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            val binder = service as SleepTimerService.MyLocalBinder
            myService = binder.getService()
            isBound = true
            updateUiAfterBinding()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    private fun updateUiAfterBinding() {
        if (myService!!.running) {
            handleTimerUpdate(myService!!.timeLeft)
            updateUiTimerRunning()
        } else {
            this.seekArc.progress = lastUsedTimePreference
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_timer)
        val serviceToStart = Intent(this, SleepTimerService::class.java)
        bindService(serviceToStart, myConnection, Context.BIND_AUTO_CREATE)
        this.startButton = findViewById(R.id.startButton)
        this.stopButton = findViewById(R.id.stopButton)
        this.extendButton = findViewById(R.id.extendButton)
        this.seekArc = findViewById(R.id.seekArc)
        this.seekArcProgress = findViewById(R.id.seekArcProgress)
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
        val serviceToStop = Intent(this, SleepTimerService::class.java)
        if (!myService!!.running) stopService(serviceToStop)
        unbindService(myConnection)
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
        val timerValueInMinutes = this.seekArc.progress
        lastUsedTimePreference = timerValueInMinutes
        val serviceToStart = Intent(this, SleepTimerService::class.java)
        startService(serviceToStart)
        myService?.startTimer(timerValueInMinutes)
        updateUiTimerRunning()
    }

    fun extendTimer(view: View) {
        myService?.extendTimer()
    }

    fun stopTimer(view: View) {
        myService?.stopTimerService()
        updateUiTimerStopped()
    }

    private fun handleTimerUpdate(timeLeft: Int) {
        this.seekArc.progress = timeLeft
    }

    private fun handleTimerFinished() {
        updateUiTimerStopped()
        this.seekArc.progress = lastUsedTimePreference
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

