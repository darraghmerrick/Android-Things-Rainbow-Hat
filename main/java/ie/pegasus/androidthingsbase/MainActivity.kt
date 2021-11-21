package ie.pegasus.androidthingsbase

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import com.google.android.things.contrib.driver.bmx280.Bmx280
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.ht16k33.Ht16k33
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.android.things.pio.Gpio

/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
class MainActivity : Activity() {

    var redLed: Gpio? = null
    var greenLed: Gpio? = null
    var blueLed: Gpio? = null

    var buttonA: Button? = null
    var buttonB: Button? = null
    var buttonC: Button? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            initButtons()

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        buttonA!!.close()
        buttonB!!.close()
        buttonC!!.close()

    }


    fun initButtons() {
        redLed = RainbowHat.openLedRed()
        redLed!!.value = false

        greenLed = RainbowHat.openLedGreen()
        greenLed!!.value = false


        blueLed = RainbowHat.openLedBlue()
        blueLed!!.value = false

        val ledstrip = RainbowHat.openLedStrip()
        ledstrip!!.close()

        buttonA = RainbowHat.openButtonA()
        buttonA!!.setOnButtonEventListener { _, pressed -> redLed!!.value = pressed

            if (pressed) {
                redLed!!.value = true
                var freq=4000.0
                playStartupSound(freq)
                rainbow(pressed)
                monitorTemp()
                displayCurrentTemp()
            }
        }

        buttonB = RainbowHat.openButtonB()
        buttonB!!.setOnButtonEventListener { _, pressed ->
            greenLed!!.value = pressed

            if (pressed) {
                greenLed!!.value = true
                var freq=1000.0
                playStartupSound(freq)
                rainbow(pressed)
                monitorTemp()
                displayCurrentTemp()
            }
        }

        buttonC = RainbowHat.openButtonC()
        buttonC!!.setOnButtonEventListener { _, pressed ->
            blueLed!!.value = pressed

            if (pressed) {
                blueLed!!.value = true
                var freq=2500.0
                playStartupSound(freq)
                rainbow(pressed)
                monitorTemp()
                displayCurrentTemp()
            }
        }
    }

    private fun playStartupSound(freq: Double) {
        // Play a note on the buzzer.
        val buzzer = RainbowHat.openPiezo()
        buzzer.play(freq)

        Thread.sleep(500)

        // Stop the buzzer.
        buzzer.stop()
        // Close the device when done.
        buzzer.close()
    }

    fun rainbow(on : Boolean) {
        // Light up the rainbow
        val ledstrip = RainbowHat.openLedStrip()
        ledstrip.brightness = 1
        val rainbow = IntArray(RainbowHat.LEDSTRIP_LENGTH)
        for (i in rainbow.indices) {
            rainbow[i] = if (!on) 0 else Color.HSVToColor(254, arrayOf(i * 360f / RainbowHat.LEDSTRIP_LENGTH , 1f, 1f ).toFloatArray() )
        }
        ledstrip.write(rainbow)
        // Close the device when done.
        ledstrip.close()
    }

    fun monitorTemp() {
        // Continously report temperature.
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        var cb = object: SensorManager.DynamicSensorCallback() {

            override fun onDynamicSensorConnected(sensor: Sensor) {
                if (sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                    sensorManager.registerListener(
                        object: SensorEventListener {
                            override fun onSensorChanged(event: SensorEvent) {
                                Log.i(TAG, "sensor changed: " + event.values[0])
                            }
                            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                                Log.i(TAG, "accuracy changed: " + accuracy)
                            }
                        },
                        sensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        }

        sensorManager.registerDynamicSensorCallback( cb)
    }

    // temp sensor always seems to report the same value of 26.711567
    fun displayCurrentTemp() {
        // Log the current temperature
        val sensor = RainbowHat.openSensor()
        sensor.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X)
        Log.d(TAG, "temperature:" + sensor.readTemperature())
        // Close the device when done.

        // Display a string on the segment display.
        val segment = RainbowHat.openDisplay()
        segment.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX)
        segment.display(Math.round(sensor.readTemperature()))
        segment.setEnabled(true)
        // Close the device when done.
        segment.close()
        sensor.close()
    }

}
