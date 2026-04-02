package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    lateinit var timerBinder: TimerService.TimerBinder
    var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val serviceConnection = object : ServiceConnection{
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                timerBinder = p1 as TimerService.TimerBinder
                isConnected = true
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                isConnected = false
            }
        }

        bindService(Intent(this,
            TimerService::class.java),
            serviceConnection, BIND_AUTO_CREATE )

        findViewById<Button>(R.id.startButton).setOnClickListener {
            if (isConnected) {
                if (!timerBinder.isRunning && !timerBinder.paused) {
                    timerBinder.start(200)
                }else if(timerBinder.isRunning && !timerBinder.paused){
                    timerBinder.pause()
                }else if(timerBinder.paused){
                    timerBinder.pause()
                }
            }
        }
        
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            if(isConnected){
                timerBinder.stop()
            }

        }
    }
}