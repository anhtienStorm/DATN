package bkav.android.btalk.messaging.ui.mediapicker.emoticon;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.Factory;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 14/04/2017.
 * adapter cua list emoticon dung trong lop EmoticonView
 */

public class EmoticonViewAdapter extends RecyclerView.Adapter<EmoticonViewAdapter.EmoticonViewHolder> {

    public interface OnEmoticonClickListener {

        void onEmoticonClick(String emoticonText);
    }
    private Context mContext;
    private OnEmoticonClickListener mListener;
    private List<EmoticonViewData> mListData = new ArrayList<>();

    public EmoticonViewAdapter(OnEmoticonClickListener mListener) {
        this.mListener = mListener;
    }

    public void updateList(List<EmoticonViewData> listData) {
        this.mListData = listData;
        notifyDataSetChanged();
    }

    @Override
    public EmoticonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = Factory.get().getApplicationContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        return new EmoticonViewHolder(inflater.inflate(R.layout.emoticon_item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(EmoticonViewHolder holder, int position) {
        final EmoticonViewData data = mListData.get(position);
        final String emoji = data.getText();
        holder.mIcon.setImageResource(data.getResId());
        holder.mIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onEmoticonClick(emoji);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    static class EmoticonViewHolder extends RecyclerView.ViewHolder {

        ImageView mIcon;

        public EmoticonViewHolder(View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.emoticon);
        }
    }
}
