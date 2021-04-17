package bkav.android.btalk.messaging.ui.conversation;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.data.ConversationData;
import com.android.messaging.datamodel.data.DraftMessageData;
import com.android.messaging.ui.conversation.ConversationInputManager;
import com.android.messaging.ui.mediapicker.MediaPicker;
import com.android.messaging.util.ImeUtil;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.ui.mediapicker.BtalkMediaPicker;

/**
 * Created by quangnd on 14/04/2017.
 * class custom lai ConversationInputManager cua source goc
 */

public class BtalkConversationInputManager extends ConversationInputManager {

    private static final int EMOTION_POSITION = 2;

    public BtalkConversationInputManager(Context context, ConversationInputHost host, ConversationInputSink sink, ImeUtil.ImeStateHost imeStateHost,
                                         FragmentManager fm, BindingBase<ConversationData> conversationDataModel, BindingBase<DraftMessageData> draftDataModel, Bundle savedState) {
        super(context, host, sink, imeStateHost, fm, conversationDataModel, draftDataModel, savedState);
    }

    @Override
    protected ConversationMediaPicker initMediaInput() {
        BtalkConversationMediaPicker conversationMediaPicker = new BtalkConversationMediaPicker(this);
        conversationMediaPicker.initMediaPickerIfNull();
        return conversationMediaPicker;
    }

    public class BtalkConversationMediaPicker extends ConversationMediaPicker {

        public BtalkConversationMediaPicker(ConversationInputBase baseHost) {
            super(baseHost);
        }

        @Override
        protected MediaPicker initMediaPicker() {
            return (BtalkMediaPicker)
                    mFragmentManager.findFragmentByTag(MediaPicker.FRAGMENT_TAG);
        }

        @Override
        public void setCurrentPagerPosition() {
            if (mPagerPosition != -1) {
                if (mMediaPicker.getViewPager() != null) {
                    mMediaPicker.getViewPager().setCurrentItem(mPagerPosition);
                }else {
                    mMediaPicker.setCurrentPosition(EMOTION_POSITION);
                }
                mPagerPosition = -1;
            }
        }

        @Override
        public int getCurrentPosition() {
            if (mMediaPicker.getViewPager() != null) {
                return mMediaPicker.getViewPager().getCurrentItem();
            }
            return 0;
        }

        @Override
        protected int getThemeColor() {
            return ContextCompat.getColor(Factory.get().getApplicationContext(), R.color.btalk_actionbar_and_tabbar_bg_no_transparent_color);
        }
    }
}
