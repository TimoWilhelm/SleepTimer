package com.timowilhelm.sleeptimer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.NumberPicker
import android.view.View
import android.widget.Button
import android.content.Intent


class SleepTimerActivity : AppCompatActivity() {

    private var initialTimerValue = 5

    lateinit var timerPicker: NumberPicker
    private lateinit var startButton: Button
    private lateinit var stopButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_timer)

        this.startButton = findViewById<Button>(R.id.startButton)
        this.stopButton = findViewById<Button>(R.id.stopButton)

        this.timerPicker = findViewById<NumberPicker>(R.id.timer)
        this.timerPicker.minValue = 1
        this.timerPicker.maxValue = 60
        this.timerPicker.wrapSelectorWheel = false

        this.timerPicker.value = this.initialTimerValue
    }

    fun startTimer(view: View) {
        var timerValueInMinutes = this.timerPicker.value

        val intent = Intent(this, SleepTimerService::class.java)
        intent.putExtra("time", timerValueInMinutes)
        startService(intent)

        startButton.setVisibility( View.GONE );
        stopButton.setVisibility( View.VISIBLE );
    }

    fun stopTimer(view: View) {

        val intent = Intent(this, SleepTimerService::class.java)
        stopService(intent)

        startButton.setVisibility( View.VISIBLE );
        stopButton.setVisibility( View.GONE );
    }
}

