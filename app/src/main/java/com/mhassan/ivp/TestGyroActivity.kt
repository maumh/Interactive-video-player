package com.mhassan.ivp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TestGyroActivity : AppCompatActivity() {
    lateinit var textX: TextView
    lateinit var textY: TextView
    lateinit var textZ : TextView
    lateinit var sensorManager: SensorManager
    lateinit var sensor : Sensor

    var gyroListener : SensorEventListener = object: SensorEventListener {

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }

        override fun onSensorChanged(event: SensorEvent?) {
            val x = event!!.values[0]
            val y = event.values[1]
            val z = event.values[2]

            textX.text = "X : $x rad/s"
            textY.text = "Y : $y rad/s"
            textZ.text = "Z : $z rad/s"
        }
    };

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_gyro_activity)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        textX = findViewById(R.id.textX)
        textY = findViewById(R.id.textY)
        textZ = findViewById(R.id.textZ)

    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(gyroListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(gyroListener)
    }
}