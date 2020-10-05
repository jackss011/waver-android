package com.jack.nars.waver

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import com.jack.nars.waver.sound.AudioLoopBlender
import com.jack.nars.waver.sound.AudioLoopPlayer
import kotlin.math.log
import kotlin.math.log10
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private lateinit var vibrator: Vibrator
    private lateinit var looper: AudioLoopBlender

    private lateinit var volumeBar: SeekBar
    private var logVolume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        volumeBar = findViewById(R.id.volume_bar)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        looper = AudioLoopBlender.create(this, R.raw.brown_noise)

        volumeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                val p = progress.toFloat() / seek.max
                val v = if (logVolume) sliderToGain(p) else p
                looper.setVolume(v)
                Log.d("MY_APP", "position: %f, volume: %f".format(p, v))
            }

            override fun onStartTrackingTouch(seek: SeekBar) {}
            override fun onStopTrackingTouch(seek: SeekBar) {}
        })


//        val duration = 300L
//
//        val configRaise = VolumeShaper.Configuration.Builder()
//            .setCurve(floatArrayOf(0f, 1f), floatArrayOf(1f, 0f))
//            .setInterpolatorType(VolumeShaper.Configuration.INTERPOLATOR_TYPE_LINEAR)
//            .setDuration(duration)
//            .build()
//
//        val configFall = VolumeShaper.Configuration.Builder()
//            .setCurve(floatArrayOf(0f, 0f), floatArrayOf(1f, 1f))
//            .setInterpolatorType(VolumeShaper.Configuration.INTERPOLATOR_TYPE_LINEAR)
//            .setDuration(duration)
//            .build()
    }


    fun onClick(v: View) {
        val effect = when (v.id) {
            R.id.main_os -> VibrationEffect.createOneShot(2000, 255)
            R.id.main_t1 -> VibrationEffect.createWaveform(
                LongArray(60) { 16L },
                IntArray(60) { 200 },
                10)
            else -> null
        }

        if(effect != null) vibrator.vibrate(effect)
    }


    fun onClick2(v: View) {
        if(!looper.isPlaying())
            looper.start()
        else
            looper.pause()
    }

    fun onClick3(v: View) {
        logVolume = !logVolume
        Toast.makeText(this, "Log volume is" + if(logVolume) "true" else "false", Toast.LENGTH_SHORT).show()
    }
}


fun sliderToGain(position: Float, silent: Float = -30f, rollOff: Boolean = true): Float {
    var g = 10f.pow(((1 - position) * silent) / 20)

    // correction to get gain=0 for position=0
    if(rollOff)
        g -= 10f.pow(silent / 20) * (1 - position)

    return g
}