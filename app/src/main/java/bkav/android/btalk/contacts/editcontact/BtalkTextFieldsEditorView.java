package bkav.android.btalk.contacts.editcontact;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.contacts.common.model.RawContactModifier;
import com.android.contacts.editor.TextFieldsEditorView;

import bkav.android.btalk.R;
import bkav.android.btalk.contacts.BtalkCompactKindSectionView;

public class BtalkTextFieldsEditorView extends TextFieldsEditorView {
    public BtalkTextFieldsEditorView(Context context) {
        super(context);
    }

    public BtalkTextFieldsEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BtalkTextFieldsEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Anhdts sua view nhap
     */
    @Override
    protected void configEditText(EditText fieldView) {
        fieldView.setBackground(null);
        fieldView.setPadding(0, fieldView.getPaddingTop(), 0, fieldView.getPaddingBottom());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMinFieldHeight = getContext().getResources().getDimensionPixelSize(
                R.dimen.btalk_editor_min_line_item_height);
    }

    /**
     * Anhdts sua lai nut delete thanh 2 nut them va xoa
     */
    @Override
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
                            mListener.onDeleteRequested(BtalkTextFieldsEditorView.this);
                            if (mEntry.isLastView()) {
                                ViewGroup parent = (ViewGroup) getParent();
                                int indexVictim = parent.indexOfChild(BtalkTextFieldsEditorView.this);
                                View viewAbove = parent.getChildAt(indexVictim - 1);
                                if (viewAbove != null && viewAbove instanceof BtalkTextFieldsEditorView) {
                                    viewAbove.findViewById(R.id.add_button).setVisibility(VISIBLE);
                                    ((BtalkTextFieldsEditorView)viewAbove).mEntry.setLastView(true);
                                }
                            }
                        }
                    }
                });
            }
        });

        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), 0);
        return true;
    }

    @Override
    protected void updateEmptiness() {
        super.updateEmptiness();
        if ((mState != null && mKind != null && !RawContactModifier.canInsert(mState, mKind))) {
            mDeleteContainer.findViewById(R.id.add_button).setVisibility(INVISIBLE);
        } else {
            if (!mEntry.isLastView()) {
                mDeleteContainer.findViewById(R.id.add_button).setVisibility(INVISIBLE);
            }
            mDeleteContainer.findViewById(R.id.add_button).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEntry.setLastView(false);
                    mDeleteContainer.findViewById(R.id.add_button).setVisibility(INVISIBLE);
                    if (mListener != null) {
                        mListener.onRequest(BtalkCompactKindSectionView.REQUEST_ADD);
                    }
                }
            });
        }
    }

}
