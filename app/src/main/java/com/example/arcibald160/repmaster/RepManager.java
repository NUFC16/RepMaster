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
    private static final float IGNORERANGE = 0.15f;

    private int numberOfReps = 0;
    private ArrayList<Float> thresholdValues = new ArrayList<Float>();
    private ArrayList<Float> sampleValues = new ArrayList<Float>();

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    public RepManager(Context context){
        senSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
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

            if (Math.abs(currVal - prevVal) > IGNORERANGE) {
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
        // here we have all the values
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

        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            float cumulativeAmplitude = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
            Log.v(TAG, String.format("%.2f", cumulativeAmplitude));
            thresholdValues.add(cumulativeAmplitude);
//            sampleValues.add(cumulativeAmplitude);
//            if (sampleValues.size() >= 20) {
////                Log.v(TAG, "x: " + String.format("%.2f", x) + " y: " + String.format("%.2f", y) + " z: " + String.format("%.2f", z));
//
//                Collections.sort(sampleValues);
//                float median = (sampleValues.get(sampleValues.size() / 2) + sampleValues.get(sampleValues.size() / 2 - 1)) / 2;
//                thresholdValues.add(median);
//                sampleValues.clear();
//            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
