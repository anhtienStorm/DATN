package bkav.android.btalk.messaging.ui.mediapicker.emoticon;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.android.messaging.datamodel.data.DraftMessageData;
import com.android.messaging.datamodel.data.MessagePartData;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 14/04/2017.
 * class hien thi vao xu ly giao dien emoticon
 */

public class EmoticonView extends FrameLayout implements EmoticonViewAdapter.OnEmoticonClickListener {

    public interface HostInterface extends DraftMessageData.DraftMessageSubscriptionDataProvider {

        void onEmoticonClick(MessagePartData messagePartData);
    }

    private RecyclerView mRecyclerEmoticon;

    private EmoticonViewAdapter mAdapter;

    private Context mContext;

    private int mColumnsGrid = COLUMNS_COUNT_PORT;

    private static final int COLUMNS_COUNT_PORT = 7;

    private static final int COLUMNS_COUNT_LAND = 10;

    private HostInterface mHost;

    public void setHost(HostInterface host) {
        this.mHost = host;
    }

    public EmoticonView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRecyclerEmoticon = (RecyclerView) findViewById(R.id.recycler_emoticon);
        mRecyclerEmoticon.setHasFixedSize(true);
        mRecyclerEmoticon.setLayoutManager(new GridLayoutManager(mContext, mColumnsGrid));
        mAdapter = new EmoticonViewAdapter(this);
        mRecyclerEmoticon.setAdapter(mAdapter);
        mAdapter.updateList(EmoticonViewData.getListEmoticon());
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mColumnsGrid = COLUMNS_COUNT_PORT;
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mColumnsGrid = COLUMNS_COUNT_LAND;
        }
    }

    @Override
    public void onEmoticonClick(String emoticonText) {
        //Bkav QuangNDb xu ly sau
        if (mHost != null) {
            final MessagePartData textItem =
                    new MessagePartData(emoticonText);
            mHost.onEmoticonClick(textItem);
        }
    }
}
