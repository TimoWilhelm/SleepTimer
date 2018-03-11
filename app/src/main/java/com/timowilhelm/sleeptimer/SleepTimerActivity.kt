package com.timowilhelm.sleeptimer

import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.widget.NumberPicker
import android.view.View
import android.widget.Button
import android.util.Log
import com.timowilhelm.sleeptimer.R.id.startButton
import com.timowilhelm.sleeptimer.R.id.stopButton
import junit.runner.Version.id


class SleepTimerActivity : AppCompatActivity() {

    private var initialTimerValue = 5

    var myService: SleepTimerService? = null
    var isBound = false

    lateinit var timerPicker: NumberPicker
    private lateinit var startButton: Button
    private lateinit var stopButton: Button


    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            if(intent?.action == "BROADCAST_TIMER_CHANGED"){
                when (intent.getStringExtra("state")){
                    "update" -> handleTimerUpdate(intent.getIntExtra("timeLeft", 0))
                    "finished" -> handleTimerFinished()
                }
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

    fun updateUiAfterBinding(){
        if(myService!!.running){
            this.timerPicker.value = myService!!.timeLeft
            startButton.setVisibility( View.GONE );
            stopButton.setVisibility( View.VISIBLE );
            timerPicker.isEnabled = false
        }else{
            this.timerPicker.value = this.initialTimerValue
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_timer)
        val serviceToStart = Intent(this, SleepTimerService::class.java)
        startService(serviceToStart)
        bindService(serviceToStart, myConnection, Context.BIND_AUTO_CREATE)
        this.startButton = findViewById<Button>(R.id.startButton)
        this.stopButton = findViewById<Button>(R.id.stopButton)
        this.timerPicker = findViewById<NumberPicker>(R.id.timer)
        this.timerPicker.minValue = 1
        this.timerPicker.maxValue = 60
        this.timerPicker.wrapSelectorWheel = false

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadCastReceiver, IntentFilter("BROADCAST_TIMER_CHANGED"))
    }

    override fun onDestroy() {
        super.onDestroy()
        val serviceToStop = Intent(this, SleepTimerService::class.java)
        if(!myService!!.running) stopService(serviceToStop)
        unbindService(myConnection)
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(broadCastReceiver)
    }

    fun startTimer(view: View) {
        var timerValueInMinutes = this.timerPicker.value
        myService?.startTimer(timerValueInMinutes)
        startButton.setVisibility( View.GONE );
        stopButton.setVisibility( View.VISIBLE );
        timerPicker.isEnabled = false
    }

    fun stopTimer(view: View) {
        myService?.stopTimerService()
        startButton.setVisibility( View.VISIBLE );
        stopButton.setVisibility( View.GONE );
        timerPicker.isEnabled = true
    }

    fun handleTimerUpdate(timeLeft:Int){
        this.timerPicker.value = timeLeft
    }

    fun handleTimerFinished(){
        startButton.setVisibility( View.VISIBLE );
        stopButton.setVisibility( View.GONE );
        timerPicker.isEnabled = true
    }
}

