package bkav.android.btalk.esim.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.esim.BtalkEsimIconView;
import bkav.android.btalk.esim.ISimProfile;

// Bkav HuyNQN create 28/11/2019
public class BtalkEsimAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<ISimProfile> mList;

    private boolean mIsSpecialFirstItem; // Bkav HuyNQN dung de xet layout cho sim dau tien cua slot0

    public void setSpecialFirstItem(boolean specialFirstItem) {
        mIsSpecialFirstItem = specialFirstItem;
    }


    public interface ESimAdapterListener {

        void setClickItemProfile(ISimProfile profile);
    }

    public void setListener(ESimAdapterListener mListener) {
        this.mListener = mListener;
    }

    private ESimAdapterListener mListener;

    public BtalkEsimAdapter(Context mContext, List<ISimProfile> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public int getItemViewType(int position) {
//        if (mList.size() > 5) { // Bkav HuyNQN ve GridLayout
//            return 2;
//        } else { // Bkav HuyNQN ve LinearLayout
//            if (position == 0) {
//                return 0;
//            } else
//                return 2;
//        }
        //Bkav QuangNDb thiet ke moi khong co arrow o item dau tien
        return 2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            if (mIsSpecialFirstItem) {
                mIsSpecialFirstItem = false;
                View viewSpecial = LayoutInflater.from(mContext).inflate(R.layout.btalk_esim_list_item_special_view_left, parent, false);
                return new BtalkEsimSpecialViewHolder(viewSpecial);
            }
            View viewSpecial = LayoutInflater.from(mContext).inflate(R.layout.btalk_esim_list_item_special_view, parent, false);
            return new BtalkEsimSpecialViewHolder(viewSpecial);
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.btalk_esim_list_item_view, parent, false);
        return new BtalkEsimViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        switch (viewHolder.getItemViewType()) {
            case 0:
                BtalkEsimSpecialViewHolder holderSpecial = (BtalkEsimSpecialViewHolder) viewHolder;
                ISimProfile p = mList.get(position);
                holderSpecial.mNameProfile.setText(p.getSimNameSetting());
                holderSpecial.mSimPossition.setText(String.valueOf(p.getProfileIndex()));
                holderSpecial.mSimPossition.setTextColor(p.getColor());
                holderSpecial.mImageSim.setColorImageResource(p.getColor());
                if (p.getNickNameProfile() != null && !p.getNickNameProfile().trim().isEmpty()) {
                    holderSpecial.mNumberPhoneProfile.setVisibility(View.VISIBLE);
                    holderSpecial.mNumberPhoneProfile.setText(p.getNickNameProfile()); // + " " + showSimState(p.getSimProfileState())
                } else {
                    holderSpecial.mNumberPhoneProfile.setVisibility(View.GONE);
                }
                if ("Zalo".equals(p.getNameSimProfile())) {
                    holderSpecial.mNameProfile.setText(mContext.getResources().getText(R.string.call_with_zalo));
                    holderSpecial.mSimPossition.setVisibility(View.GONE);
                    holderSpecial.mImageSim.setImageResource(R.drawable.ic_zalo);
                } else if ("Viber".equals(p.getNameSimProfile())) {
                    holderSpecial.mNameProfile.setText(mContext.getResources().getText(R.string.call_with_viber));
                    holderSpecial.mSimPossition.setVisibility(View.GONE);
                    holderSpecial.mImageSim.setImageResource(R.drawable.ic_viber);
                }
                break;

            case 2:
                BtalkEsimViewHolder holder = (BtalkEsimViewHolder) viewHolder;
                ISimProfile profile = mList.get(position);
                holder.mNameProfile.setText(profile.getSimNameSetting());
                holder.mSimPossition.setText(String.valueOf(profile.getProfileIndex()));
                holder.mSimPossition.setTextColor(profile.getColor());
                holder.mImageSim.setColorImageResource(profile.getColor());

                if (profile.getNickNameProfile() != null && !profile.getNickNameProfile().trim().isEmpty()) {
                    holder.mNumberPhoneProfile.setVisibility(View.VISIBLE);
                    holder.mNumberPhoneProfile.setText(profile.getNickNameProfile()); // + " " + showSimState(profile.getSimProfileState())
                } else {
                    holder.mNumberPhoneProfile.setVisibility(View.GONE);
                }
                if ("Zalo".equals(profile.getNameSimProfile())) {
                    holder.mNameProfile.setText(mContext.getResources().getText(R.string.call_with_zalo));
                    holder.mSimPossition.setVisibility(View.GONE);
                    holder.mImageSim.setImageResource(R.drawable.ic_zalo);
                } else if ("Viber".equals(profile.getNameSimProfile())) {
                    holder.mNameProfile.setText(mContext.getResources().getText(R.string.call_with_viber));
                    holder.mSimPossition.setVisibility(View.GONE);
                    holder.mImageSim.setImageResource(R.drawable.ic_viber);
                }
                break;
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class BtalkEsimViewHolder extends RecyclerView.ViewHolder {

        private BtalkEsimIconView mImageSim;
        private TextView mNameProfile;
        private TextView mNumberPhoneProfile;
        private TextView mSimPossition;

        public BtalkEsimViewHolder(View itemView) {
            super(itemView);
            mImageSim = itemView.findViewById(R.id.icon_sim);
            mNameProfile = itemView.findViewById(R.id.name_profile);
            mNumberPhoneProfile = itemView.findViewById(R.id.number_profile);
            mSimPossition = itemView.findViewById(R.id.sim_possiton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ISimProfile profile = mList.get(getAdapterPosition());
                    if (mListener != null) {
                        mListener.setClickItemProfile(profile);
                    }
                }
            });

        }
    }

    public class BtalkEsimSpecialViewHolder extends RecyclerView.ViewHolder {

        private BtalkEsimIconView mImageSim;
        private TextView mNameProfile;
        private TextView mNumberPhoneProfile;
        private TextView mSimPossition;

        public BtalkEsimSpecialViewHolder(View itemView) {
            super(itemView);
            mImageSim = itemView.findViewById(R.id.icon_sim);
            mNameProfile = itemView.findViewById(R.id.name_profile);
            mNumberPhoneProfile = itemView.findViewById(R.id.number_profile);
            mSimPossition = itemView.findViewById(R.id.sim_possiton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ISimProfile profile = mList.get(getAdapterPosition());
                    if (mListener != null) {
                        mListener.setClickItemProfile(profile);
                    }
                }
            });
        }
    }

}
