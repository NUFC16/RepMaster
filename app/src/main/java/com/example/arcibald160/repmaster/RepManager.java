package com.example.arcibald160.repmaster;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import java.util.ArrayList;

public class RepManager implements SensorEventListener {
    private static final String TAG = "RepManager";
    private static final float ALPHA = 0.25f;
    private static final float IGNORE_RANGE = 0.15f;

    private int numberOfReps = 0;
    private ArrayList<Float> thresholdValues = new ArrayList<Float>();

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    public RepManager(Context context){
        senSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void register(){
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        numberOfReps = 0;
    }

    public void unregister(){
        senSensorManager.unregisterListener(this);
    }

    public int getReps() {
        this.calculate();
        return numberOfReps;
    }
    private void calculate() {
        boolean isRising;

        thresholdValues = this.lowPassFilter(thresholdValues);
        isRising = (thresholdValues.get(1) > thresholdValues.get(0)) ? true : false;

        float prevVal = thresholdValues.get(0), currVal;
        for(int i=1; i<thresholdValues.size(); i++) {
            currVal = thresholdValues.get(i);

            if (Math.abs(currVal - prevVal) > IGNORE_RANGE) {
                if (isRising == true && (currVal < prevVal)) {
                    // inc counter only on value raise
                    numberOfReps++;
                    isRising = false;
                } else if (isRising == false && (currVal > prevVal)) {
                    isRising = true;
                }
                prevVal = thresholdValues.get(i);
            }
        }
        // reset values
        thresholdValues.clear();
    }

    protected ArrayList<Float> lowPassFilter(ArrayList<Float> input) {
        ArrayList<Float> output = new ArrayList<Float>();
        output.add(0f); // first element
        for ( int i=1; i<input.size(); i++ ) {
            output.add(output.get(i-1) + ALPHA * (input.get(i) - output.get(i-1)));
        }
        return output;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = (Math.abs(sensorEvent.values[0]) < 1) ? 0 : sensorEvent.values[0];
            float y = (Math.abs(sensorEvent.values[1]) < 1) ? 0 : sensorEvent.values[1];
            float z = (Math.abs(sensorEvent.values[2]) < 1) ? 0 : sensorEvent.values[2];

            float cumulativeAmplitude = x + y + z;
            Log.v(TAG, String.format("%.2f", cumulativeAmplitude));
            thresholdValues.add(cumulativeAmplitude);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
