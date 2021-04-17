package bkav.android.btalk.messaging.ui.mediapicker.emoticon;

import android.content.Context;
import android.content.res.TypedArray;

import com.android.messaging.Factory;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 14/04/2017.
 * Doi tuong luu tru doi tuong emoticon
 */

public class EmoticonViewData {

    private int mResId;

    private String mText;
    private int mTextId;

    public EmoticonViewData() {
    }

    public EmoticonViewData(int resId, String text) {
        this.mResId = resId;
        this.mText = text;
    }

    public EmoticonViewData(int resId, int textId) {
        this.mResId = resId;
        this.mTextId = textId;
    }

    public int getResId() {
        return mResId;
    }

    public void setResId(int resId) {
        this.mResId = resId;
    }

    public String getText() {
        return new String(Character.toChars(mTextId));
    }

    public void setText(String text) {
        this.mText = text;
    }

    /**
     * BKav QuangNDb method get list emoticon data
     */
    public static List<EmoticonViewData> getListEmoticon() {
        Context context = Factory.get().getApplicationContext();
        List<EmoticonViewData> emoticons = new ArrayList<>();
        final int[] listTextEmoticon = context.getResources().getIntArray(R.array.btalk_list_unicode_emoji);
        final TypedArray listEmotionResId = context.getResources().obtainTypedArray(R.array.btalk_list_icon_id);
        final int size = listTextEmoticon.length;
        for (int i = 0; i < size; i++) {
            final EmoticonViewData data = new EmoticonViewData(listEmotionResId.getResourceId(i,-1), listTextEmoticon[i]);
            emoticons.add(data);
        }
        return emoticons;
    }
}
