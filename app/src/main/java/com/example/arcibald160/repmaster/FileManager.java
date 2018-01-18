package com.example.arcibald160.repmaster;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class FileManager {
    private static final String [] FOLDERS = {"/rawAccelero", "/rawGyro", "/filtered_accelero", "/rawGravity", "/cutoffGravity", ""};
    private String
            PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RepMaster_debug",
            currentDateTimeString = "";
    //debugg
    File rawAccelero, rawGyro, filteredAccelero, rawGravity, cutoffGravity;
    ArrayList<File> appFiles = new ArrayList<File>(Arrays.asList(rawAccelero, rawGyro, filteredAccelero, rawGravity, cutoffGravity));

    public FileManager() {
        currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        for(int i=0; i<FOLDERS.length; i++) {
            File dir = new File(PATH + FOLDERS[i]);
            dir.mkdirs();
        }
        for(int i=0; i<appFiles.size(); i++) {
            appFiles.set(i, new File(PATH + FOLDERS[0], currentDateTimeString + ".txt"));
        }
//        rawAccelero = new File(PATH + FOLDERS[0], currentDateTimeString + ".txt");
//        rawGyro = new File(PATH + FOLDERS[1], currentDateTimeString + ".txt");
//        filteredAccelero = new File(PATH + FOLDERS[2], currentDateTimeString + ".txt");
//        rawGravity = new File(PATH + FOLDERS[3], currentDateTimeString + ".txt");
//        cutoffGravity = new File(PATH + FOLDERS[4], currentDateTimeString + ".txt");
    }

    public void makeFilesVisibleOnPC(Context context) {
        for(int i=0; i<appFiles.size(); i++) {
            MediaScannerConnection.scanFile(context, new String[] {PATH + FOLDERS[i] + "/" + currentDateTimeString + ".txt"}, null, null);
        }
//        MediaScannerConnection.scanFile(context, new String[] {PATH + FOLDERS[0] + "/" + currentDateTimeString + ".txt"}, null, null);
//        MediaScannerConnection.scanFile(context, new String[] {PATH + FOLDERS[1] + "/" + currentDateTimeString + ".txt"}, null, null);
//        MediaScannerConnection.scanFile(context, new String[] {PATH + FOLDERS[2] + "/" + currentDateTimeString + ".txt"}, null, null);
//        MediaScannerConnection.scanFile(context, new String[] {PATH + FOLDERS[3] + "/" + currentDateTimeString + ".txt"}, null, null);
//        MediaScannerConnection.scanFile(context, new String[] {PATH + FOLDERS[4] + "/" + currentDateTimeString + ".txt"}, null, null);
    }

    private String arrayToString(ArrayList<Float> array) {
        String s = "";
        for(int i=0; i<array.size(); i++) {
            s += (i+1) + ". " + array.get(i) + "\n";
        }
        return s;
    }

    private File getFile(int i) {
//        File [] l = {rawAccelero, rawGyro, filteredAccelero, rawGravity, cutoffGravity};
        return appFiles.get(i);
    }

    public void writeToFile(String data, int i) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(this.getFile(i), true);
            writer.append(data);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(ArrayList<Float> array, int i) {
        writeToFile(this.arrayToString(array), i);
    }
}
