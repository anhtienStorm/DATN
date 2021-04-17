package bkav.android.btalk.esim.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.esim.BtalkEsimIconView;
import bkav.android.btalk.esim.ISimProfile;

public class BtalkEsimDialogAdapter extends RecyclerView.Adapter<BtalkEsimDialogAdapter.BtalkEsimViewHolder> {

    public interface ESimAdapterListener {

        void setClickItemProfile(ISimProfile profile);
    }

    public static final int INDEX_SLOT = 3;


    private ESimAdapterListener mListener;
    private Context mContext;
    private List<ISimProfile> mESimList = new ArrayList<>();

    public BtalkEsimDialogAdapter(List<ISimProfile> eSimList) {
        mESimList = eSimList;
    }

    @Override
    public BtalkEsimViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new BtalkEsimViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.btalk_esim_dialog_list_item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(BtalkEsimViewHolder holder, int position) {
        ISimProfile profile = mESimList.get(position);
        holder.mNameProfile.setText(profile.getSimNameSetting());
        //Bkav QuangNDb lay profile index hien thi dung so sim
        holder.mSimPossition.setText(String.valueOf(profile.getProfileIndex()));
        holder.mSimPossition.setTextColor(profile.getColor());
        holder.mImageSim.setColorIconDialogChoose(profile.getColor());
        if (profile.getNickNameProfile() != null && !profile.getNickNameProfile().trim().isEmpty()) {
            holder.mNumberPhoneProfile.setVisibility(View.VISIBLE);
            holder.mNumberPhoneProfile.setText(profile.getNickNameProfile());
        } else {
            holder.mNumberPhoneProfile.setVisibility(View.GONE);
        }

        if ("Zalo".equals(profile.getNameSimProfile())) {
            holder.mNameProfile.setText(mContext.getResources().getText(R.string.call_with_zalo));
            holder.mSimPossition.setVisibility(View.GONE);
            holder.mImageSim.setImageResource(R.drawable.ic_zalo_ott);
            holder.mNumberPhoneProfile.setVisibility(View.GONE);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(44, 0, 39, 0);
            holder.mImageSim.setLayoutParams(lp);
        } else if ("Viber".equals(profile.getNameSimProfile())) {
            holder.mNameProfile.setText(mContext.getResources().getText(R.string.call_with_viber));
            holder.mSimPossition.setVisibility(View.GONE);
            holder.mImageSim.setImageResource(R.drawable.ic_viber_ott);
            holder.mNumberPhoneProfile.setVisibility(View.GONE);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(44, 0, 39, 0);
            holder.mImageSim.setLayoutParams(lp);
        }
    }

    @Override
    public int getItemCount() {
        return mESimList.size();
    }

    public void setListener(ESimAdapterListener listener) {
        this.mListener = listener;
    }

    class BtalkEsimViewHolder extends RecyclerView.ViewHolder {

        private BtalkEsimIconView mImageSim;
        private TextView mNameProfile;
        private TextView mNumberPhoneProfile;
        private TextView mSimPossition;

        BtalkEsimViewHolder(View itemView) {
            super(itemView);
            mImageSim = itemView.findViewById(R.id.icon_sim);
            mNameProfile = itemView.findViewById(R.id.name_profile);
            mNumberPhoneProfile = itemView.findViewById(R.id.number_profile);
            mSimPossition = itemView.findViewById(R.id.sim_possiton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ISimProfile profile = mESimList.get(getAdapterPosition());
                    if (mListener != null) {
                        mListener.setClickItemProfile(profile);
                    }
                }
            });

        }
    }

}
