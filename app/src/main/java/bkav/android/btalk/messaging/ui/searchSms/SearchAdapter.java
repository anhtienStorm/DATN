package bkav.android.btalk.messaging.ui.searchSms;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.datamodel.data.ConversationMessageData;
import com.android.messaging.util.Dates;

import java.util.ArrayList;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.util.BtalkCharacterUtil;

/**
 * Created by quangnd on 10/06/2017.
 * Adapter cua SearchFragment
 */

public class SearchAdapter extends RecyclerCursorAdapter<SearchViewHolder, ConversationMessageData> {

    private String mQuery;

    public SearchAdapter(Context context) {
        super(context);
    }

    @Override
    protected ConversationMessageData getItem(int position) {
        mCursor.moveToPosition(position);
        ConversationMessageData data = new ConversationMessageData();
        data.binSearch(mCursor);
        return data;
    }

    public void setQuery(String query) {
        mQuery = query;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.btalk_search_list_item_view, parent, false);
        return new SearchViewHolder(mContext, view);
    }

    private static final String TAG = "SearchAdapter";

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {
        ConversationMessageData data = getItem(position);
        ConversationListItemData conversationData = data.getConversationListItemData();
//        if (conversationData != null) {
            holder.mData = data;
            holder.mClickListener = mItemClickListener;
            holder.mRootView.setOnClickListener(holder);
            holder.mRootView.setOnLongClickListener(holder);
            holder.getAvatar().setImageSearchResourceUri(Uri.parse(conversationData.getIcon()), data.getSenderContactId()
                    , data.getSenderContactLookupKey(), data.getSenderNormalizedDestination()
                    , data.getConversationId(), conversationData.getName());
            holder.getAvatar().setClickable(false);
            holder.mName.setText(conversationData.getName());

            holder.mTimeStamp.setText(Dates.getBtalkConversationTimeString(data.getReceivedTimeStamp()).toString());

            if (mQuery != null) {

                // We need to make the search string bold within the full message
                // Get all of the start positions of the query within the messages
                ArrayList<Integer> indices = new ArrayList<>();
                int index;
                //HienDTk: fix loi String java.lang.String.toLowerCase()' on a null object reference
                if(data.getText() != null)
                {
                    index = BtalkCharacterUtil.get().convertToNotLatinCode(data.getText().toLowerCase()).indexOf(BtalkCharacterUtil.get().convertToNotLatinCode(mQuery.toLowerCase()));
                    while (index >= 0) {
                        indices.add(index);
                        index = data.getText().toLowerCase().indexOf(mQuery.toLowerCase(), index + 1);
                    }
                }


                // Make all instances of the search query bold
                SpannableStringBuilder sb = new SpannableStringBuilder(data.getText());
                for (int i : indices) {
                    ForegroundColorSpan span = new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.btalk_orange_color));
                    //Bkav ToanNTe fix BOS-4188: java.lang.CharSequence.length()' on a null object reference
                    if (mQuery != null) {
                        sb.setSpan(span, i, i + mQuery.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                    holder.mSnippet.setText(sb);
                }

                // Bkav TienNAb: To dam cac ky tu cua NameConversation trung voi tu khoa tim kiem
                ArrayList<Integer> indicesNameConvesation = new ArrayList<>();

                int indexNameConvesation = BtalkCharacterUtil.get().convertToNotLatinCode(conversationData.getName().toLowerCase()).indexOf(BtalkCharacterUtil.get().convertToNotLatinCode(mQuery.toLowerCase()));
                while (indexNameConvesation >= 0) {
                    indicesNameConvesation.add(indexNameConvesation);
                    indexNameConvesation = conversationData.getName().toLowerCase().indexOf(mQuery.toLowerCase(), indexNameConvesation + 1);
                }

                // Make all instances of the search query bold
                SpannableStringBuilder sbNameConvesation = new SpannableStringBuilder(conversationData.getName());
                for (int i : indicesNameConvesation) {
                    ForegroundColorSpan span = new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.btalk_orange_color));
                    sbNameConvesation.setSpan(span, i, i + mQuery.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                }
                holder.mName.setText(sbNameConvesation);
            }
//        }else {
//        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

}
