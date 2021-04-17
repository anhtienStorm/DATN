package bkav.android.btalk.contacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.ContactSaveService;
import com.android.contacts.activities.ContactEditorAccountsChangedActivity;
import com.android.contacts.common.MoreContactUtils;
import com.android.contacts.common.SimContactsConstants;
import com.android.contacts.common.lettertiles.LetterTileDrawable;
import com.android.contacts.common.model.Contact;
import com.android.contacts.common.model.RawContactDelta;
import com.android.contacts.common.model.ValuesDelta;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.common.util.MaterialColorMapUtils;
import com.android.contacts.editor.CompactPhotoEditorView;
import com.android.contacts.editor.CompactPhotoSelectionFragment;
import com.android.contacts.editor.CompactRawContactsEditorView;
import com.android.contacts.editor.ContactEditorBaseFragment;
import com.android.contacts.editor.ContactEditorUtils;
import com.android.contacts.editor.EditorIntents;
import com.android.contacts.editor.PhotoActionPopup;
import com.android.contacts.editor.PhotoSourceDialogFragment;
import com.android.contacts.editor.StructuredNameEditorView;
import com.android.contacts.quickcontact.QuickContactActivity;
import com.android.contacts.util.ContactPhotoUtils;
import com.android.contacts.util.SchedulingUtils;
import com.android.contacts.widget.QuickContactImageView;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BkavContactEditorAccountsChangedActivity;
import bkav.android.btalk.contacts.editcontact.BtalkCompactPhotoEditorView;
import bkav.android.btalk.contacts.editcontact.BtalkMultiShrinkScroller;
import bkav.android.btalk.utility.Config;

import static bkav.android.btalk.text_shortcut.Util.getPackageName;
import static com.android.contacts.ContactSaveService.EXTRA_CUSTOM_RINGTONE;
import static com.android.contacts.ContactSaveService.EXTRA_CUSTOM_RINGTONE_SMS;


public class BtalkCompactContactEditorFragment extends ContactEditorBaseFragment implements
        CompactRawContactsEditorView.Listener, CompactPhotoEditorView.Listener {

    private BtalkMultiShrinkScroller mScroller;

    private QuickContactImageView mPhotoView;

    private MaterialColorMapUtils mMaterialColorMapUtils;

    private boolean mHasComputedThemeColor = false;

    private static final String KEY_PHOTO_RAW_CONTACT_ID = "photo_raw_contact_id";
    private static final String KEY_UPDATED_PHOTOS = "updated_photos";

    private long mPhotoRawContactId;
    private Bundle mUpdatedPhotos = new Bundle();

    private PhotoSourceDialogFragment.Listener mPhotoHandler;

    private BtalkCompactPhotoEditorView mPhotoContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        setHasOptionsMenu(true);

        final View view = inflater.inflate(
                R.layout.btalk_compact_contact_editor_fragment, container, false);
        mContent = (LinearLayout) view.findViewById(R.id.raw_contacts_editor_view);
        mScroller = (BtalkMultiShrinkScroller) view.findViewById(R.id.compact_contact_editor_fragment);

        mScroller.initialize(mMultiShrinkScrollerListener, getEditorActivity().getTitleView());
        // mScroller needs to perform asynchronous measurements after initalize(), therefore
        // we can't mark this as GONE.
        if (Intent.ACTION_EDIT.equals(mAction) ||
                com.android.contacts.activities.ContactEditorBaseActivity.ACTION_EDIT.equals(mAction)) {
            mScroller.setVisibility(View.INVISIBLE);
        }
        mPhotoView = (QuickContactImageView) view.findViewById(R.id.photo);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(KEY_PHOTO_RAW_CONTACT_ID, mPhotoRawContactId);
        outState.putParcelable(KEY_UPDATED_PHOTOS, mUpdatedPhotos);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return revert();
        } else if (item.getItemId() == R.id.remove_photo) {
            mPhotoHandler.onRemovePictureChosen();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void bindEditors() {
        if (!isReadyToBindEditors()) {
            return;
        }

        // Add input fields for the loaded Contact
        final CompactRawContactsEditorView editorView = getContent();
        editorView.setListener(this);
        editorView.setState(mState, getMaterialPalette(), mViewIdGenerator, mPhotoId,
                mHasNewContact, mIsUserProfile, mAccountWithDataSet);

        // Bkav TienNAb: bo giao dien chon anh cho danh ba khi luu vao SIM
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Bkav TienNAb: check khac null de fix loi crash khi nhan vao thiet lap ho so trong cai dat danh ba
            if (isSaveOnSim()) {
                mScroller.findViewById(R.id.content_action_photo).setVisibility(View.GONE);
            } else {
                mScroller.findViewById(R.id.content_action_photo).setVisibility(View.VISIBLE);
            }
        }

        if (mHasNewContact && !TextUtils.isEmpty(mReadOnlyDisplayName)) {
            mReadOnlyNameEditorView = editorView.getPrimaryNameEditorView();
            editorView.maybeSetReadOnlyDisplayNameAsPrimary(mReadOnlyDisplayName);
        }

        // Set up the photo widget
        editorView.setPhotoListener(this);
        mPhotoRawContactId = editorView.getPhotoRawContactId();
        // If there is an updated full resolution photo apply it now, this will be the case if
        // the user selects or takes a new photo, then rotates the device.
        final Uri uri = (Uri) mUpdatedPhotos.get(String.valueOf(mPhotoRawContactId));
        if (uri != null) {
            editorView.setFullSizePhoto(uri);
        }

        // The editor is ready now so make it visible
        editorView.setEnabled(isEnabled());
        editorView.setVisibility(View.VISIBLE);

        // Refresh the ActionBar as the visibility of the join command
        // Activity can be null if we have been detached from the Activity.
        invalidateOptionsMenu();

        // Anhdts
        mEditorName = editorView.getPrimaryNameEditorView();

        showView();

        if (mPhotoHandler != null) {
            mPhotoContainer = (BtalkCompactPhotoEditorView) editorView.findViewById(R.id.photo_editor);
            mPhotoContainer.setListener(mPhotoHandler);
        }
    }

    @Override
    protected void setStateForPhoneMenuItems(Contact contact) {
        super.setStateForPhoneMenuItems(contact);
        // Anhdts bind nhac chuong
        String titleRingtone;
        if (getContent().findViewById(R.id.action_set_rington) != null) {
            if (!TextUtils.isEmpty(mCustomRingtone) && mCustomRingtone != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(mCustomRingtone));
                titleRingtone = ringtone.getTitle(getActivity());

            } else if (mCustomRingtone == null) {
                titleRingtone = getString(R.string.default_text);
            } else {
                titleRingtone = getString(R.string.no_ringtone);
            }
            ((TextView) getContent().findViewById(R.id.action_set_rington).
                    findViewById(R.id.content_action)).setText(titleRingtone);
        }

        if (mCustomRingtoneSms == null && contact != null) {
            mCustomRingtoneSms = contact.getCustomRingtoneSms();
        }
        if (getContent().findViewById(R.id.action_set_ringmess) != null) {
            if (!TextUtils.isEmpty(mCustomRingtoneSms) && mCustomRingtoneSms != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(mCustomRingtoneSms));
                titleRingtone = ringtone.getTitle(getActivity());
            } else if (mCustomRingtoneSms == null) {
                titleRingtone = getString(R.string.default_text);
            }else {
                titleRingtone = getString(R.string.no_ringtone);
            }
            ((TextView) getContent().findViewById(R.id.action_set_ringmess).
                    findViewById(R.id.content_action)).setText(titleRingtone);
        }
    }

    private boolean isReadyToBindEditors() {
        if (mState.isEmpty()) {
            return false;
        }
        if (mIsEdit && !mExistingContactDataReady) {
            return false;
        }
        if (mHasNewContact && !mNewContactDataReady) {
            return false;
        }
        return true;
    }

    @Override
    protected View getAggregationAnchorView(long rawContactId) {
        return getContent().getAggregationAnchorView();
    }

    @Override
    protected void setGroupMetaData() {
        if (mGroupMetaData != null) {
            getContent().setGroupMetaData(mGroupMetaData);
        }
    }

    @Override
    protected boolean doSaveAction(int saveMode, Long joinContactId) {
        // Anhdts luu gia tri tai khoan mac dinh
        if (((BtalkCompactRawContactsEditorView) mContent).isSetDefaultAccount()) {
            ContactEditorUtils.getInstance(mContext).saveDefaultAndAllAccounts(mAccountWithDataSet);
        }

        final Intent intent = ContactSaveService.createSaveContactIntent(mContext, mState,
                SAVE_MODE_EXTRA_KEY, saveMode, isEditingUserProfile(),
                ((Activity) mContext).getClass(),
                BtalkCompactContactEditorActivity.ACTION_SAVE_COMPLETED, mUpdatedPhotos,
                JOIN_CONTACT_ID_EXTRA_KEY, joinContactId);
            intent.putExtra(EXTRA_CUSTOM_RINGTONE, mCustomRingtone);
            intent.putExtra(EXTRA_CUSTOM_RINGTONE_SMS, mCustomRingtoneSms);
        return startSaveService(mContext, intent, saveMode);
    }

    @Override
    protected void joinAggregate(final long contactId) {
        final Intent intent = ContactSaveService.createJoinContactsIntent(
                mContext, mContactIdForJoin, contactId, BtalkCompactContactEditorActivity.class,
                BtalkCompactContactEditorActivity.ACTION_JOIN_COMPLETED);
        mContext.startService(intent);
    }

    public void removePhoto() {
        getContent().removePhoto();
        mUpdatedPhotos.remove(String.valueOf(mPhotoRawContactId));
    }

    /**
     * Remove the Sim photo info.
     */
    @Override
    protected void removeSimPhoto() {
        if (mHasNewContact && !mNewLocalProfile) {
            RawContactDelta rawContactDelta = mState.get(0);
            if (SimContactsConstants.ACCOUNT_TYPE_SIM.equals(
                    rawContactDelta.getAccountType())) {
                // As it is sim account, we should remove the photo info.
                for (ValuesDelta valuesDelta
                        : rawContactDelta.getMimeEntries(
                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
                    valuesDelta.getAfter().remove(ContactsContract.CommonDataKinds.Photo.PHOTO);
                }
                mUpdatedPhotos = new Bundle();
            }
        }
    }

    public void updatePhoto(Uri uri) throws FileNotFoundException {
        final Bitmap bitmap = ContactPhotoUtils.getBitmapFromUri(getActivity(), uri);
        if (bitmap == null || bitmap.getHeight() <= 0 || bitmap.getWidth() <= 0) {
            Toast.makeText(mContext, com.android.contacts.R.string.contactPhotoSavedErrorToast,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mUpdatedPhotos.putParcelable(String.valueOf(mPhotoRawContactId), uri);
        getContent().updatePhoto(uri);
        mScroller.updatePhoto();
    }

    public void setPrimaryPhoto(CompactPhotoSelectionFragment.Photo photo) {
        getContent().setPrimaryPhoto(photo);

        // Update the photo ID we will try to match when selecting the photo to display
        mPhotoId = photo.photoId;
    }

    @Override
    public void onNameFieldChanged(long rawContactId, ValuesDelta valuesDelta) {
        final Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        acquireAggregationSuggestions(activity, rawContactId, valuesDelta);
    }

    @Override
    public void onRebindEditorsForNewContact(RawContactDelta oldState,
                                             AccountWithDataSet oldAccount, AccountWithDataSet newAccount) {
        mNewContactAccountChanged = true;
        mAccountWithDataSet = newAccount;
        rebindEditorsForNewContact(oldState, oldAccount, newAccount);
        // Anhdts refresh lai view cuon
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mScroller.updatePhotoTintAndDropShadow();
            }
        }, 300);
    }

    @Override
    public void onBindEditorsFailed() {
        final Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            Toast.makeText(activity, com.android.contacts.R.string.compact_editor_failed_to_load,
                    Toast.LENGTH_SHORT).show();
            activity.setResult(Activity.RESULT_CANCELED);
            activity.finish();
        }
    }

    @Override
    public void onEditorsBound() {
        getLoaderManager().initLoader(LOADER_GROUPS, null, mGroupsLoaderListener);
    }

    @Override
    public void onPhotoEditorViewClicked() {
        if (isEditingMultipleRawContacts()) {
            final ArrayList<CompactPhotoSelectionFragment.Photo> photos = getContent().getPhotos();
            if (photos.size() > 1) {
                updatePrimaryForSelection(photos);
                // For aggregate contacts, the user may select a new super primary photo from among
                // the (non-default) raw contact photos, or source a new photo.
                getEditorActivity().selectPhoto(photos, getPhotoMode());
                return;
            }
        }
        // For contacts composed of a single writable raw contact, or raw contacts have no more
        // than 1 photo, clicking the photo view simply opens the source photo dialog
        getEditorActivity().changePhoto(getPhotoMode());
    }

    // This method override photo's primary flag based on photoId and set the photo currently
    // shown in the editor to be the new primary no matter how many primary photos there are in
    // the photo picker. This is because the photos returned by "getPhoto" may contain 0, 1,
    // or 2+ primary photos and when we link contacts in the editor, the photos returned may change.
    // We need to put check mark on the photo currently shown in editor, so we override "primary".
    // This doesn't modify anything in the database,so there would be no pending changes.
    private void updatePrimaryForSelection(ArrayList<CompactPhotoSelectionFragment.Photo> photos) {
        for (CompactPhotoSelectionFragment.Photo photo : photos) {
            if (photo.photoId == mPhotoId) {
                photo.primary = true;
            } else {
                photo.primary = false;
            }
            updateContentDescription(photo);
        }
    }

    private void updateContentDescription(CompactPhotoSelectionFragment.Photo photo) {
        if (!TextUtils.isEmpty(photo.accountType)) {
            photo.contentDescription = getResources().getString(photo.primary ?
                            R.string.photo_view_description_checked :
                            R.string.photo_view_description_not_checked,
                    photo.accountType, photo.accountName);
            photo.contentDescriptionChecked = getResources().getString(
                    R.string.photo_view_description_checked,
                    photo.accountType, photo.accountName);
        } else {
            photo.contentDescription = getResources().getString(photo.primary ?
                    R.string.photo_view_description_checked_no_info :
                    R.string.photo_view_description_not_checked_no_info);
            photo.contentDescriptionChecked = getResources().getString(
                    R.string.photo_view_description_checked_no_info);
        }
    }

    @Override
    public void onRawContactSelected(Uri uri, long rawContactId, boolean isReadOnly) {
        final Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
//            final Intent intent = EditorIntents.createEditContactIntentForRawContact(
//                    activity, uri, rawContactId, isReadOnly);
//            activity.startActivity(intent);

            // Bkav HuyNQN su dung giao dien edit cua btalk
            Intent intent = EditorIntents.createCompactEditContactIntent(mLookupUri, null, 0/*photoID*/);
            intent.setPackage("bkav.android.btalk");
            startActivityForResult(intent, QuickContactActivity.REQUEST_CODE_CONTACT_EDITOR_ACTIVITY);
        }
    }

    @Override
    public Bundle getUpdatedPhotos() {
        return mUpdatedPhotos;
    }

    private int getPhotoMode() {
        if (getContent().isWritablePhotoSet()) {
            return isEditingMultipleRawContacts()
                    ? PhotoActionPopup.Modes.MULTIPLE_WRITE_ABLE_PHOTOS
                    : PhotoActionPopup.Modes.WRITE_ABLE_PHOTO;
        }
        return PhotoActionPopup.Modes.NO_PHOTO;
    }

    protected BtalkCompactContactEditorActivity getEditorActivity() {
        return (BtalkCompactContactEditorActivity) getActivity();
    }

    protected StructuredNameEditorView mEditorName;

    @Override
    public void onCreate(Bundle savedState) {
        mMaterialColorMapUtils = new MaterialColorMapUtils(getResources());
        super.onCreate(savedState);

        if (savedState != null) {
            mPhotoRawContactId = savedState.getLong(KEY_PHOTO_RAW_CONTACT_ID);
            mUpdatedPhotos = savedState.getParcelable(KEY_UPDATED_PHOTOS);
        }
    }

    protected CompactRawContactsEditorView getContent() {
        return (BtalkCompactRawContactsEditorView) mContent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.btalk_edit_contact, menu);
    }

    private boolean isFocusFirst = true;

    public void focusEditText() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (mEditorName != null && isFocusFirst) {
                    EditText editText = (EditText) ((LinearLayout) mEditorName.findViewById(R.id.editors)).getChildAt(0);

                    if (editText != null && TextUtils.isEmpty(editText.getText())) {
                        editText.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.showSoftInput(editText, 0);
                            scrollWhenKeyboardShow();
                        }
                    }
                }
                isFocusFirst = false;
            }
        });
    }

    final BtalkMultiShrinkScroller.MultiShrinkScrollerListener mMultiShrinkScrollerListener
            = new BtalkMultiShrinkScroller.MultiShrinkScrollerListener() {
        @Override
        public void onScrolledOffBottom() {

        }

        @Override
        public void onScroll() {
            getEditorActivity().onScrollShrink();
        }

    };


    /**
     * Anhdts san sang hien thi view
     */
    private void showView() {
        if (mScroller != null) {
            mScroller.setVisibility(View.VISIBLE);
            if (mMaterialPalette == null) {
                mScroller.setHeaderTintColor(-43230);
            } else {
                mScroller.setHeaderTintColor(mMaterialPalette.mPrimaryColor);
            }
//            extractAndApplyTintFromPhotoViewAsynchronously(contact);
            SchedulingUtils.doOnPreDraw(mScroller, /* drawNextFrame = */ false,
                    new Runnable() {
                        @Override
                        public void run() {
                            runEntranceAnimation();
                        }
                    });
        }
    }

    private void runEntranceAnimation() {
        mScroller.scrollUpForEntranceAnimation(/* scrollToCurrentPosition */ false);
    }

    private void extractAndApplyTintFromPhotoViewAsynchronously(final Contact mContactData) {
        if (mScroller == null) {
            return;
        }
        final Drawable imageViewDrawable = mPhotoView.getDrawable();
        new AsyncTask<Void, Void, MaterialColorMapUtils.MaterialPalette>() {
            @Override
            protected MaterialColorMapUtils.MaterialPalette doInBackground(Void... params) {

                if (imageViewDrawable instanceof BitmapDrawable && mContactData != null
                        && mContactData.getThumbnailPhotoBinaryData() != null
                        && mContactData.getThumbnailPhotoBinaryData().length > 0) {
                    // Perform the color analysis on the thumbnail instead of the full sized
                    // image, so that our results will be as similar as possible to the Bugle
                    // app.
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(
                            mContactData.getThumbnailPhotoBinaryData(), 0,
                            mContactData.getThumbnailPhotoBinaryData().length);
                    try {
                        final int primaryColor = colorFromBitmap(bitmap);
                        if (primaryColor != 0) {
                            return mMaterialColorMapUtils.calculatePrimaryAndSecondaryColor(
                                    primaryColor);
                        }
                    } finally {
                        bitmap.recycle();
                    }
                }
                if (imageViewDrawable instanceof LetterTileDrawable) {
                    final int primaryColor = ((LetterTileDrawable) imageViewDrawable).getColor();
                    return mMaterialColorMapUtils.calculatePrimaryAndSecondaryColor(primaryColor);
                }
                return MaterialColorMapUtils.getDefaultPrimaryAndSecondaryColors(getResources());
            }

            @Override
            protected void onPostExecute(MaterialColorMapUtils.MaterialPalette palette) {
                super.onPostExecute(palette);
                if (mHasComputedThemeColor) {
                    // If we had previously computed a theme color from the contact photo,
                    // then do not update the theme color. Changing the theme color several
                    // seconds after QC has started, as a result of an updated/upgraded photo,
                    // is a jarring experience. On the other hand, changing the theme color after
                    // a rotation or onNewIntent() is perfectly fine.
                    return;
                }
                // Check that the Photo has not changed. If it has changed, the new tint
                // color needs to be extracted
                if (imageViewDrawable == mPhotoView.getDrawable()) {
                    mHasComputedThemeColor = true;
//                    setThemeColor(palette);
                    // update color and photo in suggestion card
//                    onAggregationSuggestionChange();
                }
            }
        }.execute();
    }

    private int colorFromBitmap(Bitmap bitmap) {
        // Author of Palette recommends using 24 colors when analyzing profile photos.
        final int NUMBER_OF_PALETTE_COLORS = 24;
        final Palette palette = Palette.generate(bitmap, NUMBER_OF_PALETTE_COLORS);
        if (palette != null && palette.getVibrantSwatch() != null) {
            return palette.getVibrantSwatch().getRgb();
        }
        return 0;
    }


    @Override
    public void pickRingTon(boolean isCallRingtone) {
        doPickRingtone(isCallRingtone);
    }

    public void scrollWhenKeyboardShow() {
        mScroller.scrollUpKeyboard();
    }

    public void setListener(PhotoSourceDialogFragment.Listener photoSelectionHandler) {
        mPhotoHandler = photoSelectionHandler;
        if (mPhotoContainer != null) {
            mPhotoContainer.setListener(mPhotoHandler);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onRingtonePicked(Uri pickedUri, boolean isRingtone) {
        Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), pickedUri);
        String title = ringtone.getTitle(getActivity());
        ((TextView) getContent().findViewById(isRingtone ? R.id.action_set_rington :
                R.id.action_set_ringmess).findViewById(R.id.content_action)).setText(title);
        super.onRingtonePicked(pickedUri, isRingtone);
    }

    public void onRemovePhotoPicker() {
        mPhotoContainer.setISend(new BtalkCompactPhotoEditorView.ISendUri() {
            @Override
            public void sendUri(Uri uri) {
                mGetUri.getUri(uri);
            }
        });
        mPhotoContainer.removePhotoPicker();
    }

    public void setIGet(IGetUri getUri) {
        mGetUri = getUri;
    }

    private IGetUri mGetUri;
    public interface IGetUri {
        void getUri(Uri uri);
    }


    public void showPhotoPickerFragment() {
        mScroller.scrollOffBottom();
    }

    /**
     * Anhdts doi huong, doi tai khoan de luu
     */
    @Override
    protected boolean checkRedirection(final int saveMode, int codeSaveError) {
        AccountWithDataSet account = mAccountWithDataSet;
        if (mAccountWithDataSet == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                account = mEditorUtils.getDefaultAccount();
            }
        }
        if (account != null && account.type != null && account.type.equals(SimContactsConstants.ACCOUNT_TYPE_SIM)) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getIdStringMessage(codeSaveError)) // Bkav HuyNQN lay text theo logic moi cho ro rang ve loi
                    .setPositiveButton(R.string.speed_dial_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog,
                                                    final int button) {
                                    mStatus = Status.EDITING;
                                    setEnabled(true);
                                }
                            })
                    .setCancelable(false)
                    .show();
            return true;
        } else {
            return false;
        }
    }

    // Bkav HuyNQN sua lai text hien thi loi khong luu duoc lien he vao sim cho ro rang hon
    private int getIdStringMessage(int code){
        if(code == ContactSaveService.CODE_ERROR_MAX_CHARACTER){
            return R.string.toast_change_account_error_max_character;
        }else if(code == ContactSaveService.CODE_ERROR_VN_MAX_CHARACTER){
            return R.string.toast_change_account_error_vn_character;
        }else {
           return R.string.toast_change_account;
        }
    }

    @NonNull
    @Override
    protected Class<?> getContactEditorAccountsChangedActivity() {
        return BkavContactEditorAccountsChangedActivity.class;
    }

    @Override
    protected void createContact(AccountWithDataSet account) {
        account = Config.changeAccountIfLocal(mContext,account);
        super.createContact(account);
    }

    @Override
    protected boolean shouldShowAccountChangedNotification() {
        AccountWithDataSet defaultAccount = mEditorUtils.getDefaultAccount();
        if(Config.isLocalAccount(mContext,defaultAccount)) {
            return false;
        }
        return super.shouldShowAccountChangedNotification();
    }

    //Bkav QuangNDb check xem co phai luu tren sim khong
    private boolean isSaveOnSim() {
        return mState.getFirstWritableRawContact(getContext()).getAccountName() != null &&
                (mState.getFirstWritableRawContact(getContext()).getAccountName().equals(MoreContactUtils.SIM1_TYPE)
                        || mState.getFirstWritableRawContact(getContext()).getAccountName().equals(MoreContactUtils.SIM2_TYPE));
    }
}
