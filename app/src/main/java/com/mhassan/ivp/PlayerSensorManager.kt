package com.mhassan.ivp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlin.math.abs
import kotlin.math.sqrt

class PlayerSensorManager(private val mPlayer: SimpleExoPlayer, context: Context) : SensorEventListener {


    private var mListener: OnShakeListener? = null
    private var mShakeTimestamp: Long = 0
    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mGyroScope: Sensor? = null

    init {
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!
            .getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        mGyroScope = mSensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    fun startShakeDetection(){
        mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI)
        mSensorManager!!.registerListener(this, mGyroScope, SensorManager.SENSOR_DELAY_UI)
    }

    fun stopShakeDetection(){
        mSensorManager!!.unregisterListener(this)
    }

    fun setOnShakeListener(listener: OnShakeListener) {
        this.mListener = listener
    }

    interface OnShakeListener {
        fun onShake()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // ignore
    }

    override fun onSensorChanged(event: SensorEvent) {
        if(event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
             handleAccelerometer(event)
        }

        // ignore gyroscope events too close to a shake
        if(event.sensor.type ==  Sensor.TYPE_GYROSCOPE
                && mShakeTimestamp + SHAKE_SLOP_TIME_MS <= System.currentTimeMillis()){
            handleGyroscope(event)
        }
    }

    private fun handleGyroscope(event: SensorEvent){
        val x = event.values[0]
        val z = event.values[2]

        if(abs(x) > 3){
            print("should control the volume")
            mPlayer.volume = mPlayer.volume + ((x/2) * 0.1f)
        }else if (abs(z) > 3){
            mPlayer.seekTo(mPlayer.currentPosition - (z*0.75f).toInt()*1000)
        }
    }

    private fun handleAccelerometer(event: SensorEvent){
        if (mListener != null) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH

            // gForce will be close to 1 when there is no movement.
            val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                val now = System.currentTimeMillis()
                // ignore shake events too close to each other (500ms)
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return
                }

                mShakeTimestamp = now

                mListener!!.onShake()
            }
        }
    }

    companion object {
        private const val SHAKE_THRESHOLD_GRAVITY = 2.5f
        private const val SHAKE_SLOP_TIME_MS = 500
    }
}