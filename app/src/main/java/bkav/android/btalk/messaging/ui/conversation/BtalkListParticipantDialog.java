package bkav.android.btalk.messaging.ui.conversation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.view.LayoutInflater;
import android.view.View;

import com.android.dialer.util.TelecomUtil;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.util.UriUtil;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.esim.dialog_choose_sim.DialogChooseSimFragment;

/**
 * Created by quangnd on 31/08/2017.
 */

public class BtalkListParticipantDialog extends DialogFragment implements BtalkListParticipantAdapter.BtalkListParticipantHost{

    private static final String CONVERSATION_KEY = "conversation_key";
    private RecyclerView mListParticipant;
    private BtalkListParticipantAdapter mAdapter;
    // Bkav HienDTk: truyen fragment sang de show dialog choose sim
    private static FragmentManager fragmentManagerCall;
    public static BtalkListParticipantDialog newInstance(ArrayList<ParticipantData> data, FragmentManager fragmentManager) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(CONVERSATION_KEY, data);
        BtalkListParticipantDialog fragment = new BtalkListParticipantDialog();
        fragment.setArguments(args);
        fragmentManagerCall = fragmentManager;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.btalk_show_list_participant_dialog, null);
                builder.setView(view);
        mListParticipant = (RecyclerView) view.findViewById(R.id.list_participant);
        mListParticipant.setHasFixedSize(true);
        mListParticipant.setLayoutManager(new LinearLayoutManager(getActivity()));
        List<ParticipantData> participantDatas = getArguments().getParcelableArrayList(CONVERSATION_KEY);
        mAdapter = new BtalkListParticipantAdapter(participantDatas,this);
        mListParticipant.setAdapter(mAdapter);
        return builder.create();
    }

    @Override
    public void onDismissDialog() {
        dismiss();
    }

    // Bkav HienDTk: fix bug - BOS-3241 - Start
    @Override
    public void onClickDialogChooseSim(String phone) {
        PhoneAccountHandle accountDefault = TelecomUtil.getDefaultOutgoingPhoneAccount(getContext(),
                PhoneAccount.SCHEME_TEL);
        // Bkav HienDTk: neu dat che do hoi truoc khi hoi thi show dialog choose sim
        if (accountDefault == null && BtalkCallLogCache.getCallLogCache(getContext()).isHasSimOnAllSlot()) {
            DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(phone);
            dialogChooseSimFragment.show(fragmentManagerCall, "chooseSim");
        } else {
            final Intent intent = new Intent(Intent.ACTION_CALL,
                    Uri.parse(UriUtil.SCHEME_TEL + phone));
            getContext().startActivity(intent);
        }

        dismiss();
    }
    // Bkav HienDTk: fix bug - BOS-3241 - End
}
