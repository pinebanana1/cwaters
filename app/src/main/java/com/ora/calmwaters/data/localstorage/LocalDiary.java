package com.ora.calmwaters.data.localstorage;

import com.ora.calmwaters.LogHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import calmwaters.BuildConfig;

public class LocalDiary {

    private File diaryEntry = null;
    private final String prefix; //file prefix
    private final File mediadirectory;
    private final String visitNum;
    private final String subjectId;
    private final String protocol;

    final String version = BuildConfig.VERSION_NAME;

    public LocalDiary(File path, String prefix, String visitNum, String subjectId, String protocol) {
        mediadirectory = path;
        this.prefix = prefix;
        this.visitNum = visitNum;
        this.subjectId = subjectId;
        this.protocol = protocol;
    }

    /**
     * Create and return a file under the file path /media/{appName or package}/diaries/
     * @return File
     */
    private File prepareDiaryFile() {
//        String currentDate = new SimpleDateFormat(DateFileHeaderFormat.diaryFileName, Locale.getDefault()).format(new Date()); //TODO: Check tz

        SimpleDateFormat gmt = new SimpleDateFormat(FileDateHeaderFormat.GMT, Locale.getDefault());
//        String currentDateTime = new SimpleDateFormat(FileDateHeaderFormat.GMT, Locale.getDefault()).format(new Date());
        String currentDateTime = gmt.format(new Date());

        //create file path via the getExternalMediaDirs()[0] and add a subdirectory "diaries"
        File diaryPath = new File(mediadirectory, "diaries");
        //create a directory using the subject's ID
        File subjectPath = new File(diaryPath, "subject_" + subjectId);
        //create a directory under the subject's ID using their visit number
        File visitPath = new File(subjectPath, "visit_" + visitNum);
        if (!diaryPath.exists()) {
            LogHelper.d("LocalDiary - prepareDiaryFile: creating directory \n" + diaryPath.getAbsolutePath());
            diaryPath.mkdir();
        }
        if(!subjectPath.exists()) {
            LogHelper.d("LocalDiary - prepareDiaryFile: creating directory \n" + subjectPath.getAbsolutePath());
            subjectPath.mkdir();
        }
        if(!visitPath.exists()) {
            LogHelper.d("LocalDiary - prepareDiaryFile: creating directory \n" + visitPath.getAbsolutePath());
            visitPath.mkdir();
        }

        //new File(path, fileName.fileType)
        diaryEntry = new File(visitPath,
                prefix + //set by the calling class
                        "_v" + version + '_' + currentDateTime + ".csv");
        LogHelper.d("LocalDiary - prepareDiaryFile(): diaryEntry's path is " + diaryEntry.getAbsolutePath());
        try {
            boolean fileCreated = diaryEntry.createNewFile();
            if (fileCreated) {
                LogHelper.d("LocalDiary - prepareDiaryFile(): file was created");
            } else {
                LogHelper.e("LocalDiary - prepareDiaryFile(): file could not be created");
            }
            return diaryEntry;
        } catch (IOException e) {
            LogHelper.e("LocalDiary - prepareDiaryFile(): error is\n" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Adding to the file using the parameters
     * @param text
     */
    public void append(String text) {
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(prepareDiaryFile(), true));
//            String currentTime = new SimpleDateFormat("hh:mm:ss: \n", Locale.getDefault()).format(new Date()); //TODO: Check tz
//            buf.append(currentTime+ "\n" + text); //we include the timestamp but we separate the data
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieving the file contents as a String
     * @return file contents or error text
     */
    public String getContents(){
        try {
            StringBuilder output = new StringBuilder();
            Scanner fileReader = new Scanner(diaryEntry);
            while (fileReader.hasNextLine()) {
                output.append(fileReader.nextLine());
            }
            return output.toString();
        } catch (Exception e) {
            LogHelper.e("LocalDiary - getContents(): error is\n" + e.getMessage());
            e.printStackTrace();
            return "Failed to retrieve contents.";
        }

    }

}
