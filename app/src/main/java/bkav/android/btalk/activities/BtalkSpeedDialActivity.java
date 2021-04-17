/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bkav.android.btalk.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.WallpaperManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Trace;
import android.provider.ContactsContract;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v13.app.FragmentCompat;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.contacts.common.CallUtil;
import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.list.ContactTileView;
import com.android.contacts.common.util.PermissionsUtil;
import com.android.dialer.TransactionSafeActivity;
import com.android.dialer.list.ContentChangedFilter;
import com.android.dialer.list.OnDragDropListener;
import com.android.dialer.list.PhoneFavoriteListView;
import com.android.dialer.list.PhoneFavoriteSquareTileView;
import com.android.dialer.list.PhoneFavoritesTileAdapter;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.IntentUtil;
import com.android.dialer.util.TelecomUtil;
import com.android.dialer.widget.EmptyContentView;
import com.android.incallui.Call.LogState;
import com.android.messaging.util.PhoneUtils;

import java.util.ArrayList;

import bkav.android.blur.activity.WallpaperBlurCompat;
import bkav.android.btalk.BtalkDialogChooseMutilNumber;
import bkav.android.btalk.R;
import bkav.android.btalk.esim.dialog_choose_sim.DialogChooseSimFragment;
import bkav.android.btalk.messaging.ui.BtalkUIIntentsImpl;
import bkav.android.btalk.mutilsim.SimUltil;
import bkav.android.btalk.speeddial.BtalkPhoneFavoritesTileAdapter;
import bkav.android.btalk.speeddial.BtalkSpeedDialSearchFragment;
import bkav.android.btalk.speeddial.HomeWatcher;
import bkav.android.btalk.suggestmagic.SuggestLoaderManager;
import bkav.android.btalk.suggestmagic.SuggestPopup;

import static android.Manifest.permission.READ_CONTACTS;
import static com.android.contacts.common.ContactTileLoaderFactory.COLUMNS_PHONE_ONLY;

/**
 * Anhdts
 * activity hien thi danh sach yeu thich
 */
public class BtalkSpeedDialActivity extends TransactionSafeActivity implements OnItemClickListener,
        PhoneFavoritesTileAdapter.OnDataSetChangedForAnimationListener,
        EmptyContentView.OnEmptyViewActionButtonClickedListener,
        FragmentCompat.OnRequestPermissionsResultCallback,
        OnDragDropListener, WallpaperBlurCompat.ChangeWallPaperListener {

    private static final int READ_CONTACTS_PERMISSION_REQUEST_CODE = 1;

    // Bkav TienNAb - Fix bug BOS-3851 - Start
    // Bkav TienNAb: uri rieng minh tu tao o duoi rom de query cac lien he thuong xuyen lien lac
    private static final Uri CONTENT_FREQUENCE_URI = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, "frequence");
    // Bkav TienNAb - Fix bug BOS-3851 - End

    /**
     * By default, the animation code assumes that all items in a list view are of the same height
     * when animating new list items into view (e.g. from the bottom of the screen into view).
     * This can cause incorrect translation offsets when a item that is larger or smaller than
     * other list item is removed from the list. This key is used to provide the actual height
     * of the removed object so that the actual translation appears correct to the user.
     */
    private static final long KEY_REMOVED_ITEM_HEIGHT = Long.MAX_VALUE;

    private static final String TAG = "BtalkSpeedDialActivity";
    private static final boolean DEBUG = false;

    private int mAnimationDuration;

    /**
     * Used with LoaderManager.
     */
    private static int LOADER_ID_CONTACT_TILE = 1;
    // ANhdts lang nghe home press
    private HomeWatcher mHomeWatcher;

    @Override
    public void onDragStarted(int x, int y, PhoneFavoriteSquareTileView view) {
        /* Anhdts TODO Make some thing */
    }

    @Override
    public void onDragHovered(int x, int y, PhoneFavoriteSquareTileView view) {

    }

    @Override
    public void onDragFinished(int x, int y) {
        /* Anhdts TODO Make some thing */
    }

    @Override
    public void onDroppedOnRemove() {

    }

    private class ContactTileLoaderListener implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public CursorLoader onCreateLoader(int id, Bundle args) {
            if (DEBUG) Log.d(TAG, "ContactTileLoaderListener#onCreateLoader.");
            // return ContactTileLoaderFactory.createStrequentPhoneOnlyLoader(BtalkSpeedDialActivity.this);
            // Bkav TienNAb - Fix bug BOS-3851 - Start
            // Bkav TienNAb: su dung uri rieng minh tu custom
            // Bkav TienNAb - Fix bug BOS-4247 - Start
            // Android 9 van dung uri cu
            Uri uri;
            if (BtalkActivity.isAndroidQ()){
                uri = CONTENT_FREQUENCE_URI.buildUpon()
                        .appendQueryParameter(ContactsContract.STREQUENT_PHONE_ONLY, "true").build();
            } else {
                uri = ContactsContract.Contacts.CONTENT_STREQUENT_URI.buildUpon()
                        .appendQueryParameter(ContactsContract.STREQUENT_PHONE_ONLY, "true").build();
            }
            // Bkav TienNAb - Fix bug BOS-4247 - End
            // Bkav TienNAb - Fix bug BOS-3851 - End
            uri = uri.buildUpon().appendQueryParameter("limit", "8").build();
            return new CursorLoader(BtalkSpeedDialActivity.this, uri, COLUMNS_PHONE_ONLY, null, null, ContactsContract.Contacts.STARRED + " limit 8");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (DEBUG) Log.d(TAG, "ContactTileLoaderListener#onLoadFinished");
            mContactTileAdapter.setContactCursor(data);
            setEmptyViewVisibility(mContactTileAdapter.getCount() == 0);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if (DEBUG) Log.d(TAG, "ContactTileLoaderListener#onLoaderReset. ");
        }
    }

    private class ContactTileAdapterListener implements ContactTileView.Listener {
        @Override
        public void onContactSelected(Uri contactUri, Rect targetRect) {
            onPickDataUri(contactUri
                    /* isVideoCall */);
        }

        @Override
        public void onCallNumberDirectly(String phoneNumber) {
            onPickPhoneNumber(phoneNumber,
                    false /* isVideoCall */, LogState.INITIATION_SPEED_DIAL);
        }

        // Anhdts get width
        @Override
        public int getApproximateTileWidth() {
            return BtalkSpeedDialActivity.this.getWindow().getDecorView().getHeight();
        }

        // Anhdts su kien mo giao dien chi tiet
        @Override
        public void onActionShowDetail() {
        }


    }

    /**
     * Anhdts
     */
    public void onPickDataUri(Uri dataUri) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        BtalkDialogChooseMutilNumber dialogChooseMutilNumber = new BtalkDialogChooseMutilNumber();
        dialogChooseMutilNumber.startInteraction(this, dataUri);

        dialogChooseMutilNumber.show(ft, BtalkDialogChooseMutilNumber.DIALOG_TAG);

//        PhoneNumberInteraction.startInteractionForPhoneCall(
//                this, dataUri, isVideoCall, callInitiationType);
    }

    /**
     * Anhdts
     */
    public void onPickPhoneNumber(String phoneNumber, boolean isVideoCall, int callInitiationType) {
        if (phoneNumber == null) {
            // Invalid phone number, but let the call go through so that InCallUI can show
            // an error message.
            phoneNumber = "";
        }
        if (getResources().getBoolean(R.bool.config_regional_number_patterns_video_call) &&
                !CallUtil.isVideoCallNumValid(phoneNumber) &&
                isVideoCall && (CallUtil.isVideoEnabled(this))) {
            Toast.makeText(this, R.string.toast_make_video_call_failed, Toast.LENGTH_LONG).show();
            return;
        }

        final Intent intent = new IntentUtil.CallIntentBuilder(phoneNumber)
                .setIsVideoCall(isVideoCall)
                .setCallInitiationType(callInitiationType)
                .build();

//        DialerUtils.startActivityWithErrorToast(this, intent);
        PhoneAccountHandle handleDefault = TelecomUtil.getDefaultOutgoingPhoneAccount(getApplicationContext(), PhoneAccount.SCHEME_TEL);
        int slotDefault = SimUltil.getSlotSimByAccount(getApplicationContext(), handleDefault);
        if(slotDefault == -1 && !(PhoneUtils.getDefault().getActiveSubscriptionCount() <= 1)) { // Bkav HuyNQN neu multisim va ko co sim mac dinh hien listsim
            DialogChooseSimFragment mDialogChooseSimFragment = DialogChooseSimFragment.newInstance(phoneNumber);
            mDialogChooseSimFragment.show(getFragmentManager(), "chooseSim");
        }
        else {
            DialerUtils.sendBroadcastCount(getApplicationContext(), DialerUtils.convertTabSeletedForCall(BtalkActivity.TAB_SELECT), 0);
            DialerUtils.startActivityWithErrorToast(this, intent);
        }
    }

    private class ScrollListener implements ListView.OnScrollListener {
        @Override
        public void onScroll(AbsListView view,
                             int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // Anhdts
            // mActivityScrollListener.onListFragmentScrollStateChange(scrollState);
            /*TODO make some thing with this*/
        }

    }

    private BtalkPhoneFavoritesTileAdapter mContactTileAdapter;

    private PhoneFavoriteListView mListView;

    private View mContactTileFrame;

    private final LongSparseArray<Integer> mItemIdTopMap = new LongSparseArray<>();
    private final LongSparseArray<Integer> mItemIdLeftMap = new LongSparseArray<>();

    /**
     * Layout used when there are no favorites.
     */
    private EmptyContentView mEmptyView;

    private final ContactTileView.Listener mContactTileAdapterListener =
            new ContactTileAdapterListener();
    private final LoaderManager.LoaderCallbacks<Cursor> mContactTileLoaderListener =
            new ContactTileLoaderListener();
    private final ScrollListener mScrollListener = new ScrollListener();

    @Override
    public void onCreate(Bundle savedState) {
        getWindow().getAttributes().windowAnimations = R.style.CustomAnimationActivity;
        // Anhdts set full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedState);

        // Anhdts blur view
        WallpaperBlurCompat blurCompat = WallpaperBlurCompat.getInstance(getApplicationContext());

        // Anhdts dang ki lang nghe doi man hinh
        blurCompat.addOnChangeWallpaperListener(this);

        setContentView(R.layout.btalk_speed_dial_fragment);

        // Bkav TienNAb: fix loi crash app
        mRenderScript = RenderScript.create(getApplicationContext());
        mScriptIntrinsicBlur = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
        // Anhdts setbackground wallpaper
        if (WallpaperBlurCompat.getInstance(getApplicationContext()).isConfigBkav()) {
            findViewById(R.id.root_view).setBackground(new BitmapDrawable(getResources(), blurCompat.getWallpaperBlur()));
        } else {
            setBackgroundBlur(25);
        }

        mListView = (PhoneFavoriteListView) findViewById(R.id.contact_tile_list);
        mListView.setOnItemClickListener(this);
        mListView.setVerticalScrollBarEnabled(false);
        mListView.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);
        mListView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);

        final ImageView dragShadowOverlay =
                (ImageView) findViewById(R.id.contact_tile_drag_shadow_overlay);
        mListView.setDragShadowOverlay(dragShadowOverlay);

        mEmptyView = (EmptyContentView) findViewById(R.id.empty_list_view);
        mEmptyView.setImage(R.drawable.empty_speed_dial);
        mEmptyView.setActionClickedListener(this);

        mContactTileFrame = findViewById(R.id.contact_tile_frame);

        final LayoutAnimationController controller = new LayoutAnimationController(
                AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        controller.setDelay(0);
        mListView.setLayoutAnimation(controller);

        mListView.setOnScrollListener(mScrollListener);
        mListView.setFastScrollEnabled(false);
        mListView.setFastScrollAlwaysVisible(false);

        //prevent content changes of the list from firing accessibility events.
        // Anhdts chua hieu dung lam gi
        mListView.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_NONE);
        ContentChangedFilter.addToParent(mListView);

        Trace.endSection();
        if (DEBUG) Log.d(TAG, "onCreate()");
        Trace.beginSection(TAG + " onCreate");

        mAnimationDuration = getResources().getInteger(R.integer.fade_duration);
        Trace.endSection();
        // Construct two base adapters which will become part of PhoneFavoriteMergedAdapter.
        // We don't construct the resultant adapter at this moment since it requires LayoutInflater
        // that will be available on onCreateView().
        mContactTileAdapter = new BtalkPhoneFavoritesTileAdapter(getApplicationContext(), mContactTileAdapterListener,
                this);
        mContactTileAdapter.setPhotoLoader(ContactPhotoManager.getInstance(getApplicationContext()));
        mListView.setAdapter(mContactTileAdapter);
        mListView.getDragDropController().addOnDragDropListener(mContactTileAdapter);

        // Anhdts goi vao giao dien quay so
        findViewById(R.id.container_floating_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                animationView(true, 0, false);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
                /*Intent intent = new Intent("btalk.intent.action.DIAL_BKAV");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.setPackage(getPackageName());
                startActivity(intent);*/

                // Bkav TienNAb: click vao icon ban phim thi quay tro ve giao dien ban phim
                onBackPressed();
            }
//                }, 200);
//            }
        });

        mContentViewSpeed = findViewById(R.id.content_grid_view_speed);

        // Bkav HuyNQN phu lop background white_opacity
        mContentViewSpeed.setBackgroundColor(getResources().getColor(R.color.btalk_white_opacity_bg));

        mContentViewSearch = findViewById(R.id.content_search);

        // Anhdts inflate fragment search
        addFragmentSearch();
        mSmartSuggestLoaderManage = new SuggestLoaderManager(this);
//        if (mContentViewSpeed != null && !mContentViewSpeed.isShown()) {
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    initAnimationBlur();
//                }
//            }, 100);
//        }
        registerHomeListener();
    }

    @Override
    public void onResume() {
        Trace.beginSection(TAG + " onResume");

        super.onResume();
        if (mContactTileAdapter != null) {
            mContactTileAdapter.refreshContactsPreferences();
        }
        if (PermissionsUtil.hasContactsPermissions(this)) {
            if (getLoaderManager().getLoader(LOADER_ID_CONTACT_TILE) == null) {
                getLoaderManager().initLoader(LOADER_ID_CONTACT_TILE, null,
                        mContactTileLoaderListener);

            } else {
                getLoaderManager().getLoader(LOADER_ID_CONTACT_TILE).forceLoad();
            }

            mEmptyView.setDescription(R.string.speed_dial_empty);
            mEmptyView.setActionLabel(R.string.speed_dial_empty_add_favorite_action);
        } else {
            mEmptyView.setDescription(R.string.permission_no_speeddial);
            mEmptyView.setActionLabel(R.string.permission_single_turn_on);
        }
        Trace.endSection();

        // Anhdts hide popup search
        if (mSmartSuggestLoaderManage != null) {
            mSmartSuggestLoaderManage.hideViewSuggest();
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            // Anhdts dim view ra
            if (!mIsShowSearch) {
                if (mContentViewSpeed != null && !mContentViewSpeed.isShown()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animationView(false, 1, false);
                        }
                    }, 200);
                }
            }
    }

    /* package */ void setEmptyViewVisibility(final boolean visible) {
        final int previousVisibility = mEmptyView.getVisibility();
        final int emptyViewVisibility = visible ? View.VISIBLE : View.GONE;
        final int listViewVisibility = visible ? View.GONE : View.VISIBLE;

        if (previousVisibility != emptyViewVisibility) {
            // Anhdts convert relative layout
            final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mContactTileFrame
                    .getLayoutParams();
            params.height = visible ? LayoutParams.WRAP_CONTENT : LayoutParams.MATCH_PARENT;
            mContactTileFrame.setLayoutParams(params);
            mEmptyView.setVisibility(emptyViewVisibility);
            mListView.setVisibility(listViewVisibility);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            mListView.getDragDropController().addOnDragDropListener(this);
            setDragDropController();
        } catch (ClassCastException e) {
            throw new ClassCastException(toString()
                    + " must implement OnDragDropListener and HostInterface");
        }

        // Use initLoader() instead of restartLoader() to refraining unnecessary reload.
        // This method call implicitly assures ContactTileLoaderListener's onLoadFinished() will
        // be called, on which we'll check if "all" contacts should be reloaded again or not.
        if (PermissionsUtil.hasContactsPermissions(this)) {
            getLoaderManager().initLoader(LOADER_ID_CONTACT_TILE, null, mContactTileLoaderListener);
        } else {
            setEmptyViewVisibility(true);
        }
    }

    public void setDragDropController() {
    }

    /**
     * {@inheritDoc}
     *
     * This is only effective for elements provided by {@link #mContactTileAdapter}.
     * {@link #mContactTileAdapter} has its own logic for click events.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final int contactTileAdapterCount = mContactTileAdapter.getCount();
        if (position <= contactTileAdapterCount) {
            Log.e(TAG, "onItemClick() event for unexpected position. "
                    + "The position " + position + " is before \"all\" section. Ignored.");
        }
    }

    /**
     * Cache the current view offsets into memory. Once a relayout of views in the ListView
     * has happened due to a dataset change, the cached offsets are used to create animations
     * that slide views from their previous positions to their new ones, to give the appearance
     * that the views are sliding into their new positions.
     */
    private void saveOffsets(int removedItemHeight) {
        final int firstVisiblePosition = mListView.getFirstVisiblePosition();
        if (DEBUG) {
            Log.d(TAG, "Child count : " + mListView.getChildCount());
        }
        for (int i = 0; i < mListView.getChildCount(); i++) {
            final View child = mListView.getChildAt(i);
            final int position = firstVisiblePosition + i;
            // Since we are getting the position from mListView and then querying
            // mContactTileAdapter, its very possible that things are out of sync
            // and we might index out of bounds.  Let's make sure that this doesn't happen.
            if (!mContactTileAdapter.isIndexInBound(position)) {
                continue;
            }
            final long itemId = mContactTileAdapter.getItemId(position);
            if (DEBUG) {
                Log.d(TAG, "Saving itemId: " + itemId + " for listview child " + i + " Top: "
                        + child.getTop());
            }
            mItemIdTopMap.put(itemId, child.getTop());
            mItemIdLeftMap.put(itemId, child.getLeft());
        }
        mItemIdTopMap.put(KEY_REMOVED_ITEM_HEIGHT, removedItemHeight);
    }

    /*
     * Performs animations for the gridView
     */
    private void animateGridView(final long... idsInPlace) {
        if (mItemIdTopMap.size() == 0) {
            // Don't do animations if the database is being queried for the first time and
            // the previous item offsets have not been cached, or the user hasn't done anything
            // (dragging, swiping etc) that requires an animation.
            return;
        }

        final ViewTreeObserver observer = mListView.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @SuppressWarnings("unchecked")
            @Override
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                final int firstVisiblePosition = mListView.getFirstVisiblePosition();
                final AnimatorSet animSet = new AnimatorSet();
                final ArrayList<Animator> animators = new ArrayList<>();
                for (int i = 0; i < mListView.getChildCount(); i++) {
                    final View child = mListView.getChildAt(i);
                    int position = firstVisiblePosition + i;

                    // Since we are getting the position from mListView and then querying
                    // mContactTileAdapter, its very possible that things are out of sync
                    // and we might index out of bounds.  Let's make sure that this doesn't happen.
                    if (!mContactTileAdapter.isIndexInBound(position)) {
                        continue;
                    }

                    final long itemId = mContactTileAdapter.getItemId(position);

                    if (containsId(idsInPlace, itemId)) {
                        animators.add(ObjectAnimator.ofFloat(
                                child, "alpha", 0.0f, 1.0f));
                        break;
                    } else {
                        Integer startTop = mItemIdTopMap.get(itemId);
                        Integer startLeft = mItemIdLeftMap.get(itemId);
                        final int top = child.getTop();
                        final int left = child.getLeft();
                        int deltaX;
                        int deltaY;

                        if (startLeft != null) {
                            if (startLeft != left) {
                                deltaX = startLeft - left;
                                animators.add(ObjectAnimator.ofFloat(
                                        child, "translationX", deltaX, 0.0f));
                            }
                        }

                        if (startTop != null) {
                            if (startTop != top) {
                                deltaY = startTop - top;
                                animators.add(ObjectAnimator.ofFloat(
                                        child, "translationY", deltaY, 0.0f));
                            }
                        }

                        if (DEBUG) {
                            Log.d(TAG, "Found itemId: " + itemId + " for listview child " + i +
                                    " Top: " + top +
                                    " Delta: " + deltaY);
                        }
                    }
                }

                if (animators.size() > 0) {
                    animSet.setDuration(mAnimationDuration).playTogether(animators);
                    animSet.start();
                }

                mItemIdTopMap.clear();
                mItemIdLeftMap.clear();
                return true;
            }
        });
    }

    private boolean containsId(long[] ids, long target) {
        // Linear search on array is fine because this is typically only 0-1 elements long
        for (long id : ids) {
            if (id == target) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDataSetChangedForAnimation(long... idsInPlace) {
        animateGridView(idsInPlace);
    }

    @Override
    public void cacheOffsetsForDatasetChange() {
        saveOffsets(0);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onEmptyViewActionButtonClicked() {
        if (!PermissionsUtil.hasPermission(this, READ_CONTACTS)) {
            requestPermissions(new String[]{READ_CONTACTS},
                    READ_CONTACTS_PERMISSION_REQUEST_CODE);
        } else {
            try {
                // Bkav TienNAb: sua lai logic them lien he yeu thich moi
//                Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
//                intent.putExtra(ContactEditorFragment.INTENT_EXTRA_NEW_LOCAL_PROFILE, true);
//                ImplicitIntentsUtil.startActivityInApp(this, intent);
                BtalkUIIntentsImpl.get().launchAddFavoriteContactListActivity(getApplicationContext());
            } catch (Exception e) {
                //#TODO make something
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == READ_CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 1 && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                PermissionsUtil.notifyPermissionGranted(this, READ_CONTACTS);
            }
        }
    }

    // Anhdts doi man hinh
    @Override
    public void onChangeWallpaper() {
        setBackgroundBlur(getResources().getInteger(R.integer.btalk_blur_radius));
    }

    /**
     * Anhdts xoa listener change background
     */
    @Override
    public void onDestroy() {
        WallpaperBlurCompat.getInstance(getApplicationContext()).removeOnChangeWallpaperListener(this);
        mHomeWatcher.stopWatch();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            onStop();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Dang ky su kien nang nghe nut home de stop app, khong luc vao btalk
     * no lai hien giao di en nay truoc
     */
    private void registerHomeListener() {
        mHomeWatcher = new HomeWatcher(getApplicationContext());
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                onStop();
                finish();
            }

            @Override
            public void onHomeLongPressed() {
            }
        });
        mHomeWatcher.startWatch();
    }

    // Anhdts search fragment

    private SuggestLoaderManager mSmartSuggestLoaderManage;

    private static final String TAG_SEARCH_FRAGMENT = "search_fragment_speed";

    private BtalkSpeedDialSearchFragment mSearchFragment;

    private View mContentViewSpeed;

    private View mContentViewSearch;

    /**
     * Anhdts add fragment search
     */
    private void addFragmentSearch() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (fragmentManager.findFragmentByTag(TAG_SEARCH_FRAGMENT) != null) {
            mSearchFragment = (BtalkSpeedDialSearchFragment) fragmentManager.findFragmentByTag(TAG_SEARCH_FRAGMENT);
        } else {
            mSearchFragment = new BtalkSpeedDialSearchFragment();
            ft.replace(R.id.content_search, mSearchFragment, TAG_SEARCH_FRAGMENT).addToBackStack(null);
            ft.show(mSearchFragment);
            ft.commit();
        }
    }

    private static final int DISTANCE_FLY_VIEW = 900;

    private boolean mStateFly = false;

    private float mPosDown;

    private boolean mIsShowSearch = false;

    private long mTimeDown;

    private boolean mIsActionDrag = false;

    private boolean mIsLockAnimation = false;

    private static final long TIME_DURATION_ANIMATION = 300;

    private static final float VELOCITY_ACCEPT_FLY = 2f;

    private static final float PIVOT_BOUND = 0.5f;

    private static final float PIVOT_START_ANIM_SEARCH_VIEW = 0.4f;

    private static final float PIVOT_START_ANIM_SPEED_VIEW = 0.6f;

    private MotionEvent mEventDown;

    private boolean mIsActionDownStart = false;

    private boolean mIsKeepTouch = false;

    /**
     * Anhdts custom dispatch touch event
     */
    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        if (mIsLockAnimation) {
            return true;
        }
        if (mIsShowSearch) {
            if (mSmartSuggestLoaderManage != null) {
                mSmartSuggestLoaderManage.hideViewSuggest();
            }
            return super.dispatchTouchEvent(ev);
        }
        if (mIsActionDownStart && ev.getAction() != MotionEvent.ACTION_DOWN) {
            mIsActionDownStart = false;
        }

        if (mIsActionDrag && ev.getAction() != MotionEvent.ACTION_DOWN) {
            return !(!mIsKeepTouch || ev.getDownTime() - mTimeDown > 400) || super.dispatchTouchEvent(ev);
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mPosDown = ev.getY();
            mTimeDown = System.currentTimeMillis();
            mIsActionDrag = false;
            mEventDown = ev;
            mIsActionDownStart = true;
            mIsKeepTouch = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mIsActionDownStart) {
                        mIsActionDrag = true;
                        mIsKeepTouch = true;
                        dispatchEvent(mEventDown);
                        mEventDown.setAction(MotionEvent.ACTION_MOVE);
                        dispatchEvent(mEventDown);
                    }
                }
            }, 200);
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (!mStateFly && (Math.abs(ev.getY() - mPosDown) < 10
                    && (System.currentTimeMillis() - mTimeDown) > 200) || ev.getY() == mPosDown) {
                mIsActionDrag = true;
                ev.setAction(MotionEvent.ACTION_DOWN);
                super.dispatchTouchEvent(mEventDown);
                ev.setAction(MotionEvent.ACTION_MOVE);
                return super.dispatchTouchEvent(ev);
            }

            float posTouch = ev.getY();
            if (!mStateFly) {
                mStateFly = true;
            }
            if (mPosDown > posTouch) {
                mContentViewSearch.setVisibility(View.VISIBLE);
                float rate = (mPosDown - posTouch) / DISTANCE_FLY_VIEW;
                rate = rate > 1 ? 1 : rate;
                updateView(rate);
            }
        } else if (mStateFly) {
            if (ev.getAction() == MotionEvent.ACTION_UP) {
                mStateFly = false;
                float posTouch = ev.getY();
                float rate = (mPosDown - posTouch) / DISTANCE_FLY_VIEW;
                mIsShowSearch = ((mPosDown - posTouch) / (System.currentTimeMillis() - mTimeDown))
                        > VELOCITY_ACCEPT_FLY || rate > PIVOT_BOUND;
                rate = rate > 1 ? 1 : (rate < 0 ? 0 : rate);
                animationView(mIsShowSearch, rate, true);
            }
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (Math.abs(ev.getY() - mPosDown) < 10 && (System.currentTimeMillis() - mTimeDown) < 200) {
                ev.setAction(MotionEvent.ACTION_DOWN);
                super.dispatchTouchEvent(ev);
                ev.setAction(MotionEvent.ACTION_UP);
                return super.dispatchTouchEvent(ev);
            }
        }
        return true;
    }

    /**
     * Anhdts update alpha va scale zoom theo tay vuot
     */
    private void updateView(float rate) {
        float rateSpeed = rate > PIVOT_START_ANIM_SPEED_VIEW ? 0 :
                (1 - rate / PIVOT_START_ANIM_SPEED_VIEW);
        float rateSearch = rate < PIVOT_START_ANIM_SEARCH_VIEW ? 0 :
                ((rate - PIVOT_START_ANIM_SEARCH_VIEW) / PIVOT_START_ANIM_SPEED_VIEW);

        float rateScale = PIVOT_START_ANIM_SEARCH_VIEW * rateSpeed + PIVOT_START_ANIM_SPEED_VIEW;

        if (mListView != null) {
            ViewGroup viewGroup = mListView;
            for (int i = 0; i < viewGroup.getChildCount(); ++i) {
                View child = viewGroup.getChildAt(i);
                if (child instanceof PhoneFavoriteSquareTileView) {
                    child.setScaleY(rateScale);
                    child.setScaleX(rateScale);
                }
            }
        }

        mContentViewSpeed.setAlpha(rateSpeed);
        mContentViewSearch.setAlpha(rateSearch);
    }

    /**
     * Anhdts animation view; alpha view va zoom item gridview
     * @param isModeSearch chuyen sang che do search vaf thuong
     * @param rate toa do bay cua tay khi tha ra (0 va 1 trong che do animation tu thoi diem bat dau)
     * @param isChangeMode co phai chuyen che do search mode khong hay la animation start hoac exist
     */
    private void animationView(final boolean isModeSearch, final float rate, final boolean isChangeMode) {
        ArrayList<Animator> arrayListObjectAnimators = new ArrayList<>();
        long duration = (long) (TIME_DURATION_ANIMATION * (isModeSearch ? ((1 - rate) / PIVOT_BOUND) : rate / PIVOT_BOUND));
        if (duration > TIME_DURATION_ANIMATION) {
            if (!isChangeMode && isModeSearch) {
                duration = 200;
            } else {
                duration = TIME_DURATION_ANIMATION;
            }
        }

        final float rateSpeed = rate > PIVOT_START_ANIM_SPEED_VIEW ? 0 :
                (1 - rate / PIVOT_START_ANIM_SPEED_VIEW);
        mContentViewSpeed.setAlpha(rateSpeed);
        mContentViewSpeed.setVisibility(View.VISIBLE);
        ObjectAnimator animSpeed =
                ObjectAnimator.ofFloat(mContentViewSpeed, "alpha", isModeSearch ? 0 : 1);
        arrayListObjectAnimators.add(animSpeed);

        if (isChangeMode) {
            float rateSearch = rate < PIVOT_START_ANIM_SEARCH_VIEW ? 0 :
                    ((rate - PIVOT_START_ANIM_SEARCH_VIEW) / PIVOT_START_ANIM_SPEED_VIEW);
            mContentViewSearch.setAlpha(rateSearch);
            mContentViewSearch.setVisibility(View.VISIBLE);
            final ObjectAnimator animSearch =
                    ObjectAnimator.ofFloat(mContentViewSearch, "alpha", isModeSearch ? 1 : 0);
            arrayListObjectAnimators.add(animSearch);
        }

        if (mListView != null) {
            ViewGroup viewGroup = mListView;
            for (int i = 0; i < viewGroup.getChildCount(); ++i) {
                View child = viewGroup.getChildAt(i);
                if (child instanceof PhoneFavoriteSquareTileView) {
                    float scaleLast = isModeSearch ? PIVOT_START_ANIM_SPEED_VIEW : 1f;

                    float rateScale = PIVOT_START_ANIM_SEARCH_VIEW * rateSpeed + PIVOT_START_ANIM_SPEED_VIEW;
                    child.setScaleY(rateScale);
                    child.setScaleX(rateScale);

                    ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(child,
                            PropertyValuesHolder.ofFloat("scaleX", scaleLast),
                            PropertyValuesHolder.ofFloat("scaleY", scaleLast));
                    arrayListObjectAnimators.add(scale);
                }
            }
        }

//        if (isStart) {
//            WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
//            final Bitmap bitmap = drawableToBitmap(wallpaperManager.getDrawable());
//            ValueAnimator blurAnimator = ValueAnimator.ofInt(MIN_RADIUS, 4);
//            blurAnimator.setDuration(duration);
//            blurAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                int value = 0;
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    int radius = (int) animation.getAnimatedValue() * 6;
//                    Log.v("Anhdts", "initAnimationBlur " + radius);
//                    if (value == radius) {
//                        return;
//                    }
//                    value = radius;
//                    Bitmap blurBitmap = blur(radius, bitmap);
//                    findViewById(R.id.content_view_speed).setBackground(new BitmapDrawable(getResources(), blurBitmap));
//                }
//            });
//            arrayListObjectAnimators.add(blurAnimator);
//        }

        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(arrayListObjectAnimators);
        animSetXY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsLockAnimation = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isModeSearch) {
                    if (isChangeMode) {
                        mContentViewSearch.setAlpha(1);
                        mContentViewSpeed.setAlpha(0);
                    } else {
                        finish();
                    }
                } else {
                    mContentViewSearch.setVisibility(View.INVISIBLE);
                    mContentViewSearch.setAlpha(0);
                    mContentViewSpeed.setAlpha(1);
                }
                mIsLockAnimation = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSetXY.setDuration(duration);
        animSetXY.start();
    }


/*    private void animationView(final boolean isModeSearch, float rate) {
        final float alphaSpeed = rate > 0.6f ? 0 : (1 - rate / 0.6f);
        float alphaSearch = rate < 0.4f ? 0 : ((rate - 0.4f) / 0.6f);

        final Animation alphaAnim = new AlphaAnimation(alphaSearch, isModeSearch ? 1 : 0);
        alphaAnim.setDuration(5000);

        Animation alphaAnimSpeed = new AlphaAnimation(alphaSpeed, isModeSearch ? 0 : 1);
        alphaAnimSpeed.setDuration(5000);

        Log.v("Anhdts", "animationView " + alphaSpeed);
        mContentViewSpeed.setVisibility(View.VISIBLE);
        mContentViewSpeed.setAlpha(alphaSpeed);

        alphaAnimSpeed.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mContentViewSearch.startAnimation(alphaAnim);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isModeSearch) {
                    mContentViewSearch.setAlpha(1);
                    mContentViewSpeed.setAlpha(0);
                } else {
                    mContentViewSearch.setVisibility(View.INVISIBLE);
                    mContentViewSearch.setAlpha(0);
                    mContentViewSpeed.setAlpha(1);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mContentViewSpeed.startAnimation(alphaAnimSpeed);
    }*/


    /**
     * Anhdts search popup magic
     */
    public void querySmartContact(String query, SuggestPopup.ActionSmartSuggest listener) {
        if (mSmartSuggestLoaderManage != null) {
            mSmartSuggestLoaderManage.startLoad(query, false, listener);
        }
    }

    /**
     * Anhdts neu trong che do search thi thoat search, khong thi animation view
     */
    @Override
    public void onBackPressed() {
        if (mIsShowSearch) {
            mSearchFragment.existSearchMode();
        } else {
//            animationView(true, 0, false);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
            finish();
//                }
//            }, 200);
        }
    }

    /**
     * Anhdts thoat che do search; an popup search
     */
    public void existSearchMode() {
        mContentViewSpeed.setVisibility(View.VISIBLE);
        mSmartSuggestLoaderManage.hideViewSuggest();
        mIsShowSearch = false;
        animationView(false, 1, true);
    }

    private void dispatchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
    }

    private ScriptIntrinsicBlur mScriptIntrinsicBlur;

    private RenderScript mRenderScript;

//    private static final int MIN_RADIUS = 1;

//    /**
//     * Anhdts
//     */
//    public void initAnimationBlur() {
//        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
//        final Bitmap bitmap = drawableToBitmap(wallpaperManager.getDrawable());
//        ValueAnimator blurAnimator = ValueAnimator.ofInt(MIN_RADIUS, 4);
//        blurAnimator.setDuration(150);
//        blurAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            public void onAnimationUpdate(ValueAnimator animation) {
//                int radius = (int) animation.getAnimatedValue() * 6;
//
//                Bitmap blurBitmap = blur(radius, bitmap);
//                findViewById(R.id.content_view_speed).setBackground(new BitmapDrawable(getResources(), blurBitmap));
//            }
//        });
//        blurAnimator.start();
//    }

    /**
     * Anhdts bur animation. blur tu tu
     */
    public Bitmap blur(float radius, Bitmap bitmapToBlur) {
        Bitmap outputBitmap = Bitmap.createBitmap(bitmapToBlur);
        Allocation tmpIn = Allocation.createFromBitmap(mRenderScript, bitmapToBlur);
        Allocation tmpOut = Allocation.createFromBitmap(mRenderScript, outputBitmap);
        mScriptIntrinsicBlur.setRadius(radius);
        mScriptIntrinsicBlur.setInput(tmpIn);
        mScriptIntrinsicBlur.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        tmpIn.destroy();
        tmpOut.destroy();
        return outputBitmap;
    }

    /**
     * Anhdts change drawable to bitmap de blur; scale size vua voi man hinh
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        if (height > displayMetrics.heightPixels) {
            height = displayMetrics.heightPixels;
        }
        if (width > displayMetrics.widthPixels) {
            width = displayMetrics.widthPixels;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Anhdts set background blur
     */
    private void setBackgroundBlur(int radius) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        if (radius > 0) {
            Bitmap bitmap = drawableToBitmap(wallpaperManager.getDrawable());
            findViewById(R.id.root_view).setBackground(new BitmapDrawable(getResources(), blur(radius, bitmap)));
            bitmap.recycle();
        } else {
            findViewById(R.id.root_view).setBackground(wallpaperManager.getDrawable());
        }
    }
}