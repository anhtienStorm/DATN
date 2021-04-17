package bkav.android.btalk.messaging;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.esim.ISimProfile;
import bkav.android.btalk.messaging.custom_view.BtalkEsimIconView;

public class BtalkSendEsimAdapter extends RecyclerView.Adapter<BtalkSendEsimAdapter.ItemSendEsimViewHolder> {

    public interface EsimMessageAdapterListener {

        void itemEsimOnClickListener(int position, ISimProfile profile);
    }

    private EsimMessageAdapterListener mListener;

    public void setListener(EsimMessageAdapterListener listener) {
        this.mListener = listener;
    }

    private List<ISimProfile> mList;
    private Context mContext;

    public BtalkSendEsimAdapter(Context mContext, List<ISimProfile> mList) {
        this.mList = mList;
        this.mContext = mContext;
    }

    @Override
    public ItemSendEsimViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.btalk_list_item_esim_send_view, parent, false);
        return new ItemSendEsimViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemSendEsimViewHolder holder, int position) {
        ISimProfile simProfile = mList.get(position);
        holder.mSimName.setText(simProfile.getSimNameSetting()); // + " : " + getSimState(simProfile.getSimProfileState())
        if (simProfile.getNickNameProfile() == null || simProfile.getNickNameProfile().trim().isEmpty()) {
            holder.mPhoneNumber.setVisibility(View.GONE);
        } else {
            holder.mPhoneNumber.setText(simProfile.getNickNameProfile());
        }
//        holder.mImageSend.setCustomImageResource(simProfile.getColor(), position);
        // Bkav HienDTk: fix loi icon sim bi cat khi cai dat form chu to => BOS-2657 - Start
        holder.mImageSend.setImageResource(position,  mContext.getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size), simProfile.getColor());
        // Bkav HienDTk: fix loi icon sim bi cat khi cai dat form chu to => BOS-2657 - End
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ItemSendEsimViewHolder extends RecyclerView.ViewHolder {

        private TextView mSimName;
        private TextView mPhoneNumber;
        private BtalkEsimIconView mImageSend;

        public ItemSendEsimViewHolder(View itemView) {
            super(itemView);
            mSimName = itemView.findViewById(R.id.name);
            mPhoneNumber = itemView.findViewById(R.id.phone_number);
            mImageSend = itemView.findViewById(R.id.image_send);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ISimProfile profile = mList.get(getAdapterPosition());
                    if (mListener != null) {
                        mListener.itemEsimOnClickListener(getAdapterPosition(), profile);
                    }
                }
            });
        }
    }

    public void unbind() {
        mListener = null;
    }
}
