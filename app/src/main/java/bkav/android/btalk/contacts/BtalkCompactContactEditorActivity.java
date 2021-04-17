package bkav.android.btalk.contacts;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toolbar;

import com.android.contacts.activities.ContactEditorBaseActivity;
import com.android.contacts.common.activity.RequestPermissionsActivity;
import com.android.contacts.common.model.RawContactDeltaList;
import com.android.contacts.detail.PhotoSelectionHandler;
import com.android.contacts.editor.CompactContactEditorFragment;
import com.android.contacts.editor.CompactPhotoSelectionFragment;
import com.android.contacts.editor.PhotoSourceDialogFragment;
import com.android.messaging.Factory;
import com.android.messaging.util.ImeUtil;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import bkav.android.btalk.R;
import bkav.android.btalk.contacts.editcontact.BtalkDefaultPhotoPickerFragment;


/**
 * Anhdts class dùng để chỉnh xử thông tin trong phần edit contact.
 */
public class BtalkCompactContactEditorActivity extends ContactEditorBaseActivity implements
        PhotoSourceDialogFragment.Listener, CompactPhotoSelectionFragment.Listener {

    private static final String TAG_COMPACT_EDITOR = "compact_editor";
    private static final String TAG_PHOTO_SELECTION = "photo_selector";
    private static final String TAG_PHOTO_PICKER = "photo_pick";

    private static final String STATE_PHOTO_MODE = "photo_mode";
    private static final String STATE_IS_PHOTO_SELECTION = "is_photo_selection";
    private static final String STATE_ACTION_BAR_TITLE = "action_bar_title";
    private static final String STATE_PHOTO_URI = "photo_uri";
    // Anhdts request code permission camera
    private static final int REQUEST_CODE_CAMERA = 4545;

    private TextView mTitleView;

    private int mHeightScreen;

    private boolean mIsAboveKeyboard = true;

    public TextView getTitleView() {
        return mTitleView;
    }

    /**
     * Displays a PopupWindow with photo edit options.
     */
    public final class CompactPhotoSelectionHandler extends PhotoSelectionHandler {

        /**
         * Receiver of photo edit option callbacks.
         */
        private final class CompactPhotoActionListener extends PhotoActionListener {

            @Override
            public void onRemovePictureChosen() {
                getEditorFragment().removePhoto();
                if (mIsPhotoSelection) {
                    showEditorFragment();
                }
            }

            @Override
            public void onPhotoSelected(Uri uri) throws FileNotFoundException {
                mPhotoUri = uri;
                getEditorFragment().updatePhoto(uri);
                if (mIsPhotoSelection) {
                    showEditorFragment();
                }

                // Re-create the photo handler the next time we need it so that additional photo
                // selections create a new temp file (and don't hit the one that was just added
                // to the cache).
                mPhotoSelectionHandler = null;
            }

            @Override
            public Uri getCurrentPhotoUri() {
                return mPhotoUri;
            }

            @Override
            public void onPhotoSelectionDismissed() {
                if (mIsPhotoSelection) {
                    showEditorFragment();
                }
            }

            @Override
            public void onRemovePhotoPicker(){
                //Bkav ToanNTe fix Danh bạ - BOS 8.7 - Lỗi: Không trở về ảnh đại diện mặc định cho liên hệ khi chạm bỏ tick tại ảnh đang chọn trong giao diện Thêm mới liên hệ
                onRemovePictureChosen();
                //Bkav ToanNTe cập nhật lại ảnh trước đó của liên hệ khi chưa ấn chọn 1 ảnh có sẵn
                ((BtalkCompactContactEditorFragment) mFragment).setIGet(new BtalkCompactContactEditorFragment.IGetUri() {
                    @Override
                    public void getUri(Uri uri) {
                        try {
                            getEditorFragment().updatePhoto(uri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
                ((BtalkCompactContactEditorFragment) mFragment).onRemovePhotoPicker();
            }
        }

        private final CompactPhotoSelectionHandler.CompactPhotoActionListener mPhotoActionListener;
        private boolean mIsPhotoSelection;

        CompactPhotoSelectionHandler(int photoMode, boolean isPhotoSelection) {
            // We pass a null changeAnchorView since we are overriding onClick so that we
            // can show the photo options in a dialog instead of a ListPopupWindow (which would
            // be anchored at changeAnchorView).

            // TODO: empty raw contact delta list
            super(BtalkCompactContactEditorActivity.this, /* changeAnchorView =*/ null, photoMode,
                    /* isDirectoryContact =*/ false, new RawContactDeltaList());
            mPhotoActionListener = new CompactPhotoSelectionHandler.CompactPhotoActionListener();
            mIsPhotoSelection = isPhotoSelection;
        }

        @Override
        public PhotoActionListener getListener() {
            return mPhotoActionListener;
        }

        @Override
        protected void startPhotoActivity(Intent intent, int requestCode, Uri photoUri) {
            mPhotoUri = photoUri;
            startActivityForResult(intent, requestCode);
        }
    }

    private CompactPhotoSelectionFragment mPhotoSelectionFragment;
    private CompactPhotoSelectionHandler mPhotoSelectionHandler;
    private Uri mPhotoUri;
    private int mPhotoMode;
    private boolean mIsPhotoSelection;

    @Override
    public void onCreate(Bundle savedState) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mHeightScreen = displayMetrics.heightPixels;
        Window w = getWindow(); // in Activity's onCreate() for instance
//        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        w.setStatusBarColor(Color.TRANSPARENT);

        w.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);


        super.onCreate(savedState);

        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            return;
        }

        setContentView(R.layout.btalk_contact_editor_activity);
        setActionBar((Toolbar) findViewById(R.id.toolbar));

        mTitleView = (TextView) findViewById(R.id.title_quick_edit);
        if (savedState == null) {
            // Create the editor and photo selection fragments
            //mFragment = new CompactContactEditorFragment();
            //AnhNDd: custom
            createEditorFragment();
            mPhotoSelectionFragment = new CompactPhotoSelectionFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, getEditorFragment(), TAG_COMPACT_EDITOR)
                    .add(R.id.fragment_container, mPhotoSelectionFragment, TAG_PHOTO_SELECTION)
                    .hide(mPhotoSelectionFragment)
                    .commit();
        } else {
            // Restore state
            mPhotoMode = savedState.getInt(STATE_PHOTO_MODE);
            mIsPhotoSelection = savedState.getBoolean(STATE_IS_PHOTO_SELECTION);
            mActionBarTitleResId = savedState.getInt(STATE_ACTION_BAR_TITLE);
            mPhotoUri = Uri.parse(savedState.getString(STATE_PHOTO_URI));

            // Show/hide the editor and photo selection fragments (w/o animations)
            if (getFragmentManager().findFragmentByTag(TAG_COMPACT_EDITOR) instanceof CompactContactEditorFragment){
                mFragment = (CompactContactEditorFragment) getFragmentManager()
                        .findFragmentByTag(TAG_COMPACT_EDITOR);
            }
            mPhotoSelectionFragment = (CompactPhotoSelectionFragment) getFragmentManager()
                    .findFragmentByTag(TAG_PHOTO_SELECTION);
            final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            if (mIsPhotoSelection) {
                fragmentTransaction.hide(getEditorFragment()).show(mPhotoSelectionFragment);
                mTitleView.setText(getResources().getString(R.string.photo_picker_title));
            } else {
                fragmentTransaction.show(getEditorFragment()).hide(mPhotoSelectionFragment);
                mTitleView.setText(getResources().getString(mActionBarTitleResId));
            }
            fragmentTransaction.commit();
        }

        mPhotoSelectionFragment.setListener(this);

        // Load editor data (even if it's hidden)
        final String action = getIntent().getAction();
        final Uri uri = Intent.ACTION_EDIT.equals(action) ? getIntent().getData() : null;

        // Bkav HuyNQN BOS-3006 start
        if(mFragment != null) {
            // Set listeners
            mFragment.setListener(mFragmentListener);
            mFragment.load(action, uri, getIntent().getExtras());
        }
        // Bkav HuyNQN BOS-3006 end

        if (mTitleView != null) {
            if (Intent.ACTION_EDIT.equals(action) || ACTION_EDIT.equals(action)) {
                mActionBarTitleResId = R.string.contact_editor_title_existing_contact;
            } else {
                mActionBarTitleResId = R.string.contact_editor_title_new_contact;
            }

            mTitleView.setText(getResources().getString(mActionBarTitleResId));
            findViewById(R.id.action_home_up).setVisibility(View.VISIBLE);
            findViewById(R.id.action_home_up).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BtalkCompactContactEditorActivity.super.onBackPressed();
                }
            });
        }
        ((BtalkCompactContactEditorFragment) mFragment).setListener(this);
        attachKeyboardListeners();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ((Toolbar) findViewById(R.id.toolbar)).setTitle("");
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_PHOTO_MODE, mPhotoMode);
        outState.putBoolean(STATE_IS_PHOTO_SELECTION, mIsPhotoSelection);
        outState.putInt(STATE_ACTION_BAR_TITLE, mActionBarTitleResId);
        outState.putString(STATE_PHOTO_URI,
                mPhotoUri != null ? mPhotoUri.toString() : Uri.EMPTY.toString());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mPhotoSelectionHandler == null) {
            mPhotoSelectionHandler = (CompactPhotoSelectionHandler) getPhotoSelectionHandler();
        }
        if (mPhotoSelectionHandler.handlePhotoActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (mIsPhotoSelection) {
            mIsPhotoSelection = false;
            showEditorFragment();
        } else if (mPhotoPickerFragment != null && findViewById(R.id.fragment_avatar_picker).isShown()) {
            findViewById(R.id.fragment_avatar_picker).setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Displays photos from all raw contacts, clicking one set it as the super primary photo.
     */
    public void selectPhoto(ArrayList<CompactPhotoSelectionFragment.Photo> photos, int photoMode) {
        mPhotoMode = photoMode;
        mIsPhotoSelection = true;
        mPhotoSelectionFragment.setPhotos(photos, photoMode);
        showPhotoSelectionFragment();
    }

    /**
     * Opens a dialog showing options for the user to change their photo (take, choose, or remove
     * photo).
     */
    public void changePhoto(int photoMode) {
        mPhotoMode = photoMode;
        mIsPhotoSelection = false;
        PhotoSourceDialogFragment.show(this, mPhotoMode);
    }

    private void showPhotoSelectionFragment() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .hide(getEditorFragment())
                .show(mPhotoSelectionFragment)
                .commit();
        mTitleView.setText(getResources().getString(R.string.photo_picker_title));
    }

    private void showEditorFragment() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .hide(mPhotoSelectionFragment)
                .show((CompactContactEditorFragment) mFragment)
                .commit();
        mTitleView.setText(getResources().getString(mActionBarTitleResId));
        mIsPhotoSelection = false;
    }

    @Override
    public void onRemovePictureChosen() {
        getPhotoSelectionHandler().getListener().onRemovePictureChosen();
    }

    @Override
    public void onTakePhotoChosen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
        } else {
            getPhotoSelectionHandler().getListener().onTakePhotoChosen();
        }
    }

    // Anhdts check neu da cap quyen thi mo may anh len luon
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CAMERA) {
            onTakePhotoChosen();
        }
    }

    @Override
    public void onPickFromGalleryChosen() {
        getPhotoSelectionHandler().getListener().onPickFromGalleryChosen();
    }

    @Override
    public void onChooseFromExistImage() {
        mIsShowKeyboard = false;
        //HienDTk: an ban phim truoc khi show photo picker
        hideKeyboard();
        showFragmentPhotoPicker();
    }

    private BtalkDefaultPhotoPickerFragment mPhotoPickerFragment;

    private void showFragmentPhotoPicker() {
        if (mPhotoPickerFragment == null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            mPhotoPickerFragment = new BtalkDefaultPhotoPickerFragment();
            View view = findViewById(R.id.fragment_avatar_picker);
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = metrics.heightPixels - metrics.widthPixels;
            view.setLayoutParams(params);
            view.bringToFront();
            ft.add(R.id.fragment_avatar_picker, mPhotoPickerFragment, TAG_PHOTO_PICKER)
                    .commit();
            mPhotoPickerFragment.setPhotoHandler(getPhotoSelectionHandler(), mPhotoUri);
            ft.show(mPhotoPickerFragment);
        } else {
            findViewById(R.id.fragment_avatar_picker).setVisibility(View.VISIBLE);
            findViewById(R.id.fragment_avatar_picker).bringToFront();
        }
        ((BtalkCompactContactEditorFragment) mFragment).showPhotoPickerFragment();
    }

    @Override
    public void onPhotoSelected(CompactPhotoSelectionFragment.Photo photo) {
        getEditorFragment().setPrimaryPhoto(photo);
        showEditorFragment();
    }

    private PhotoSelectionHandler getPhotoSelectionHandler() {
        if (mPhotoSelectionHandler == null) {
            mPhotoSelectionHandler = new CompactPhotoSelectionHandler(
                    mPhotoMode, mIsPhotoSelection);
        }
        return mPhotoSelectionHandler;
    }

    private BtalkCompactContactEditorFragment getEditorFragment() {
        return (BtalkCompactContactEditorFragment) mFragment;
    }

    //=======================BKAV====================

    //AnhNDd: tạo fragment bằng fragment của mình.
    public void createEditorFragment() {
        mFragment = new BtalkCompactContactEditorFragment();
    }

//    @Override
//    public void onCreate(Bundle savedState) {
//        super.onCreate(savedState);
//        // Anhdts doi mau icon statusbar ve mau den
//        if (OsUtil.isAtLeastICS_MR1()) {
//            int flags = findViewById(R.id.fragment_container).getSystemUiVisibility();
//            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
//            findViewById(R.id.fragment_container).setSystemUiVisibility(flags);
//        }
//
//    }

    private boolean mIsFirstRun = true;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mFragment != null && mIsFirstRun) {
            mIsFirstRun = false;
            ((BtalkCompactContactEditorFragment) mFragment).focusEditText();
        }
    }

    private boolean mIsShowKeyboard = false;

    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            View view = rootLayout.getRootView();
            final Rect rect = new Rect();
            view.getWindowVisibleDisplayFrame(rect);

            ViewGroup.LayoutParams params = rootLayout.getLayoutParams();
            if (params.height < 0) {
                params.height = rect.bottom;
                rootLayout.setLayoutParams(params);
                return;
            }
            if (rect.bottom > 0 && (params.height - rect.bottom != 0)) {
                if (params.height != rect.bottom) {
                    if (params.height > rect.bottom && !mIsShowKeyboard && !mIsAboveKeyboard) {
                        mIsShowKeyboard = true;
                        ((BtalkCompactContactEditorFragment) mFragment).
                                scrollWhenKeyboardShow();
                    } else if (params.height > rect.bottom) {
                        mIsShowKeyboard = true;
                        params.height = rect.bottom;
                        rootLayout.setLayoutParams(params);
                        return;
                    } else if (params.height < rect.bottom &&
                            mHeightScreen == rect.bottom) {
                        mIsShowKeyboard = false;
                    } else {
                        return;
                    }
                    params.height = rect.bottom;
                    rootLayout.setLayoutParams(params);
                }
//                params.height = rect.bottom;
//                rootLayout.setLayoutParams(params);
            }
        }
    };

    // Anhdts chieu cao uoc tinh cua ban phim
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mIsAboveKeyboard = ev.getY() < mHeightScreen - 1000 && ev.getAction() == MotionEvent.ACTION_UP;
        View view = this.getCurrentFocus();
        if (view != null && mIsShowKeyboard) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && ev.getY() > mHeightScreen - 1000) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                mIsShowKeyboard = false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean keyboardListenersAttached = false;
    private ViewGroup rootLayout;

    protected void attachKeyboardListeners() {
        if (keyboardListenersAttached) {
            return;
        }

        rootLayout = (ViewGroup) findViewById(R.id.rootLayout);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);

        keyboardListenersAttached = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (keyboardListenersAttached) {
            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
        }
    }

    public void onScrollShrink() {
        if (mPhotoPickerFragment != null && mPhotoPickerFragment.isVisible()) {
            findViewById(R.id.fragment_avatar_picker).setVisibility(View.GONE);
        }
    }

    /**
     * HienDTk: an ban phim
     */
    private void hideKeyboard(){
        ImeUtil.hideSoftInput(Factory.get().getApplicationContext(), mTitleView);
    }
}
