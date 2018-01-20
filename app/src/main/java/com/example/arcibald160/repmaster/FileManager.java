package com.example.arcibald160.repmaster;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FileManager {
    private static String[] FOLDERS;
    private String
            PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RepMaster_debug",
            currentDateTimeString = "";
    //debugg
    ArrayList<ArrayList<File>> appFiles = new ArrayList<ArrayList<File>>();;

    public FileManager(String[] folders) {
        FOLDERS = folders.clone();
        currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        for(int i=0; i<FOLDERS.length; i++) {
            File dir = new File(PATH + FOLDERS[i]);
            dir.mkdirs();
            // Make ArrayList for every folder
            appFiles.add(new ArrayList<File>());
        }
    }

    public void makeFilesVisibleOnPC(Context context) {
        for(int i=0; i<appFiles.size(); i++) {
            for(int j=0; j<appFiles.get(i).size(); j++) {
                MediaScannerConnection.scanFile(context, new String[]{PATH + FOLDERS[i] + "/" + appFiles.get(i).get(j).getName()}, null, null);
            }
        }
    }

    private String arrayToString(ArrayList<Float> array) {
        String s = "";
        for(int i=0; i<array.size(); i++) {
            s += (i+1) + ". " + array.get(i) + "\n";
        }
        return s;
    }

    private File getFile(int i, String filename) {
        filename = (filename == null) ? currentDateTimeString + ".txt" : filename + currentDateTimeString + ".txt";

        for (File file : appFiles.get(i)) {
            if (file.getName().contains(filename)) {
                return file;
            }
        }
        appFiles.get(i).add(new File(PATH + FOLDERS[i], filename));

        int lastIndex = appFiles.get(i).size() -1 ;
        return appFiles.get(i).get(lastIndex);
    }

    private void writeToFile(String data, File file) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file, true);
            writer.append(data);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(ArrayList<Float> array, int folderIndex) {
        writeToFile(this.arrayToString(array), getFile(folderIndex, null));
    }

    // write to certain directory with custom filename
    public void writeToFile(ArrayList<Float> array, int folderIndex, String filename) {
        writeToFile(this.arrayToString(array), getFile(folderIndex, filename));
    }

    public void writeToFile(String data, int folderIndex) {
        writeToFile(data, getFile(folderIndex, null));
    }
}
