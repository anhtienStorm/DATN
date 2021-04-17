package bkav.android.btalk.messaging.datamodel.manager;

import android.database.Cursor;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.MessagingContentProvider;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.messaging.datamodel.data.QuickResponseData;

/**
 * Created by quangnd on 14/12/2017.
 */

public class QuickResponseManager {


    /**Bkav QuangNDb Lay danh sanh quick response*/
    public static List<QuickResponseData> getQuickResponseDataList(){
        List<QuickResponseData> dataList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = Factory.get().getApplicationContext()
                    .getContentResolver().query(MessagingContentProvider.QUICK_RESPONSE_URI, null
                            , null, null, "_id desc");
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    final QuickResponseData data = new QuickResponseData();
                    data.bind(cursor);
                    dataList.add(data);
                } while (cursor.moveToNext());
            }
        }finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return dataList;
    }
}
