package com.example.arcibald160.repmaster;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

public class RepManager implements SensorEventListener {
    /*
    cutoff filtar is removed in this version -> real time calculation (start on sound signal, end on desired rep count)
    gyroscope is thus not needed
     */
    private static final float IGNORE_RANGE = 0.4f; // detect minimal change which we consider significant enough

    private int numberOfReps = 0;
    private ArrayList<Float>
            thresholdValues = new ArrayList<Float>();
//            deviceMotion = new ArrayList<Float>();
    private ArrayList<ArrayList<Float>>
            gravityAxis = new ArrayList<ArrayList<Float>>(3);

    private TextView showValue;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer, senGyroscope, senGravity;
    private FileManager appFiles;
    private DataFilter dFilter;
    private ArrayList<Float> filteredData = new ArrayList<Float>();


    private static final String TAG = "RepManager";

    public RepManager(Context context, TextView showText) {
        senSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        senGravity = senSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        dFilter = new DataFilter();
        this.showValue = showText;

        gravityAxis.add(new ArrayList<Float>());
        gravityAxis.add(new ArrayList<Float>());
        gravityAxis.add(new ArrayList<Float>());
        Log.v(TAG, String.valueOf(gravityAxis.size()));
    }

    // this is FileManager method, but we need context of an activity
    public void makeFilesVisibleOnPC(Context c) {
        appFiles.makeFilesVisibleOnPC(c);
    }

    public void register(String username, String exercise, String realNumberOfReps) {
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senGyroscope , SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this,senGravity, SensorManager.SENSOR_DELAY_NORMAL);

        String [] folders = new String[]{"/rawAccelero", "/rawGyro", "/filtered_accelero", "/rawGravity", "/cutoffGravity"};
        appFiles = new FileManager(folders, username, exercise, realNumberOfReps);
        numberOfReps = 0;
    }

    public void unregister() {
        senSensorManager.unregisterListener(this);
    }

    public int getReps() {
        this.calculate();
        return numberOfReps;
    }


    public String getExcersise(){
        String exercise = "Unknown exercise";
        float [] axisPercentageVector = calculate_exercise();

        if (axisPercentageVector[1] >= 0.57f) { // dominant y and x
            exercise =  "Pull ups";
        } else if (axisPercentageVector[1] + axisPercentageVector[2] >= 0.70f) { // y+z >= ~75
            exercise = "Squats";
        } else if (axisPercentageVector[0] + axisPercentageVector[2] >= 0.80f) { // dominant x(1) and z(2), x+z >= 85
            exercise = "Push ups";
        }
        this.clear_variables();
        return exercise ;

    }

    private float [] calculate_exercise() {
        //raw gravity
//        appFiles.writeToFile(gravityAxis.get(0), 3, "RawgravityX", false);
//        appFiles.writeToFile(gravityAxis.get(1), 3, "RawgravityY", false);
//        appFiles.writeToFile(gravityAxis.get(2), 3, "RawgravityZ", false);

        float xAvg, yAvg,zAvg;
//        gravityAxis.set(0, dFilter.cutoffFilter(gravityAxis.get(0), this.deviceMotion));
//        gravityAxis.set(1, dFilter.cutoffFilter(gravityAxis.get(1), this.deviceMotion));
//        gravityAxis.set(2, dFilter.cutoffFilter(gravityAxis.get(2), this.deviceMotion));

        //cutoff gravity
        appFiles.writeToFile(gravityAxis.get(0), 4, "GravityX", false);
        appFiles.writeToFile(gravityAxis.get(1), 4, "GravityY", false);
        appFiles.writeToFile(gravityAxis.get(2), 4, "GravityZ", false);

        float sumX = 0.0f, sumY = 0.0f, sumZ = 0.0f, sumAll = 0.0f;
        for (int i = 0; i < gravityAxis.get(0).size(); i++){
            sumX += gravityAxis.get(0).get(i);
            sumY += gravityAxis.get(1).get(i);
            sumZ += gravityAxis.get(2).get(i);
        }

        xAvg = sumX / (float) gravityAxis.get(0).size();
        yAvg = sumY / (float) gravityAxis.get(1).size();
        zAvg = sumZ / (float) gravityAxis.get(2).size();


        appFiles.writeToFile("\n Average: " + xAvg, 4, "GravityX", true);
        appFiles.writeToFile("\n Average: " + yAvg, 4, "GravityY", true);
        appFiles.writeToFile("\n Average: " + zAvg, 4, "GravityZ", true);

        sumAll = xAvg + yAvg + zAvg;
        float [] axisPercentageVector = {xAvg / sumAll, yAvg / sumAll, zAvg / sumAll};

        appFiles.writeToFile("\n Percentage: " + axisPercentageVector[0], 4, "GravityX", true);
        appFiles.writeToFile("\n Percentage: " + axisPercentageVector[1], 4, "GravityY", true);
        appFiles.writeToFile("\n Percentage: " + axisPercentageVector[2] + " sumall " + sumAll, 4, "GravityZ", true);

        return axisPercentageVector;
    }

    private void calculate() {
        boolean isRising;
        numberOfReps = 0;
        appFiles.writeToFile(thresholdValues, 0, false);
//        appFiles.writeToFile(deviceMotion, 1, false);

        //thresholdValues = dFilter.cutoffFilter(thresholdValues, deviceMotion);
        filteredData = dFilter.lowPassFilter(thresholdValues);
        appFiles.writeToFile(filteredData, 2, false);
        isRising = (filteredData.get(1) > filteredData.get(0)) ? true : false; // TODO: this val is wrong(find first range of motion: up or down)

        float prevVal = filteredData.get(0),
              currVal;

        for(int i=0; i<filteredData.size(); i++) {
            currVal = filteredData.get(i);

            if (Math.abs(currVal - prevVal) > IGNORE_RANGE) {
                if (isRising == true && (currVal < prevVal)) {
                    // inc counter only on value raise
                    numberOfReps++;
                    appFiles.writeToFile("\n inc " + String.valueOf(numberOfReps) + " position " + (i+1) + "\n", 2,true);
                    isRising = false;
                } else if (isRising == false && (currVal > prevVal)) {
                    isRising = true;
                }
                prevVal = currVal;
            }
        }
        appFiles.writeToFile("\n Number of reps: " + String.valueOf(numberOfReps), 2, true);
        filteredData.clear();
    }

    private void clear_variables() {
        // reset values
        thresholdValues.clear();
//        deviceMotion.clear();
        //gravity values
        gravityAxis.clear();
        gravityAxis.add(new ArrayList<Float>());
        gravityAxis.add(new ArrayList<Float>());
        gravityAxis.add(new ArrayList<Float>());
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = (Math.abs(sensorEvent.values[0]) < 1) ? 0 : sensorEvent.values[0];
            float y = (Math.abs(sensorEvent.values[1]) < 1) ? 0 : sensorEvent.values[1];
            float z = (Math.abs(sensorEvent.values[2]) < 1) ? 0 : sensorEvent.values[2];

            float cumulativeAmplitude = x + y + z;
            thresholdValues.add(cumulativeAmplitude);
        }

//        if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
//            float ignoreValue = 0.2f;
//            float x = (Math.abs(sensorEvent.values[0]) < ignoreValue) ? 0 : sensorEvent.values[0];
//            float y = (Math.abs(sensorEvent.values[1]) < ignoreValue) ? 0 : sensorEvent.values[1];
//            float z = (Math.abs(sensorEvent.values[2]) < ignoreValue) ? 0 : sensorEvent.values[2];
//
//            float cumulativeMotion = x + y + z;
//            deviceMotion.add(cumulativeMotion);
//        }

        if (mySensor.getType() == Sensor.TYPE_GRAVITY){
            float x = Math.abs(sensorEvent.values[0]);
            float y = Math.abs(sensorEvent.values[1]);
            float z = Math.abs(sensorEvent.values[2]);
            gravityAxis.get(0).add(x);
            gravityAxis.get(1).add(y);
            gravityAxis.get(2).add(z);

        }
        int i = 5;
        if (thresholdValues.size() >= i) {
            showValue.setText(String.valueOf(getReps()));
            i+=5;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
