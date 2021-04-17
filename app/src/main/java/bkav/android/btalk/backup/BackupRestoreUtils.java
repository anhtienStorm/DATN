package bkav.android.btalk.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;



import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

public class BackupRestoreUtils {

	public static final boolean BUILD_DEBUG = true;
	public static final int ERROR = -1;
	public static final int OK = 1;
	
	//danh sách email trong danh bạ để kiểm tra trùng email
	public static List<String> listEmailToCheckExist;

	/**
	 * Ghep file.
	 * @return the string
	 */
	public static String ghepFileBackup(Context context, 
			String pathDirStorage, String file1,
			String file2,
			String file3, String filename) {
		String fileName = pathDirStorage + File.separator + filename;

		try {
			RandomAccessFile fo = new RandomAccessFile(fileName, "rw");
			// MAGIC
			String tmpp = "BKAV";
			byte[] a = tmpp.getBytes();
			fo.write(a);

			File fileIn1 = new File(file1);
			File fileIn2 = new File(file2);
			File fileIn3 = new File(file3);

			if (fileIn1.exists()) {
				fo.writeInt((int) fileIn1.length());
			} else
				fo.writeInt(0);

			if (fileIn2.exists()) {
				fo.writeInt((int) fileIn2.length());
			} else
				fo.writeInt(0);

			if (fileIn3.exists()) {
				fo.writeInt((int) fileIn3.length());
			} else
				fo.writeInt(0);
			if (fileIn1.exists()) {
				RandomAccessFile fi = new RandomAccessFile(fileIn1, "r");
				byte[] b = new byte[(int) fi.length()];
				fi.read(b);
				fo.write(b);
				fi.close();
				fileIn1.delete();
			}

			if (fileIn2.exists()) {
				RandomAccessFile fi = new RandomAccessFile(fileIn2, "r");
				byte[] b = new byte[(int) fi.length()];
				fi.read(b);
				fo.write(b);
				fi.close();
				fileIn2.delete();
			}

			if (fileIn3.exists()) {
				RandomAccessFile fi = new RandomAccessFile(fileIn3, "r");
				byte[] b = new byte[(int) fi.length()];
				fi.read(b);
				fo.write(b);
				fi.close();
				fileIn3.delete();
			}
			// MAGIC
			fo.writeInt(0x67891011);
			fo.close();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			return null;
		}
		return fileName;
	}

	/**
	 * Gets the tmp file name.
	 *
	 * @param ctx the ctx
	 * @param extention the extention
	 * @return the tmp file name
	 */
	public static String getTmpFileName(Context ctx, String extention) {

		String LASTDAY_HAS_BACKUP = "lastDayHasBackup";
		StringBuilder sb = new StringBuilder();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		Editor editor = prefs.edit();

		Date d = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());
		String temp = formatter.format(d);
		long time = prefs.getLong(LASTDAY_HAS_BACKUP, 0);
		Date nd = new Date(time);
		String temp2 = formatter.format(nd);
		int fileNo;
		
		if(temp.equals(temp2)) {
			fileNo = prefs.getInt("fileNo", 0);
			editor.putInt("fileNo", fileNo + 1);
			editor.commit();
		} else {
			fileNo = 0;
			editor.putInt("fileNo", 0);
			editor.putLong(LASTDAY_HAS_BACKUP, d.getTime());
			editor.commit();
		}

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
		String nowAsString = df.format(d);
		sb.append(nowAsString);
		sb.append("-");
		sb.append(Integer.toString(fileNo));
		sb.append(extention);

		return sb.toString();
	}

	/**
	 * Compress file.
	 *
	 * @param context the context
	 * @param fileName the file name
	 * @param newFileName the new file name
	 * @param passWord the pass word
	 * @return the int
	 */
	public static int compressFile(Context context,
			String fileName, 
			String newFileName,
			String passWord) {

		FileInputStream fin;
		FileOutputStream fout;
		byte[] initialVector;

		if (passWord == null) {
			initialVector = "3EC0C78C87D49D5F735DC6E8072E349E".getBytes();
		} else if (passWord.length() <= 0) {
			initialVector = "3EC0C78C87D49D5F735DC6E8072E349E".getBytes();
		} else {
			initialVector = md5(passWord.getBytes()).getBytes();
		}
		try {
			fin = new FileInputStream(fileName);
			File ftmp = new File(context.getFilesDir().getAbsolutePath() + "/" + 
			            fileName.substring(fileName.lastIndexOf('/') + 1, fileName.lastIndexOf('.')) + "compress.tmp");
			fout = new FileOutputStream(ftmp);
			DeflaterOutputStream dos = new DeflaterOutputStream(fout);

			for (int c = fin.read(), round = 0, passEncryptRange = 0; c != -1; c = fin.read()) {
				if (passEncryptRange < 77) {
					dos.write(c ^ initialVector[round]);
					round = (round + 1) % 32;
					passEncryptRange++;
				} else {
					dos.write(c);
				}
			}

			// close all handle
			dos.close();
			fin.close();
			fout.close();

			// copy file
			fin = new FileInputStream(ftmp);
			if (newFileName == null) {
				fout = new FileOutputStream(fileName);
			} else {
				File newFile = new File(newFileName);
				fout = new FileOutputStream(newFile);
			}

			byte[] buf = new byte[1024];
			int len;

			while ((len = fin.read(buf)) > 0) {
				fout.write(buf, 0, len);
			}
			fin.close();
			fout.close();
			ftmp.delete();

		} catch (FileNotFoundException e) {
			Log.e("Bkav compress file", e.toString());
			return ERROR;
		} catch (IOException e) {
			Log.e("Bkav compress file", e.toString());
			return ERROR;
		}

		return OK;
	}

	/**
	 * Md5.
	 *
	 * @param bytes the bytes
	 * @return the string
	 */
	public static String md5(byte[] bytes) {
		try {
			MessageDigest digester = MessageDigest.getInstance("MD5");
			digester.update(bytes);
			byte[] digest = digester.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < digest.length; i++) 
			{
				String hex = Integer.toHexString(0xFF & digest[i]);
				if (hex.length() == 1) hexString.append('0');

				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Alert message.
	 *
	 * @param context the context
	 * @param msg the msg
	 */
	public static void AlertMessage(Context context, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Caution!");
		builder.setMessage(msg);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {	
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		builder.show();
	}

	/**
	 * Delete.
	 *
	 * @param file the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void delete(File file) throws IOException {
		if (file != null && file.isDirectory()) {

			if (file.listFiles() != null && file.listFiles().length == 0) {
				file.delete();
			} else {
				File[] files = file.listFiles();
				if (files != null) {
					for (File fileDelete : files) {
						delete(fileDelete);
					}
					// check the directory again, if empty then delete it
					if (file.list().length == 0) {
						file.delete();
					}
				}
			}

		} else {
			if (file != null) {
				file.delete();
			}
		}
	}

	/**
	 * 
	 * @param fileName
	 * @param datFilePath
	 * @param parentTempDir Thu muc temp cha (se duoc su dung de chua dung cac thu muc temp con, moi thu muc temp
	 * con tuong ung voi mot tien trin thuc hien restore du lieu)
	 * @return Duong dan toi thu muc temp cho qua trinh phuc hoi du lieu hien tai
	 */
	public static String dataFileDivider(String datFilePath, String parentTempDir) {
		String tempDirOfSeparationPath = "";
		File fileData = null;
		File tempSeparationDir  = null;
		RandomAccessFile randomAccessFile = null;
		RandomAccessFile fo1 = null;
		RandomAccessFile fo2  = null;
		RandomAccessFile fo3 = null;

		try {
			tempDirOfSeparationPath = parentTempDir + 
					File.separator + 
					String.valueOf(System.currentTimeMillis());

			// Tao thu muc Temp danh cho phien restore nay
			tempSeparationDir = new File(tempDirOfSeparationPath);
			fileData = new File(datFilePath);

			if ( !tempSeparationDir.mkdirs() ) {
				return tempDirOfSeparationPath;
			}

			randomAccessFile = new RandomAccessFile(fileData, "r");

			byte[] a = new byte[4];
			randomAccessFile.read(a, 0, 4);

			// Kiem tra tinh hop le cua file (la du lieu backup cua Bkav)
			if ((a[0]!=66) || (a[1]!=75) || (a[2]!=65) || (a[3]!=86)) {
				randomAccessFile.close();
				return tempDirOfSeparationPath;
			}

			int sizeSms = randomAccessFile.readInt();
			int sizeCon = randomAccessFile.readInt();
			int sizeCall = randomAccessFile.readInt();

			byte[] b = new byte[Math.max(Math.max(sizeSms, sizeCall), sizeCon)];
			randomAccessFile.read(b, 0, sizeSms);

			fo1 = new RandomAccessFile(
					tempDirOfSeparationPath +File.separator+ BackupRestoreConstants.fileTmpBackupSms,
					"rw");
			fo1.write(b, 0, sizeSms);
			fo1.close();

			randomAccessFile.read(b, 0, sizeCon);
			fo2 = new RandomAccessFile(
					tempDirOfSeparationPath +File.separator+ BackupRestoreConstants.fileTmpBackupCon, 
					"rw");
			fo2.write(b, 0, sizeCon);
			fo2.close();

			randomAccessFile.read(b, 0, sizeCall);
			fo3 = new RandomAccessFile(
					tempDirOfSeparationPath +File.separator+ BackupRestoreConstants.fileTmpBackupCallLog,
					"rw");
			fo3.write(b, 0, sizeCall);
			fo3.close();

			randomAccessFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 

		return tempDirOfSeparationPath;
	}

	/**
	 * De compress file.
	 *
	 * @param context the context
	 * @param fileName the file name
	 * @param newFileName the new file name
	 * @param passWord the pass word
	 * @return the int
	 */
	public static int deCompressFile(Context context, String fileName,
			String newFileName, String passWord) {
		FileInputStream fin;
		FileOutputStream fout;
		byte[] initialVector;

		if (passWord == null) {
			initialVector = "3EC0C78C87D49D5F735DC6E8072E349E".getBytes();
		} else if (passWord.length() <= 0) {
			initialVector = "3EC0C78C87D49D5F735DC6E8072E349E".getBytes();
		} else {
			initialVector = md5(passWord.getBytes()).getBytes();
		}
		try {
			// decompress
			File ftmp = new File(context.getFilesDir().getAbsolutePath() + "/" + 
			            fileName.substring(fileName.lastIndexOf('/') + 1, fileName.lastIndexOf('.')) + "decompress.tmp");
			fout = new FileOutputStream(ftmp);

			fin = new FileInputStream(fileName);
			InflaterInputStream dos = new InflaterInputStream(fin);

			for (int c = dos.read(), round = 0, passEncryptRange = 0; c != -1; c = dos.read()) {
				if (passEncryptRange < 77) {
					fout.write(c ^ initialVector[round]);
					round = (round + 1) % 32;
					passEncryptRange++;
				} else {
					fout.write(c);
				}
			}

			dos.close();
			fin.close();
			fout.close();

			// copy file
			fin = new FileInputStream(ftmp);
			if (newFileName == null) {
				fout = new FileOutputStream(fileName);
			} else {
				File newFile = new File(newFileName);
				fout = new FileOutputStream(newFile);
			}

			byte[] buf = new byte[1024];
			int len;
			while ((len = fin.read(buf)) > 0) {
				fout.write(buf, 0, len);
			}
			fin.close();
			fout.close();
			ftmp.delete();
		} catch (FileNotFoundException e) {
			Log.e("BkavDecompressFile", e.toString());
			return ERROR;
		} catch (IOException e) {
			Log.e("BkavDecompressFile", e.toString());
			return ERROR;
		}

		return OK;
	}

	/**
	 * Gets the thread id.
	 *
	 * @param context the context
	 * @param phoneno the phoneno
	 * @return the thread id
	 */
	public final static long getSMSThreadId(Context context, String phoneno) {
		long threadId = 0;
		try {
			String SORT_ORDER = "date DESC";
			final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
			final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");
			int count = 0;
			int i;
			String tmp;

			Cursor cursor = context.getContentResolver().query(SMS_INBOX_CONTENT_URI,
					new String[] { "_id", "thread_id", "address", "person", "date",	"body" }, null, null, SORT_ORDER);

			if (cursor != null) {
				try {
					count = cursor.getCount();
					if (count > 0) {
						cursor.moveToFirst();
						for (i = 0; i < count; i++) {
							tmp = cursor.getString(2);
							if (tmp != null && tmp.equals(phoneno)) {
								threadId = cursor.getLong(1);
								break;
							} else cursor.moveToNext();
						}
					}
				} finally {
					cursor.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return threadId;
	}
	
//	/**
//	 * Write log chung
//	 *
//	 * @param log the log
//	 */
//	public static void writeLog(String log, String filePath)
//	{
//		try 
//		{
//			RandomAccessFile fi = new RandomAccessFile(filePath, "rw");
//			File f = new File(filePath);
//			fi.seek(f.length());
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());             
//			String dateWithoutTime = sdf.format(new Date());
//			String tmp = dateWithoutTime + ":" + log + "\n";
//			fi.write(tmp.getBytes());
//			fi.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * Lay danh dach email trong contact de kiem tra 
	 * truong hop trung lap email khi phuc hoi
	 */
	public static void getContactEmailList(Context context) {
		listEmailToCheckExist = new ArrayList<String>();
		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		if (cur != null) {
    		if (cur.getCount() > 0) {
    			while (cur.moveToNext()) {
    				String id = cur.getString(cur
    						.getColumnIndex(ContactsContract.Contacts._ID));
    				Cursor cur1 = cr.query(
    						ContactsContract.CommonDataKinds.Email.CONTENT_URI,
    						null, ContactsContract.CommonDataKinds.Email.CONTACT_ID
    								+ " = ?", new String[] { id }, null);
    				if (cur1 != null) {
    				    while (cur1.moveToNext()) {
    				        String email = cur1.getString(cur1
    				                .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
    				        listEmailToCheckExist.add(email);
    				    }
    				    cur1.close();
    				}
    			}
    		}
    		cur.close();
		}
	}

	/**
	 * Ham kiem tra trung lap email khi phuc  hoi
	 * 
	 * @param email ten cua email
	 * @return true neu trung, false neu khong trung
	 */
	public static boolean checkEmailExist(String email) {  
	    if (!listEmailToCheckExist.contains(email)) {
	        listEmailToCheckExist.add(email);
	        return false;	        
	    }
		return true;
	}
}
