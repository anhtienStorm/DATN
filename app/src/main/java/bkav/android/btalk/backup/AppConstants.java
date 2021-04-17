package bkav.android.btalk.backup;

public class AppConstants {
    public static final String GOOGLE_TRANSPORT = "com.google.android.gms/.backup.BackupTransportService";
    
    // Permissions
    public static final String PERMISSIONS_TRANSPORT = 
            "vn.com.bkav.mobile.syncdatabk.permissions.SYNC_TRANSACTION";
    
    // Restore
    public static final String RESTORE_ACTION_TRANSPORT = 
            "vn.com.bkav.mobile.syncdatabk.action.SYNC_TRANSACTION_RESTORE";
    public static final String TYPE_RESTORE_FINISH = "type_restore_finish";
    
    // Backup
    public static final String BACKUP_ACTION_TRANSPORT_LOCAl = 
            "vn.com.bkav.mobile.syncdatabk.action.SYNC_TRANSACTION_BACKUP_LOCAL";
    public static final String BACKUP_IS_SMS = "is_backup_sms";
    public static final String BACKUP_IS_CONTACT= "is_backup_contact";
    public static final String BACKUP_IS_CALLLOG = "is_backup_calllog";
    
    public static final String RESTORE_ACTION_TRANSPORT_LOCAl = 
            "vn.com.bkav.mobile.syncdatabk.action.SYNC_TRANSACTION_RESTORE_LOCAL";
    
    public static final String BACKUP_ACTION_TRANSPORT = 
            "vn.com.bkav.mobile.syncdatabk.action.SYNC_TRANSACTION_BACKUP";
    public static final String TYPE_BACKUP = "type_backup";
    
    // Other
    public static final String POWER_SAVER_ACTION_TRANSPORT = "com.bkav.android.bcleaner.ACTION_POWER_SAVER";
    public static final String DATA_MOBILE_ACTION_TRANSPORT = "com.bkav.android.bcleaner.ACTION_DATA_MOBILE";
    public static final String DATA_MOBILE_ON_OFF = "action_data_mobile";
}
