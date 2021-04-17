package bkav.android.btalk.backup;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;

/**
 * Cac ham Utils
 * Anhdts
 */
public class AppUtils {

    public static final boolean DEBUG = false;

    /**
     * Ham ghi log
     *
     * @param log Noi dung truyen vao
     *  @param packageName lay packageName cua app de tao thu muc
     */
    public static void writeLog(String log, String packageName) {
        if (!DEBUG) {
            return;
        }
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                // Check BCleaner folder
                // Bkav HienDTk: thuc hien chuan hoa ten thu muc -> khong tao thu muc cap 1 nua
                File BCleanerFolder = new File(Environment.getExternalStorageDirectory() + "/Android/data", packageName);
//                File BCleanerFolder = new File(Environment.getExternalStorageDirectory(), "BKAV");
                if (!BCleanerFolder.exists()) {
                    if (!BCleanerFolder.mkdirs()) {
                        return;
                    }
                }
                RandomAccessFile randomAccessFile =
                        new RandomAccessFile(new File(BCleanerFolder, "BtalkBackup.log"), "rw");
                randomAccessFile.seek(randomAccessFile.length());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss", Locale.getDefault());
                String dateWithoutTime = sdf.format(new Date());
                String tmp = dateWithoutTime + ":" + log + "\n\n";
                randomAccessFile.write(tmp.getBytes());
                randomAccessFile.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
