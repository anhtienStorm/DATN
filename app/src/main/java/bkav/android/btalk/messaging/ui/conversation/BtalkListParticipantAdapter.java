package bkav.android.btalk.messaging.ui.conversation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.util.AvatarUriUtil;
import com.android.messaging.util.UriUtil;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.ui.BtalkContactPhotoView;

/**
 * Created by quangnd on 31/08/2017.
 */

public class BtalkListParticipantAdapter extends RecyclerView.Adapter<BtalkListParticipantAdapter.ParticipantHolder> {

    private List<ParticipantData> mListParticipant = new ArrayList<>();
    private Context mContext;

    public interface BtalkListParticipantHost {

        void onDismissDialog();

        // Bkav HienDTk: fix bug - BOS-3241 - Start
        // Bkav HienDTk: them interface de xu ly viec choose sim ben BtalkListParticipantDialog
        void onClickDialogChooseSim(String phone);
        // Bkav HienDTk: fix bug - BOS-3241 - End
    }

    private BtalkListParticipantHost mHost;

    public BtalkListParticipantAdapter(List<ParticipantData> listParticipant, BtalkListParticipantHost host) {
        this.mListParticipant = listParticipant;
        mHost = host;
    }

    @Override
    public ParticipantHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        return new ParticipantHolder(inflater.inflate(R.layout.btalk_participant_item, parent, false), mContext);
    }

    @Override
    public void onBindViewHolder(ParticipantHolder holder, int position) {
        holder.bind(mListParticipant.get(position), mHost);
    }

    @Override
    public int getItemCount() {
        return mListParticipant.size();
    }

    static class ParticipantHolder extends RecyclerView.ViewHolder {

        BtalkContactPhotoView mIcon;
        TextView mParticipantName;
        TextView mPhone;
        View mRootView;
        Context mContext;

        public ParticipantHolder(View itemView, Context context) {
            super(itemView);
            mRootView = itemView.findViewById(R.id.participant_item);
            mPhone = (TextView) itemView.findViewById(R.id.phone_number);
            mParticipantName = (TextView) itemView.findViewById(R.id.participant_name);
            mIcon = (BtalkContactPhotoView) itemView.findViewById(R.id.conversation_icon);
            mContext = context;
        }

        public void bind(ParticipantData data, final BtalkListParticipantHost mHost) {
            final Uri avatarUri = AvatarUriUtil.createAvatarUri(data);
            mIcon.setImageResourceUriFromParticipant(avatarUri, data.getContactId()
                    , data.getLookupKey(), data.getNormalizedDestination(), data.getId(), data.getDisplayName(false));
            mPhone.setText(data.getNormalizedDestination());
            mParticipantName.setText(data.getDisplayName(true));
            mIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    makeAcall(mHost);
                    mHost.onClickDialogChooseSim(mPhone.getText().toString());
                }
            });
            mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    makeAcall(mHost);
                    mHost.onClickDialogChooseSim(mPhone.getText().toString());
                }
            });
        }

        private void makeAcall(final BtalkListParticipantHost mHost) {
            final Intent intent = new Intent(Intent.ACTION_CALL,
                    Uri.parse(UriUtil.SCHEME_TEL + mPhone.getText().toString()));
            mContext.startActivity(intent);
            mHost.onDismissDialog();
        }
    }
}
