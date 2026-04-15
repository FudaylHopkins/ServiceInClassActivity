package edu.temple.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.content.edit

class TimerService : Service() {

    private var isRunning = false
    private var paused = false
    private var currentValue = 0

    private var timerHandler: Handler? = null
    private lateinit var t: TimerThread

    private val preferences by lazy {
        getSharedPreferences("timer_pref", MODE_PRIVATE)
    }

    inner class TimerBinder : Binder() {

        val isRunning: Boolean
            get() = this@TimerService.isRunning

        val paused: Boolean
            get() = this@TimerService.paused

        fun start(startValue: Int) {
            if (!this@TimerService.isRunning) {
                this@TimerService.startTimer(startValue)
            }
        }

        fun stop() {
            this@TimerService.stopTimer()
        }

        fun pause() {
            this@TimerService.pauseTimer()
        }

        fun resume() {
            this@TimerService.resumeTimer()
        }

        fun setHandler(handler: Handler) {
            timerHandler = handler
        }

        fun getSavedValue(): Int {
            return this@TimerService.getSavedValue()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return TimerBinder()
    }


    private fun startTimer(startValue: Int) {
        if (::t.isInitialized) {
            t.interrupt()
        }

        isRunning = true
        paused = false
        currentValue = startValue

        t = TimerThread(startValue)
        t.start()
    }

    private fun stopTimer() {
        if (::t.isInitialized) {
            t.interrupt()
        }

        isRunning = false
        paused = false
        preferences.edit { remove("paused_value") }
    }

    private fun pauseTimer() {
        if (::t.isInitialized && isRunning) {
            paused = true
            isRunning = false
            preferences.edit {
                putInt("paused_value", currentValue)
            }
        }
    }

    private fun resumeTimer() {
        if (paused) {
            startTimer(getSavedValue())
        }
    }

    private fun getSavedValue(): Int {
        return preferences.getInt("paused_value", 0)
    }

    inner class TimerThread(startValue: Int) : Thread() {

        private var timeRemaining = startValue

        override fun run() {
            try {
                while (timeRemaining > 0 && !isInterrupted) {

                    if (paused) {
                        sleep(100)
                        continue
                    }

                    currentValue = timeRemaining

                    Log.d("Countdown", timeRemaining.toString())

                    timerHandler?.sendEmptyMessage(timeRemaining)

                    sleep(1000)
                    timeRemaining--
                }

                if (!paused) {
                    preferences.edit { remove("paused_value") }
                }

            } catch (e: InterruptedException) {
                Log.d("Timer", "Interrupted")
            } finally {
                isRunning = false
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::t.isInitialized) {
            t.interrupt()
        }
        Log.d("TimerService", "Destroyed")
    }
}