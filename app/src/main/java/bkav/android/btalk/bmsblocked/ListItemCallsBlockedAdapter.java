package bkav.android.btalk.bmsblocked;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import bkav.android.btalk.R;

public class ListItemCallsBlockedAdapter extends RecyclerView.Adapter<ListItemCallsBlockedAdapter.CallsBlockedViewHolder>{
    private Context mContext;
    private List<String> mListPhoneBlocked;
    public ListItemCallsBlockedAdapter(Context context, List<String> list) {
        this.mContext =context;
        this.mListPhoneBlocked = list;
    }

    @NonNull
    @Override
    public CallsBlockedViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item_calls_blocked, viewGroup, false);
        return new CallsBlockedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallsBlockedViewHolder callsBlockedViewHolder, int i) {
            callsBlockedViewHolder.mNumber.setText(mListPhoneBlocked.get(i));
    }

    @Override
    public int getItemCount() {
        return mListPhoneBlocked.size();
    }

    public class CallsBlockedViewHolder extends RecyclerView.ViewHolder{
        private TextView mNumber;
        private ImageView mImgDelete;
        public CallsBlockedViewHolder(@NonNull View itemView) {
            super(itemView);

            mNumber = itemView.findViewById(R.id.number_phone_blocked);
            mImgDelete = itemView.findViewById(R.id.delete_calls_blocked);
            mImgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null){
                        mListener.onDeleteCallsBlocked();
                    }
                }
            });
        }
    }

    public interface CallsBlockedAdapterListener {
        void onDeleteCallsBlocked();
    }

    private CallsBlockedAdapterListener mListener;

    public void setListener(CallsBlockedAdapterListener mListener) {
        this.mListener = mListener;
    }
}
