package bkav.android.btalk.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

/**
 * Anhdts Class thuc hien viec backup va restore cac file
 */
public class BkavBackupAgent extends BackupAgentHelper {
    @Override
    public void onCreate() {
        super.onCreate();
        FileBackupHelper helperData =
                new FileBackupHelper(this, BackupRestoreConstants.FILE_NAME_SMS,
                        BackupRestoreConstants.FILE_NAME_CALLLOG);
        addHelper(getPackageName(), helperData);
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
                         ParcelFileDescriptor newState) throws IOException {
        synchronized (BkavBackupManagerApplication.sDataLock) {
            AppUtils.writeLog("BkavBackupManagerService::mBackupReceiver::onReceive::Backup", getApplicationContext().getPackageName());
            BkavBackupData backupData = new BkavBackupData(this);
            backupData.doBackupData();
            super.onBackup(oldState, data, newState);
        }
    }


    /**
     * Anhdts
     * get file xong thi xu ly du lieu luu vao co so du lieu
     */
    @Override
    public void onRestoreFinished() {
        AppUtils.writeLog("BkavBackupManagerService::mBackupReceiver::onReceive::Restore", getApplicationContext().getPackageName());
        BkavRestoreData bkavRestoreData = new BkavRestoreData(this);
        bkavRestoreData.doBackupData();
        super.onRestoreFinished();
    }

    /**
     * Anhdts
     * ham nay de get file tu google drive
     */
    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
                          ParcelFileDescriptor newState) throws IOException {
        synchronized (BkavBackupManagerApplication.sDataLock) {
            super.onRestore(data, appVersionCode, newState);
        }
    }
}