package bkav.android.btalk.contacts;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.editor.StructuredNameEditorView;

import bkav.android.btalk.R;

/**
 * AnhNDd: class kê thừa từ StructuredNameEditorView để điều chỉnh view trong lúc chỉnh sửa contact
 */
public class BtalkStructuredNameEditorView extends StructuredNameEditorView {

    public boolean mISModeMoreFields = false;

    public BtalkStructuredNameEditorView(Context context) {
        super(context);
    }

    public BtalkStructuredNameEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BtalkStructuredNameEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setIsModeMoreFields(boolean bool) {
        mISModeMoreFields = bool;
    }

    @Override
    public void setUpVisibleFieldView(EditText fieldView, AccountType.EditField field) {
        //AnhNDd: ẩn đi 2 ô text PREFIX va SUFFIX
        if ((field.column.equals(ContactsContract.CommonDataKinds.StructuredName.PREFIX)
                || field.column.equals(ContactsContract.CommonDataKinds.StructuredName.SUFFIX)) && !mISModeMoreFields) {
            fieldView.setVisibility(GONE);
        }
    }

    /**
     * Anhdts sua view nhap
     */
    @Override
    protected void configEditText(EditText fieldView) {
        fieldView.setBackground(null);
        fieldView.setPadding(0, fieldView.getPaddingTop(), 0, fieldView.getPaddingBottom());
    }

    /**
     * Anhdts doi icon drop down
     * Creates or removes the type/label button. Doesn't do anything if already correctly configured
     */
    @Override
    protected void setupExpansionView(boolean readOnly, boolean shouldExist, boolean collapsed) {
        mExpansionViewContainer.setBackground(ContextCompat.getDrawable(getContext(), collapsed
                ? R.drawable.icon_dropdown
                : R.drawable.icon_dropup));
        mExpansionViewContainer.setVisibility(shouldExist ? View.VISIBLE : View.INVISIBLE);
        mExpansionViewContainer.setEnabled(!readOnly && isEnabled());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMinFieldHeight = getContext().getResources().getDimensionPixelSize(
                R.dimen.btalk_editor_min_line_item_height);
    }

    /**
     * Anhdts
     */
    @Override
    public void setExpansionViewContainerDisabled() {
//        mExpansionViewContainer.setEnabled(false);
//        mExpansionViewContainer.setBackground(null);
    }
}
