@file:Suppress("DEPRECATION")

package org.waylander.strober

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var flashLen = 50
    private var flashInt = 1000
    private var camAvailable = false
    private val userCameraAgreeCode = 100
    private var cam : Camera? = null
    private var strobeActive = false
    private var strobeON = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), userCameraAgreeCode)
        } else {
            camAvailable = true
        }

        seekLength.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                flashLen = (progress * 10) + 50
                txtLength.setText(flashLen.toString())
            }
        })

        seekInterval.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                flashInt = (progress * 10) + 100
                txtInterval.setText(flashInt.toString())
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == userCameraAgreeCode) {
            camAvailable = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }

    fun btnClick(v: View?) {
        if (camAvailable && applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            if (strobeActive) {
                btnSwitch.text = "SWITCH ON"

                cam?.release()
            } else {
                btnSwitch.text = "SWITCH OFF"

                try {
                    cam = Camera.open()
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "Camera failed!", Toast.LENGTH_SHORT).show()
                }

                val p: Camera.Parameters? = cam?.getParameters()
                p?.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
                cam?.setParameters(p)

                strobeON = true
                val timer = object: CountDownTimer(flashLen.toLong(), flashLen.toLong()) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        strobePhase()
                    }
                }
                timer.start()
            }
            strobeActive = !strobeActive
        } else {
            Toast.makeText(applicationContext, "Flashlight unavailable!", Toast.LENGTH_SHORT).show()
        }
    }

    fun strobePhase() {
        if (!strobeActive) return

        val p: Camera.Parameters? = cam?.getParameters()
        p?.setFlashMode(if (strobeON) Camera.Parameters.FLASH_MODE_OFF else Camera.Parameters.FLASH_MODE_TORCH)
        cam?.setParameters(p)

        val t = if (strobeON) flashInt.toLong() else flashLen.toLong()
        val timer = object: CountDownTimer(t, t) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                strobePhase()
            }
        }
        strobeON = !strobeON
        timer.start()
    }
}