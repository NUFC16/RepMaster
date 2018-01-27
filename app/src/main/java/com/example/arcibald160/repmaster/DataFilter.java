package com.example.arcibald160.repmaster;

import java.util.ArrayList;

public class DataFilter {

    public DataFilter () {

    }

    private static final float
            ALPHA = 0.25f, // low pass filter const
            NO_MOTION = 0.0f;

    // we require DELAY_BUFFER_SIZE (9) * SENSOR_DELAY (~200ms) ~= 2 sec of idle state
    // used to determine start and end of an exercise
    private static final int DELAY_BUFFER_SIZE = 9;

    public ArrayList<Float> lowPassFilter(ArrayList<Float> input) {
        ArrayList<Float> output = new ArrayList<Float>();
        output.add(input.get(0)); // first element

        for (int i=1; i<input.size(); i++) {
            output.add(output.get(i-1) + ALPHA * (input.get(i) - output.get(i-1)));
        }
        return output;
    }

    public ArrayList<Float> cutoffFilter(ArrayList<Float> values, ArrayList<Float> gyroscope_values) {
        ArrayList<Float> cutoffArray = new ArrayList<Float>();

        int start = this.findStartIndex(gyroscope_values),
                end = this.findEndIndex(gyroscope_values, values);

        // create subarray
        for (int i=start; i<=end; i++) {
            cutoffArray.add(values.get(i));
        }

        if (cutoffArray.size() == 0) {
            return values;
        }

        // add element on last position that is lower then last element of thresholdValues
        // this is needed in order to count last rep if motion was on its way up but never felt down
        int lastIdx = values.size() - 1;
        cutoffArray.add(values.get(lastIdx) - 2.0f);
        return cutoffArray;
    }

    private int findStartIndex(ArrayList<Float> gyroscope_values) {
        boolean writeOnChange = false;
        int counter = 0,
                endIdx = gyroscope_values.size() / 2 - 1;

        for(int i=0; i<=endIdx; i++) {

            if (gyroscope_values.get(i) == NO_MOTION) {
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

    private int findEndIndex(ArrayList<Float> gyroscope_values, ArrayList<Float> values) {
        boolean writeOnChange = false;
        int counter = 0,
                startIdx = gyroscope_values.size() - 2,
                endIdx = gyroscope_values.size() / 2;

        for(int i=startIdx; i>=endIdx; i--) {
            if ( gyroscope_values.get(i) == NO_MOTION) {
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
        return values.size() - 1;
    }
}
