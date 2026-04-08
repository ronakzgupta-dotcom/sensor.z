package com.example.sensorz;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

/**
 * MainActivity handles sensor data collection (Accelerometer, Light, Proximity)
 * and provides a theme switching capability.
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "SensorMonitor";
    private SensorManager sensorManager;
    private Sensor accelerometer, lightSensor, proximitySensor;
    private TextView accelText, lightText, proximityText;
    private SwitchCompat themeSwitch;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize SharedPreferences to store theme state
        sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("isDarkMode", false);

        // Apply theme before super.onCreate and setContentView to avoid flickering
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        accelText = findViewById(R.id.accel_text);
        lightText = findViewById(R.id.light_text);
        proximityText = findViewById(R.id.proximity_text);
        themeSwitch = findViewById(R.id.theme_switch);

        // Set the switch state based on saved preference
        themeSwitch.setChecked(isDarkMode);
        
        // Listener for theme toggle
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                sharedPreferences.edit().putBoolean("isDarkMode", true).apply();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                sharedPreferences.edit().putBoolean("isDarkMode", false).apply();
            }
        });

        // Get SensorManager system service
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager != null) {
            // Attempt to get default sensors from the device
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

            // Inform the user if a sensor is not available on their hardware
            if (accelerometer == null) accelText.setText("Accelerometer not available");
            if (lightSensor == null) lightText.setText("Light sensor not available");
            if (proximitySensor == null) proximityText.setText("Proximity sensor not available");
        } else {
            Log.e(TAG, "SensorManager is null!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register listeners for all available sensors
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (proximitySensor != null) {
            // Registering the proximity sensor. 
            // Note: Proximity sensor often only updates when the distance changes.
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister listeners to save battery when the app is in the background
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    /**
     * Called when there is a new sensor event.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();

        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                // values[0]: X-axis, values[1]: Y-axis, values[2]: Z-axis
                accelText.setText(String.format("X: %.2f, Y: %.2f, Z: %.2f", 
                        event.values[0], event.values[1], event.values[2]));
                break;

            case Sensor.TYPE_LIGHT:
                // values[0]: Ambient light level in lux
                lightText.setText(String.format("Luminosity: %.2f lx", event.values[0]));
                break;

            case Sensor.TYPE_PROXIMITY:
                // values[0]: Proximity distance in centimeters. 
                // Many sensors only return binary values: 0 for "near" and maxRange for "far".
                float distance = event.values[0];
                proximityText.setText(String.format("Distance: %.2f cm", distance));
                
                // Debug log to verify if events are being received
                Log.d(TAG, "Proximity event received: " + distance);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // This method is called when the sensor's accuracy changes.
        // We don't need to handle it for this simple monitor.
    }
}