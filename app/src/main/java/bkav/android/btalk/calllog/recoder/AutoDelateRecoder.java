package bkav.android.btalk.calllog.recoder;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AutoDelateRecoder {

    private static AutoDelateRecoder sInstance;

    public static final int AUTO_DELETE_RECODER_30 = 30;
    public static final int AUTO_DELETE_RECODER_60 = 60;
    public static final int AUTO_DELETE_RECODER_90 = 90;


    public static AutoDelateRecoder getInstance() {
        if (sInstance == null) {
            sInstance = new AutoDelateRecoder();
        }
        return sInstance;
    }

    /**
     * HienDTk: tinh so ngay de xoa file ghi am
     */
    public void getDay(String path, int dateAutoDeleteRecoder) {
        if(dateAutoDeleteRecoder > 0){
            File file = new File(path);
            SimpleDateFormat myFormat = new SimpleDateFormat("dd MM yyyy");
            if (file.exists()) {
                File[] f = file.listFiles();

                for (int i = 0; i < f.length; i++) {
                    String inputStringDateFile = getDateFileCreate(f[i].toString());
                    String inputStringDateNow = getDateNow();
                    try {
                        Date dateFileCreate = myFormat.parse(inputStringDateFile);
                        Date dateNow = myFormat.parse(inputStringDateNow);
                        long diff = dateNow.getTime() - dateFileCreate.getTime();
                        long date = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

                        if (dateAutoDeleteRecoder == AUTO_DELETE_RECODER_30) {
                            if (date >= AUTO_DELETE_RECODER_30) {
                                deleteFileRecoder(f[i].toString());
                            }
                        } else if (dateAutoDeleteRecoder == AUTO_DELETE_RECODER_60) {
                            if (date >= AUTO_DELETE_RECODER_60) {
                                deleteFileRecoder(f[i].toString());
                            }
                        } else if (dateAutoDeleteRecoder == AUTO_DELETE_RECODER_90) {
                            if (date >= AUTO_DELETE_RECODER_90) {
                                deleteFileRecoder(f[i].toString());
                            }
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    /**
     * HienDTk: lay ngay ma file ghi am duoc tao
     */
    public String getDateFileCreate(String path) {

        int day = 0, month = 0, year = 0;

        File file = new File(path);
        Path filePath = file.toPath();

        BasicFileAttributes attributes = null;
        try {
            attributes =
                    Files.readAttributes(filePath, BasicFileAttributes.class);
        } catch (IOException exception) {
            System.out.println("Exception handled when trying to get file " +
                    "attributes: " + exception.getMessage());
        }
        long milliseconds = attributes.creationTime().to(TimeUnit.MILLISECONDS);
        if ((milliseconds > Long.MIN_VALUE) && (milliseconds < Long.MAX_VALUE)) {
            Date creationDate =
                    new Date(attributes.creationTime().to(TimeUnit.MILLISECONDS));
            day = creationDate.getDate();
            month = creationDate.getMonth() + 1;
            year = creationDate.getYear() + 1900;
        }
        return day + " " + month + " " + year;
    }

    /**
     * HienDTk: Lay ngay hien tai cua he thong
     */
    public String getDateNow() {
        int dayNow = 0, monthNow = 0, yearNow = 0;
        Calendar c = Calendar.getInstance();
        dayNow = c.get(Calendar.DAY_OF_MONTH);
        monthNow = c.get(Calendar.MONTH) + 1;
        yearNow = c.get(Calendar.YEAR);
        return dayNow + " " + monthNow + " " + yearNow;
    }

    /**
     * HienDTk: xoa ghi am cuoc goi
     */
    private void deleteFileRecoder(String path) {
        DeleteCallRecordPathAction.deleteCallRecordPath(path);
        File fileDelete = new File(path);
        if (fileDelete.exists()) {
            fileDelete.delete();
        }
    }
}
