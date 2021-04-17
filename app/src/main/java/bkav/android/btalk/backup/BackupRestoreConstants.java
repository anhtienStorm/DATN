package bkav.android.btalk.backup;

public class BackupRestoreConstants {


	//=========================== Shared Preferences String Name ===================
	/** Bien shared preference luu thoi diem lan cuoi thuc hien backup tin nhan. */
	public static final String SMSLASTBACKUP = "SMS_lastbackup";

	/** Co lua chon thuc hien backup tin nhan hay khong*/
	public static final String BACKUP_SMS = "backup_sms";

	/** Co lua chon thuc hien backup tin nhan hay khong */
	public static final String BACKUP_CONTACT = "backup_contact";

	/** The Constant BACKUP_CALLLOG. */
	public static final String BACKUP_CALLLOG = "backup_callog";
	
	// File name
	public static final String FILE_NAME_SMS = "sms.bmgr";
	public static final String FILE_NAME_CONTACT = "contact.bmgr";
	public static final String FILE_NAME_CALLLOG = "calllog.bmgr";
    public static final String FILE_NAME_BACKUP = "backup.bms";

	/** The Constant EXTENSION_XML. */
	public static final String EXTENSION_XML = ".bms";
	

	// Header for backup SMS process
	/** The address. */
	public static String ADDRESS_TAG = "a";

	/** The body. */
	public static String BODY_TAG = "b";

	/** The type. */
	public static String TYPE_TAG = "t";

	/** The person. */
	public static String PERSON_TAG = "p";

	/** The date. */
	public static String DATE_TAG = "d";

	/** The sms. */
	public static String SMS_TAG = "s";

	/** The root. */
	public static String ROOT_TAG = "r";

	
	//====================== Constants String =================================

	/** Feilds in SMS Database*/
	public static final String ADDRESS = "address";
	public static final String BODY = "body";
	public static final String DATE = "date";
	public static final String PERSON = "person";
	public static final String TYPE = "type";

	public static final String BACKUP_DIR_NAME = "BACKUP";

	/** The "Temp" Directory name. */
	public static final String TEMP_DIR_NAME = "Temp";


	public static final String fileTmpBackupCon = "tmpCon.bk";
	public static final String fileTmpBackupCallLog = "tmpCall.bk";
	public static final String fileTmpBackupSms = "tmpSMS.bk";

	public static final String VCARD_VERSION_WHEN_RESTORE = "VcardVersionWhenRestore";
	

	//====================== Constants Integer =================================

	public static final int vCard21Code = 1;
	public static final int vCard30Code = 2;
	
	// Ma cac che do thuc hien sao luu du lieu
	public static final int GENERAL_BACKUP_MODE = 1;
	public static final int SCHEDULE_BACKUP_MODE = 2;
	public static final int SMSREMOTE_BACKUP_MODE = 3;

	//Ma kieu du lieu khi hien thi thong bao tien trinh thuc hien
	public static final int CONTACT_TYPE_CODE = 1;
	public static final int SMS_TYPE_CODE = 2;
	public static final int CALL_LOG_TYPE_CODE = 3;

}
