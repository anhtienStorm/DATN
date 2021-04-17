package android.support.v4.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by trungth on 05/07/2017.
 */

public class BkavViewPager extends ViewPager {

    public BkavViewPager(Context context) {
        super(context);
    }

    public BkavViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    //========================BKAV ==========================//

    public static final boolean ENABLE_VIEWPAGER = true;

    public static final int TAB_NUMBERS_FIRST_TIME;

    static {
        if (ENABLE_VIEWPAGER) {
            TAB_NUMBERS_FIRST_TIME = 1;
        } else {
            TAB_NUMBERS_FIRST_TIME = 0;
        }
    }

    // Bkav QuangLH:
    private boolean mEnablePager;

    private boolean mKeepPage;

    private boolean mEnableLoadOneTab = false;

    // TODO XXX BUILD MMM THI MO COMMENT DOAN NAY TOI DOAN TODO BEN DUOI
//    @Override
//    protected void initValue() {
//        // Bkav QuangLH
//        mEnablePager = true;
//        mKeepPage = false;
//    }
//
//    // Bkav QuangLH:
//    public void enablePager(boolean enable) {
//        mEnablePager = enable;
//    }
//
//    @Override
//    protected boolean isEnablePager() {
//        return mEnablePager;
//    }
//
//    // Bkav QuangLH:
//    public void enableKeepPage(boolean enable) {
//        mKeepPage = enable;
//    }
//
//    @Override
//    protected boolean isEnableKeepPager() {
//        return mKeepPage;
//    }
//
//    @Override
//    protected boolean ismEnableLoadOneTab() {
//        return mEnableLoadOneTab;
//    }
//
//    // Bkav Trungth
//    public void enableLoadOneTab(boolean enable) {
//        if (enable) {
//            mEnableLoadOneTab = enable;
//            mOffscreenPageLimit = 0;
//        }
//    }
//
//    @Override
//    protected int getPageLimit() {
//        return mEnableLoadOneTab ? 0 : mOffscreenPageLimit;
//    }
//
//    @Override
//    protected void initEnableLoadOneTab(boolean enableLoadMoreTab) {
//        // Bkav TrungTH ko de size bang 1 vi ham populate sau khi tao xong view dau
//        // van duoc chay o ham onlayout => neu de >=1 => trong qua trinh day lai
//        // load tiep => van day do
//        // de khi nao item = 2 tro len thi se mo viec load cac tab ben canh
//        // th khi moi 1 tab ma vuot tay => them biet enableLoadMoreTab de bat
//        // viec load tab ben canh
//        if (mItems.size() > 1 || enableLoadMoreTab) {
//            mEnableLoadOneTab = false;
//        }
//        // Xac dinh pos dau va pos cuoi dua vao so pagelimmit duoc set
//        // Neu dat la 2 thi diem dau se la 2 tab phia truoc vi tri hien tai,
//        // dieu cuoi se la 2 tab phia sau,
//    }
//
//    @Override
//    protected float getExtraWidthLeft() {
//        return mEnableLoadOneTab ? 1.f : 0.0f;
//    }
//
//    @Override
//    protected float getExtraWidthRight(ItemInfo curItem) {
//        return mEnableLoadOneTab ? 2.f : curItem.widthFactor; // BKav TrungTH them vao
//    }


    //TODO BUILD ANDROID STUDIO THI MO COMMENT DOAN DUOI RA VA CM DOAN BEN TREN LAI
    //    //====================================================
    // Doan code them vao de fix loi muldex
    public void enablePager(boolean enable) {
    }

    public void enableLoadOneTab(boolean enable) {
    }

    public void enableKeepPage(boolean enable) {
    }

    public void populate(boolean enableLoadMoreTab) {
    }
}
