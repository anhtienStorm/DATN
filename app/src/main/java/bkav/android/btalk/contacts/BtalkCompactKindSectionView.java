package bkav.android.btalk.contacts;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.android.contacts.editor.CompactKindSectionView;
import com.android.contacts.editor.StructuredNameEditorView;

import bkav.android.btalk.R;
import bkav.android.btalk.contacts.editcontact.BtalkEditorUiUtils;

public class BtalkCompactKindSectionView extends CompactKindSectionView {

    public static final int REQUEST_ADD = 6;

    private BtalkStructuredNameEditorView mNameView;

    public BtalkCompactKindSectionView(Context context) {
        this(context, /* attrs =*/ null);
    }

    public BtalkCompactKindSectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public StructuredNameEditorView hookToInfalteStructuredNameEditorView() {
        mNameView = (BtalkStructuredNameEditorView) mLayoutInflater
                .inflate(R.layout.btalk_structured_name_editor_view, mEditors, /* attachToRoot =*/ false);

        // Anhdts them id tim cho de
        mNameView.setId(R.id.editors_name);
        return mNameView;
    }

    public void showAllStructEditorName() {
        if (mNameView != null) {
            mNameView.setIsModeMoreFields(true);
        }
    }

    // Anhdts getResource by mimetype
    @Override
    protected int getResourceId(String mimeType) {
        return BtalkEditorUiUtils.getLayoutResourceId(mimeType);
    }

    // Anhdts doi lai icon
    @Override
    protected Drawable getMimeTypeDrawable(Context context, String mimeType) {
        return BtalkEditorUiUtils.getMimeTypeDrawable(getContext(),
                mimeType);
    }

    private boolean mIsAcceptShowEmptyEditor = true;
    // Anhdts quyet dinh xem co them truong khong

    @Override
    protected void setAcceptShowEmptyField(boolean isAccept) {
        mIsAcceptShowEmptyEditor = isAccept;
    }

    @Override
    protected boolean getIsAcceptShowEmptyEditor() {
        return mIsAcceptShowEmptyEditor;
    }

    // Anhdts request them 1 truong moi
    @Override
    protected void onRequestAdd(int request) {
        if (request == REQUEST_ADD) {
            mIsAcceptShowEmptyEditor = true;
            updateEmptyNonNameEditors(true);
            mIsAcceptShowEmptyEditor = false;
        }
    }

    // Anhdts doi layout giao dien chinh sua biet danh
    public int getIdPhoneticView() {
        return R.layout.btalk_phonetic_name_editor_view;
    }

    // Anhdts doi layout group
    public int getLayoutMembership() {
        return R.layout.btalk_item_group_membership;
    }
}
