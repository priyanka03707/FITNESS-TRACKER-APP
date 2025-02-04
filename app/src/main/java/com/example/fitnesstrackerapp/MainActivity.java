package com.example.fitnesstrackerapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int totalSteps = 0;
    private int previousTotalSteps = 0;
    private TextView stepCountText, distanceText, caloriesText;
    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stepCountText = findViewById(R.id.step_count);
        distanceText = findViewById(R.id.distance);
        caloriesText = findViewById(R.id.calories);

        loadPreviousSteps(); // Load previous step count
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Check and request permission for activity recognition
        checkPermission();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        ACTIVITY_RECOGNITION_REQUEST_CODE);
            } else {
                initializeStepSensor();
            }
        } else {
            initializeStepSensor();
        }
    }

    private void initializeStepSensor() {
        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepSensor != null) {
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
            } else {
                stepCountText.setText("Step Sensor Not Available!");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeStepSensor(); // Start step tracking if permission granted
            } else {
                Toast.makeText(this, "Permission Denied! Steps tracking may not work.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            totalSteps = (int) event.values[0];
            int currentSteps = totalSteps - previousTotalSteps;
            stepCountText.setText("Steps: " + currentSteps);

            float distance = currentSteps * 0.762f; // Approximate 1 step = 76.2 cm
            float calories = currentSteps * 0.04f; // Approximate 0.04 kcal per step

            distanceText.setText("Distance: " + String.format("%.2f", distance) + " m");
            caloriesText.setText("Calories: " + String.format("%.2f", calories) + " kcal");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No action needed
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSteps(); // Save step count when app is paused
    }

    private void loadPreviousSteps() {
        SharedPreferences sharedPreferences = getSharedPreferences("stepData", MODE_PRIVATE);
        previousTotalSteps = sharedPreferences.getInt("previousSteps", 0);
    }

    private void saveSteps() {
        SharedPreferences sharedPreferences = getSharedPreferences("stepData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("previousSteps", totalSteps);
        editor.apply();
    }
}
