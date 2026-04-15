package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var timerBinder: TimerService.TimerBinder
    private var isConnected = false
    private lateinit var serviceConnection: ServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textView)

        val handler = Handler(mainLooper) {
            textView.text = it.what.toString()
            true
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                timerBinder = service as TimerService.TimerBinder
                timerBinder.setHandler(handler)
                isConnected = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                isConnected = false
            }
        }

        bindService(
            Intent(this, TimerService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )

        findViewById<Button>(R.id.startButton).setOnClickListener {

            if (!isConnected) return@setOnClickListener

            when {

                // Not running and not paused → start fresh or resume from saved value
                !timerBinder.isRunning && !timerBinder.paused -> {
                    val savedValue = timerBinder.getSavedValue()

                    if (savedValue > 0) {
                        timerBinder.start(savedValue)
                    } else {
                        timerBinder.start(200)   // Default countdown value
                    }
                }

                // If running → pause
                timerBinder.isRunning -> {
                    timerBinder.pause()
                }

                // If paused → resume
                timerBinder.paused -> {
                    timerBinder.resume()
                }
            }
        }


        findViewById<Button>(R.id.stopButton).setOnClickListener {
            if (isConnected) {
                timerBinder.stop()
                textView.text = "0"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isConnected) {
            unbindService(serviceConnection)
            isConnected = false
        }
    }
}