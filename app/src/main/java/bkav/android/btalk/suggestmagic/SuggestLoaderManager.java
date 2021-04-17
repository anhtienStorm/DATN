package bkav.android.btalk.suggestmagic;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.text.TextUtils;

import com.android.dialer.database.DialerDatabaseHelper;
import com.android.ex.chips.RecipientEntry;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.R;

/**
 * Created by anhdt on 30/10/2017.
 * class hien thi goi y danh ba dau tien sap xep theo tieu chi magic pad
 */

public class SuggestLoaderManager implements LoaderManager.LoaderCallbacks<DialerDatabaseHelper.ContactNumber[]> {

    private Activity mContext;

    private String mQuery;

    private SuggestPopup mSuggestView;

    private static final int ID_LOADER_SUGGEST = 7531;

    private ArrayList<SuggestContactDetail> mListTempSuggest = new ArrayList<>();

    private String mLastTextQuery = "";

    private long mLastTimeCheckSuggest = 0;

    private static final long MAX_TIME_CHECK_DELAY = 1000;

    private boolean mReadySearch = false;

    public SuggestLoaderManager(Activity context) {
        mContext = context;
        mContext.getLoaderManager().initLoader(ID_LOADER_SUGGEST, null, this);
        mSuggestView = new SuggestPopup(mContext);
    }

    public SuggestLoaderManager(Activity activity, SuggestPopup.ActionSmartSuggest listener, int paddingBottom) {
        this(activity);
        mSuggestView.setListener(listener);
        mSuggestView.setMessageMode(paddingBottom);
    }

    @Override
    public Loader<DialerDatabaseHelper.ContactNumber[]> onCreateLoader(int id, Bundle args) {
        SmartSuggestLoader loader = new SmartSuggestLoader(mContext);
        if (TextUtils.isEmpty(mQuery)) {
            loader.configureQuery("");
        } else {
            if (mRecipientEntries != null) {
                loader.configureBlackList(mRecipientEntries);
            }
            loader.configureQuery(mQuery);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<DialerDatabaseHelper.ContactNumber[]> loader, DialerDatabaseHelper.ContactNumber[] data) {
        if (data == null || data.length == 0 || !mReadySearch) {
            clearTempSuggest();
            mSuggestView.clearView();
            mSuggestView.hide();
        } else {
            mSuggestView.setData(getContactSuggest(data), mQuery);
            mSuggestView.showAsDropDown(mContext.findViewById(R.id.root_view));
        }
    }

    @Override
    public void onLoaderReset(Loader<DialerDatabaseHelper.ContactNumber[]> loader) {
        clearTempSuggest();
        mSuggestView.hide();
        mSuggestView.clearView();
    }

    public void startLoad(String query, boolean showNumber, SuggestPopup.ActionSmartSuggest listener) {
        mReadySearch = true;
        mSuggestView.setShowNumberMode(showNumber);
        mSuggestView.setListener(listener);
        mQuery = query;
        if (mQuery.length() > 0) {
            Loader loader = mContext.getLoaderManager().getLoader(ID_LOADER_SUGGEST);
            if (loader != null && !loader.isReset()) {
                mContext.getLoaderManager().restartLoader(ID_LOADER_SUGGEST, null, this);
            } else {
                if (loader != null) {
                    stopLoader();
                }
                mContext.getLoaderManager().initLoader(ID_LOADER_SUGGEST, null, this);
            }
        } else {
            stopLoader();
            clearTempSuggest();
            mSuggestView.clearView();
            mSuggestView.hide();
        }
    }

    private List<RecipientEntry> mRecipientEntries;//Bkav QuangNDb Them bien de tao danh sach den khi load

    /**Bkav QuangNDb start load o giao dien message*/
    public void startLoadMessage(String query, boolean showNumber, List<RecipientEntry> entries) {
        mReadySearch = true;
        mSuggestView.setShowNumberMode(showNumber);
        mQuery = query;
        mRecipientEntries = entries;
        if (mQuery.length() > 0) {
            Loader loader = mContext.getLoaderManager().getLoader(ID_LOADER_SUGGEST);
            if (loader != null && !loader.isReset()) {
                mContext.getLoaderManager().restartLoader(ID_LOADER_SUGGEST, null, this);
            } else {
                mContext.getLoaderManager().initLoader(ID_LOADER_SUGGEST, null, this);
            }
        } else {
            stopLoader();
            clearTempSuggest();
            mSuggestView.clearView();
            mSuggestView.hide();
        }
    }

    private void stopLoader() {
        mContext.getLoaderManager().destroyLoader(ID_LOADER_SUGGEST);
    }

    /**
     * Anhdts xu ly du lieu suggest
     */
    private DialerDatabaseHelper.ContactNumber getContactSuggest(DialerDatabaseHelper.ContactNumber[] results) {
        if (results != null && results.length > 0) {
            long timeCurrent = System.currentTimeMillis();
            getPosSuggest(results, timeCurrent);

            DialerDatabaseHelper.ContactNumber contactNumberSuggest;
            if (results.length > mPosTakeSuggest) {
                contactNumberSuggest = results[mPosTakeSuggest];
            } else {
                contactNumberSuggest = results[results.length - 1];
            }

            mLastTimeCheckSuggest = timeCurrent;
            mLastTextQuery = mQuery;
            return contactNumberSuggest;
        } else {
            mLastTimeCheckSuggest = 0;
            mLastTextQuery = "";
            mListTempSuggest.clear();
            mPosTakeSuggest = 0;
            return null;
        }
    }

    private int mPosTakeSuggest = 0;

    /**
     * Anhdts lay vi tri phan tu goi y
     */
    private void getPosSuggest(DialerDatabaseHelper.ContactNumber[] listMatch, long timeCurrent) {
        if (mLastTextQuery.equals(mQuery)) {
            mPosTakeSuggest = 0;
            for (SuggestContactDetail tmp : mListTempSuggest) {
                if (tmp.mTextQuery.equals(mQuery)) {
                    tmp.mPosTakeSuggest = 0;
                    break;
                }
            }
            return;
        } else {
            // Vi tri suggest set o diem cuoi cung
            mPosTakeSuggest = 0;
            // TH1 vua moi khoi tao hoac nhap so dau tien thi vi tri bang 0 luon
            if (mLastTimeCheckSuggest == 0 || TextUtils.isEmpty(mQuery) || (mLastTextQuery.isEmpty() && mQuery.length() == 1)) {
                mListTempSuggest.clear();
                SuggestContactDetail currentContact = new SuggestContactDetail(listMatch[0]);
                currentContact.setMakeSuggest(mQuery, 0);
                mListTempSuggest.add(currentContact);
                return;
            }
            // TH2 thoi gian nhap phim lon hon thoi gian MAGIC
            else if ((timeCurrent - mLastTimeCheckSuggest) > MAX_TIME_CHECK_DELAY) {
                // Neu text cu ngan hon text moi, bat dau tim kiem
                if (mLastTextQuery.length() > mQuery.length()) {
                    // Neu text cu dai hon text moi, tim trong mang, neu khong co thi search binh thuong
                    int i = mListTempSuggest.size();
                    while (i > 0 && !mListTempSuggest.get(i - 1).mTextQuery.equals(mQuery)) {
                        i--;
                    }
                    if (i > 0) {
                        int total = mListTempSuggest.size();
                        for (int j = i; j < total; j++) {
                            mListTempSuggest.remove(mListTempSuggest.size() - 1);
                        }
                        mPosTakeSuggest = mListTempSuggest.get(i - 1).mPosTakeSuggest;
                        return;
                    }
                }
            } else {
                SuggestContactDetail currentContact = new SuggestContactDetail(listMatch[0]);
                currentContact.setMakeSuggest(mQuery, 0);
                mListTempSuggest.add(currentContact);
                return;
            }
        }

        int i = 0;
        do {
            SuggestContactDetail currentContact = new SuggestContactDetail(listMatch[i]);
            boolean found = false;
            for (SuggestContactDetail tmp : mListTempSuggest) {
                if (currentContact.equals(tmp)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                mPosTakeSuggest = i;
                currentContact.setMakeSuggest(mQuery, mPosTakeSuggest);
                mListTempSuggest.add(currentContact);
                return;
            }
            i++;
        } while (i < listMatch.length);
        mPosTakeSuggest = 0;
    }

    private void clearTempSuggest() {
        mLastTimeCheckSuggest = 0;
        mLastTextQuery = "";
        mListTempSuggest.clear();
        mPosTakeSuggest = 0;
    }

    public void hideViewSuggest() {
        if (mSuggestView.isShowing()) {
            mReadySearch = false;
            mSuggestView.hide();
        }
    }

    public void updateSim() {
        mSuggestView.updateSim();
    }

    public boolean isInteractive() {
        return mSuggestView.isInteract();
    }
}
