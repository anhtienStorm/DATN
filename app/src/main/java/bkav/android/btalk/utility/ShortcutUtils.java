package bkav.android.btalk.utility;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.CallLog;
import android.support.v4.content.ContextCompat;

import com.android.messaging.Factory;

import java.io.File;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.recoder.DeleteCallRecordPathAction;

public class ShortcutUtils {
    private static volatile ShortcutUtils sInstance;
    private static final String RECORD_CALL_DATA = "record_call_data";

    public static ShortcutUtils get() {
        if (sInstance == null) {
            sInstance = new ShortcutUtils();
        }
        return sInstance;
    }

    /**Bkav QuangNDb cap nhat xem xoa hay them shortcut*/
    public void updateCallLogRecordShortcut() {
        //Bkav QuangNDb check de update icon call recorder tren launcher la an hay hien
        if (checkExistCallLogWithCallRecord()) {
            createOrRemoveShortCut(true);
        }else {
            createOrRemoveShortCut(false);
        }
    }

    /**Bkav QuangNDb tao shortcut call record tren launcher*/
    public void createOrRemoveShortCut(boolean creating) {
        Context context = Factory.get().getApplicationContext();
        Intent shortcutIntent = new Intent();
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        shortcutIntent.setComponent(new ComponentName("bkav.android.btalk", "bkav.android.btalk.calllog.recoder.CallLogRecoderActivity"));
        shortcutIntent.setPackage("bkav.android.btalk");
        String action = creating ? "bkav.android.launcher.action.INSTALL_SHORTCUT" : "bkav.android.launcher.action.UNINSTALL_SHORTCUT";
        Intent requestShortcut = new Intent(action);
        requestShortcut.setPackage("bkav.android.launcher3");
        requestShortcut.setComponent(new ComponentName("bkav.android.launcher3", "com.android.launcher3.InstallShortcutReceiver"));
        requestShortcut.putExtra("duplicate", false);
        requestShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getResources().getString(R.string.title_call_recorder));
        requestShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.ic_call_recoder_laucher)));
        requestShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        context.sendBroadcast(requestShortcut);
    }

    /**Bkav QuangNDb convert drawable to bitmap*/
    private  Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**Bkav QuangNDb check xem co du lieu call log co ghi am cuoc goi khong, neu co thi check them co ton tai trong file system khong*/
    private boolean checkExistCallLogWithCallRecord() {
        Context context = Factory.get().getApplicationContext();
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                RECORD_CALL_DATA + " is not null and " + RECORD_CALL_DATA + " != ''", null, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String path = cursor.getString(cursor.getColumnIndex(RECORD_CALL_DATA));
                File file = new File(path);
                if (file.exists()) {
                    count++;
                }else {
                    DeleteCallRecordPathAction.deleteCallRecordPath(path);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return count > 0;
    }
}
