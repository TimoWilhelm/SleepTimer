package com.timowilhelm.sleeptimer

import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.widget.NumberPicker
import android.view.View
import android.widget.Button

class SleepTimerActivity : AppCompatActivity() {

    private var initialTimerValue = 5

    var myService: SleepTimerService? = null
    var isBound = false

    private lateinit var timerPicker: NumberPicker
    private lateinit var startButton: Button
    private lateinit var stopButton: Button


    private val broadCastReceiver = object : BroadcastReceiver() {
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
            handleTimerUpdate(myService!!.timeLeft)
            updateUiTimerRunning()
        }else{
            this.timerPicker.value = this.initialTimerValue
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_timer)
        val serviceToStart = Intent(this, SleepTimerService::class.java)
        bindService(serviceToStart, myConnection, Context.BIND_AUTO_CREATE)
        this.startButton = findViewById(R.id.startButton)
        this.stopButton = findViewById(R.id.stopButton)
        this.timerPicker = findViewById(R.id.timer)
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
        val timerValueInMinutes = this.timerPicker.value
        val serviceToStart = Intent(this, SleepTimerService::class.java)
        startService(serviceToStart)
        myService?.startTimer(timerValueInMinutes)
        updateUiTimerRunning()
    }

    fun stopTimer(view: View) {
        myService?.stopTimerService()
        updateUiTimerStopped()
    }

    fun handleTimerUpdate(timeLeft:Int){
        this.timerPicker.value = timeLeft
    }

    fun handleTimerFinished(){
        updateUiTimerStopped()
    }

    private fun updateUiTimerRunning(){
        startButton.visibility = View.GONE
        stopButton.visibility = View.VISIBLE
        timerPicker.isEnabled = false
    }

    private fun updateUiTimerStopped(){
        startButton.visibility = View.VISIBLE
        stopButton.visibility = View.GONE
        timerPicker.isEnabled = true
    }
}

