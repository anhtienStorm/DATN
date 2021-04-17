package bkav.android.btalk.contacts.editcontact;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.contacts.editor.EventFieldEditorView;

import bkav.android.btalk.R;
import bkav.android.btalk.contacts.BtalkCompactKindSectionView;

public class BtalkEventFieldsEditorView extends EventFieldEditorView {
    public BtalkEventFieldsEditorView(Context context) {
        super(context);
    }

    public BtalkEventFieldsEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BtalkEventFieldsEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Anhdts sua lai nut delete thanh 2 nut them va xoa
     */
    protected boolean inflateDeleteContainer() {
        mDelete = (ImageView) findViewById(R.id.delete_button);
        mDeleteContainer = findViewById(R.id.delete_button_container);
        mDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // defer removal of this button so that the pressed state is visible shortly
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        // Don't do anything if the view is no longer attached to the window
                        // (This check is needed because when this {@link Runnable} is executed,
                        // we can't guarantee the view is still valid.
                        if (!mIsAttachedToWindow) {
                            return;
                        }
                        // Send the delete request to the listener (which will in turn call
                        // deleteEditor() on this view if the deletion is valid - i.e. this is not
                        // the last {@link Editor} in the section).
                        // Anhdts neu xoa hang ben duoi thi hien lai nut add hang ben tren
                        if (mListener != null) {
                            mListener.onDeleteRequested(BtalkEventFieldsEditorView.this);
                            ViewGroup parent = (ViewGroup) getParent();
                            int indexVictim = parent.indexOfChild(BtalkEventFieldsEditorView.this);
                            View viewAbove = parent.getChildAt(indexVictim - 1);
                            if (viewAbove != null && viewAbove instanceof BtalkTextFieldsEditorView) {
                                viewAbove.findViewById(R.id.add_button).setVisibility(VISIBLE);
                            }
                        }
                    }
                });
            }
        });

        mDeleteContainer.findViewById(R.id.add_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeleteContainer.findViewById(R.id.add_button).setVisibility(INVISIBLE);
                if (mListener != null) {
                    mListener.onRequest(BtalkCompactKindSectionView.REQUEST_ADD);
                }
            }
        });

        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), 0);
        return true;
    }
}
