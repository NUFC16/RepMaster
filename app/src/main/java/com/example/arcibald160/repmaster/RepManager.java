package com.example.arcibald160.repmaster;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.util.ArrayList;

public class RepManager implements SensorEventListener {
    private static final float
            ALPHA = 0.25f, // low pass filter const
            IGNORE_RANGE = 0.3f, // detect minimal change which we consider significant enough
            NO_MOTION = 0.0f;

    // we require DELAY_BUFFER_SIZE (9) * SENSOR_DELAY (~200ms) ~= 2 sec of idle state
    // used to determine start and end of an exercise
    private static final int DELAY_BUFFER_SIZE = 9;

    private int numberOfReps = 0;
    private ArrayList<Float>
            thresholdValues = new ArrayList<Float>(),
            deviceMotion = new ArrayList<Float>();

    private SensorManager senSensorManager;
    private Sensor senAccelerometer, senGyroscope;
    private FileManager appFiles;

    private static final String TAG = "RepManager";

    public RepManager(Context context) {
        senSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public void makeFilesVisibleOnPC(Context c) {
        appFiles.makeFilesVisibleOnPC(c);
    }

    public void register() {
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senGyroscope , SensorManager.SENSOR_DELAY_NORMAL);
        appFiles = new FileManager();
        numberOfReps = 0;
    }

    public void unregister() {
        senSensorManager.unregisterListener(this);
    }

    public int getReps() {
        this.calculate();
        return numberOfReps;
    }

    private int findStartIndex() {
        boolean writeOnChange = false;
        int counter = 0,
                endIdx = deviceMotion.size() / 2 - 1;

        for(int i=0; i<=endIdx; i++) {

            if (deviceMotion.get(i) == NO_MOTION) {
                counter++;
            } else if (writeOnChange == true) {
                return i;
            } else {
                counter = 0;
            }

            if (counter >= DELAY_BUFFER_SIZE) {
                writeOnChange = true;
            }
        }
        return 0;
    }

    private int findEndIndex() {
        boolean writeOnChange = false;
        int counter = 0,
            startIdx = deviceMotion.size() - 2,
            endIdx = deviceMotion.size() / 2;

        for(int i=startIdx; i>=endIdx; i--) {
            if ( deviceMotion.get(i) == NO_MOTION) {
                counter++;
            } else if (writeOnChange == true ) {
                return i;
            } else {
                counter = 0;
            }

            if (counter >= DELAY_BUFFER_SIZE) {
                writeOnChange = true;
            }
        }
        return thresholdValues.size() - 1;
    }

    private ArrayList<Float> cutoffFilter() {
        ArrayList<Float> cutoffArray = new ArrayList<Float>();

        int start = this.findStartIndex(),
            end = this.findEndIndex();

        // create subarray
        for (int i=start; i<=end; i++) {
            cutoffArray.add(thresholdValues.get(i));
        }

        if (cutoffArray.size() == 0) {
            return thresholdValues;
        }

        // add element on last position that is lower then last element of thresholdValues
        // this is needed in order to count last rep if motion was on its way up but never felt down
        int lastIdx = thresholdValues.size() - 1;
        cutoffArray.add(thresholdValues.get(lastIdx) - 2.0f);
        return cutoffArray;
    }

    private void calculate() {
        boolean isRising;
        appFiles.writeToFile(thresholdValues, 0);
        appFiles.writeToFile(deviceMotion, 1);
        thresholdValues = this.cutoffFilter();
        thresholdValues = this.lowPassFilter(thresholdValues);
        appFiles.writeToFile(thresholdValues, 2);
        isRising = (thresholdValues.get(1) > thresholdValues.get(0)) ? true : false; // TODO: this val is wrong(find first range of motion: up or down)

        float prevVal = thresholdValues.get(0),
              currVal;

        for(int i=1; i<thresholdValues.size(); i++) {
            currVal = thresholdValues.get(i);

            if (Math.abs(currVal - prevVal) > IGNORE_RANGE) {
                if (isRising == true && (currVal < prevVal)) {
                    // inc counter only on value raise
                    numberOfReps++;
                    appFiles.writeToFile("\n inc " + String.valueOf(numberOfReps) + " position " + (i+1) + "\n", 2);
                    isRising = false;
                } else if (isRising == false && (currVal > prevVal)) {
                    isRising = true;
                }
                prevVal = currVal;
            }
        }
        appFiles.writeToFile("\n Number of reps: " + String.valueOf(numberOfReps), 2);
        // reset values
        thresholdValues.clear();
        deviceMotion.clear();
    }

    protected ArrayList<Float> lowPassFilter(ArrayList<Float> input) {
        ArrayList<Float> output = new ArrayList<Float>();
        output.add(input.get(0)); // first element

        for (int i=1; i<input.size(); i++) {
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
//            Log.v(TAG, String.format("%.2f", cumulativeAmplitude));
            thresholdValues.add(cumulativeAmplitude);
        }

        if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float ignoreValue = 0.2f;
            float x = (Math.abs(sensorEvent.values[0]) < ignoreValue) ? 0 : sensorEvent.values[0];
            float y = (Math.abs(sensorEvent.values[1]) < ignoreValue) ? 0 : sensorEvent.values[1];
            float z = (Math.abs(sensorEvent.values[2]) < ignoreValue) ? 0 : sensorEvent.values[2];

            float cumulativeMotion = x + y + z;
//            Log.v(TAG, String.format("Motion %.2f", cumulativeMotion));
            deviceMotion.add(cumulativeMotion);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
