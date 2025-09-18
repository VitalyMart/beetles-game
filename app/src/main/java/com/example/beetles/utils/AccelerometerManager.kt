package com.example.beetles.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset

class AccelerometerManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
    
    var gravity = mutableStateOf(Offset.Zero)
        private set
    
    fun startListening() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }
    
    fun stopListening() {
        sensorManager.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        gravity.value = Offset(-event.values[0] * 0.1f, event.values[1] * 0.1f)
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}