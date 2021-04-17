package bkav.android.btalk.messaging.ui.contact;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.android.ex.chips.RecipientEntry;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.android.messaging.ui.contact.ContactRecipientAutoCompleteView;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by quangnd on 26/04/2017.
 */

public class BtalkContactRecipientAutoCompleteView extends ContactRecipientAutoCompleteView {

    public BtalkContactRecipientAutoCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void hideScrollBar() {
        mScrollView.setVerticalScrollBarEnabled(false);
    }

    @Override
    public Set<String> getSelectedDestinationsNotCanonical() {
        Set<String> set = new HashSet<String>();
        final DrawableRecipientChip[] recips = getText()
                .getSpans(0, getText().length(), DrawableRecipientChip.class);

        for (final DrawableRecipientChip recipient : recips) {
            final RecipientEntry entry = recipient.getEntry();
            if (entry != null && entry.isValid() && entry.getDestination() != null) {
                set.add(entry.getDestination());
            }
        }
        return set;
    }

    @Override
    protected int getIdResDrawable() {
        return android.R.color.transparent;
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        // React:
        switch (id){
            case android.R.id.paste:
                if (mChipsChangeListener.onPasteParticipant(getText().toString())) {
                    return false;
                }
                break;
        }
        return super.onTextContextMenuItem(id);
    }

    /**Bkav QuangNDb Khi focus vao o nhap lien he mo lai giao dien list contact neu can*/
    @Override
    public void onFocusChanged(boolean hasFocus, int direction, Rect previous) {
        super.onFocusChanged(hasFocus, direction, previous);
        if (hasFocus) {
            if (mChipsChangeListener != null) {
                mChipsChangeListener.onResumeRequest();
            }
        }
    }

    @Override
    protected void removeRecipientEntryListener(RecipientEntry entry) {
        if (mChipsChangeListener != null) {
            mChipsChangeListener.onRemoveEntry(entry);
        }
    }

    @Override
    protected void appendRecipientEntryListener(RecipientEntry entry) {
        if (mChipsChangeListener != null) {
            mChipsChangeListener.onInsertEntry(entry);
        }
    }
}
