package bkav.android.btalk.calllog.ulti;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.android.contacts.common.compat.CompatUtils;
import com.android.dialer.calllog.PhoneAccountUtils;
import com.android.dialer.calllog.calllogcache.CallLogCache;
import com.android.dialer.util.TelecomUtil;
import com.android.messaging.util.PhoneUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.esim.ISimProfile;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.esim.provider.ESimDbController;
import bkav.android.btalk.utility.PermissionUtil;

/**
 * Created by anhdt on 20/07/2017.
 * lop load icon sim
 */

public class BtalkCallLogCache extends CallLogCache {

    private boolean mIsMutilSim = false;

    private boolean mIsSimChange = false;

    private String mDefaultSimId;

    private SubscriptionManager mSubscriptionManager;
    //Bkav QuangNDb cache profile list esim
    private List<ISimProfile> mProfileListCache = new ArrayList<>();
    //Bkav QuangNDb cache subsciption list sim thuong
    private List<SubscriptionInfo> mSubscriptionInfoListCache = new ArrayList<>();

    private int mSim1Color;
    private int mSim2Color;

    private BtalkCallLogCache(Context context) {
        super(context);
        mSimIconCache = new HashMap<>();
        mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        mSim1Color = context.getColor(R.color.esim_01);
        mSim2Color = context.getColor(R.color.esim_02);
        checkMutilSim();
        mDefaultSimId = "";
    }

    private HashMap<String, Drawable> mSimIconCache;

    public static BtalkCallLogCache getCallLogCache(Context context) {
        if (sInstance == null) {
            if (CompatUtils.isClassAvailable("android.telecom.PhoneAccountHandle")) {
                sInstance = new BtalkCallLogCache(context);
            }
        }
        return sInstance;
    }

    public void clearCache() {
        mSimIconCache.clear();
        checkMutilSim();
        mDefaultSimId = "";
    }

    @Override
    public boolean isVoicemailNumber(PhoneAccountHandle accountHandle, CharSequence number) {
        return false;
    }

    @Override
    public String getAccountLabel(PhoneAccountHandle accountHandle) {
        return null;
    }

    @Override
    public int getAccountColor(PhoneAccountHandle accountHandle) {
        return 0;
    }

    @Override
    public boolean doesAccountSupportCallSubject(PhoneAccountHandle accountHandle) {
        return false;
    }

    /**
     * Anhdhts get drawable cho icon sim
     * Neu chua co thi load trong du lieu roi luu vao cache
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public Drawable getAccountIcon(PhoneAccountHandle phoneAccount) {
        if (phoneAccount == null) {
            return null;
        }
        final String iccidPhoneAccount = stripTrailingFs(phoneAccount.getId());
        Drawable drawable = mSimIconCache.get(phoneAccount.getId());
        if (drawable != null) {
            return drawable;
        } else if (mIsMutilSim || mProfileListCache.size() > 1) {
            PhoneAccount account = PhoneAccountUtils.getAccountOrNull(mContext, phoneAccount);
            if (account == null) {
                //Bkav QuangNDb van ve mau cac profile chua activate trong list sim
                if (mProfileListCache.size() > 0) {
                    for (int i = 0; i < mProfileListCache.size(); i++) {
                        if (iccidPhoneAccount.equals(new String(mProfileListCache.get(i).getSimIdProfile()))) {
                            drawable = new BitmapDrawable(mContext.getResources(), getIconSimBitmap(mContext, mProfileListCache.get(i).getColor(), i));
                            mSimIconCache.put(phoneAccount.getId(), drawable);
                            return drawable;
                        }
                    }
                }
                mSimIconCache.put(phoneAccount.getId(), null);
                return null;
            } else {
                // TrungTH them nhanh ve lai icon
                if (mSubscriptionManager != null && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    //Bkav QuangNDb neu co eSim thi lay mau trong danh sach eSim,sim thuong
                    if (mProfileListCache.size() > 0) {
                        for (int i = 0; i < mProfileListCache.size(); i++) {
                            if (iccidPhoneAccount.equals(new String(mProfileListCache.get(i).getSimIdProfile()))) {
                                drawable = new BitmapDrawable(mContext.getResources(), getIconSimBitmap(mContext, mProfileListCache.get(i).getColor(), i));
                                mSimIconCache.put(phoneAccount.getId(), drawable);
                                return drawable;
                            }
                        }

                    } else {
                        for (SubscriptionInfo subscriptionInfo : mSubscriptionInfoListCache) {
                            if (iccidPhoneAccount.equals(subscriptionInfo.getIccId())) {
                                drawable = new BitmapDrawable(mContext.getResources(), getIconSimBitmap(mContext, subscriptionInfo.getSimSlotIndex() == 0 ? mSim1Color : mSim2Color, subscriptionInfo.getSimSlotIndex()));
                                break;
                            }
                        }
                    }

                    if (drawable == null) {
                        if (account.getIcon() != null) {
                            drawable = account.getIcon().loadDrawable(mContext);
                        } else {
                            return null;
                        }
                    }
                    mSimIconCache.put(phoneAccount.getId(), drawable);
                }
                return drawable;
            }
        }
        return null;
    }

    //Bkav QuangNDb lay icon sim tu iccid
    public Drawable getSimIconWithIccid(String iccid) {
        if (iccid == null) {
            return null;
        }
        Drawable drawable = mSimIconCache.get(iccid);
        if (drawable != null) {
            return drawable;
        } else {
            // TrungTH them nhanh ve lai icon
            if (mSubscriptionManager != null && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                //Bkav QuangNDb neu co eSim thi lay mau trong danh sach eSim,sim thuong
                if (mProfileListCache.size() > 0) {
                    for (int i = 0; i < mProfileListCache.size(); i++) {
                        if (iccid.equals(new String(mProfileListCache.get(i).getSimIdProfile()))) {
                            drawable = new BitmapDrawable(mContext.getResources(), getIconSimBitmap(mContext, mProfileListCache.get(i).getColor(), i));
                            mSimIconCache.put(iccid, drawable);
                            return drawable;
                        }
                    }

                } else {
                    if (mSubscriptionInfoListCache != null) {
                        for (SubscriptionInfo subscriptionInfo : mSubscriptionInfoListCache) {
                            if (iccid.equals(subscriptionInfo.getIccId())) {
                                drawable = new BitmapDrawable(mContext.getResources(), getIconSimBitmap(mContext, subscriptionInfo.getSimSlotIndex() == 0 ? mSim1Color : mSim2Color, subscriptionInfo.getSimSlotIndex()));
                                break;
                            }
                        }
                    }
                }
                mSimIconCache.put(iccid, drawable);
            }
            return drawable;
        }
    }


    /**
     * Strip all the trailing 'F' characters of a string, e.g., an ICCID.
     * Bkav QuangNDb format lai iccid khi lay ra tu PhoneAccountHandle
     */
    private String stripTrailingFs(String s) {
        return s == null ? null : s.replaceAll("(?i)f*$", "");
    }


    private BitmapDrawable getRegularSim() {
        return (BitmapDrawable) ContextCompat.getDrawable(mContext, R.drawable.ic_esim_calllog);
    }

    //Bkav QuangNDb tra ve bitmap icon sim voi mau va so sim
    private Bitmap getIconSimBitmap(Context context, int color, int pos) {
        BitmapDrawable bitmapDrawable = getRegularSim();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        paint.setAlpha(0xff);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(bitmap, 0, 0, paint);

        // Write the sim slot index.
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        paint.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP));
        // Set text size scaled by density
        paint.setTextSize(8 * metrics.density);
        // Convert sim slot index to localized string
        final String index = String.format("%d", pos + 1);
        final Rect textBound = new Rect();
        paint.getTextBounds(index, 0, 1, textBound);
        final float xOffset = (getRegularSim().getBitmap().getWidth() / 2.f) - textBound.centerX();
        final float yOffset = (getRegularSim().getBitmap().getHeight() / 2.f) - textBound.centerY();
        canvas.drawText(index, xOffset, yOffset, paint);
        return newBitmap;
    }

    /**
     * Anhdts kiem tra lai moi khi resume hoac sim thay doi
     */
    public void checkMutilSim() {
        if (PermissionUtil.get().requestPermission(mContext)) {
            mIsMutilSim = PhoneUtils.getDefault().getActiveSubscriptionCount() > 1;
//            List<PhoneAccountHandle> listAccount = PhoneAccountUtils.getSubscriptionPhoneAccounts(mContext);
//                    &&
//                            listAccount != null && listAccount.size() > 1;
            // TrungTH them doan check sim Default xem co thay doi hay khong
            PhoneAccountHandle defaultHandle = TelecomUtil.getDefaultOutgoingPhoneAccount(mContext,
                    PhoneAccount.SCHEME_TEL);
            String defaulSimId;
            defaulSimId = (defaultHandle != null) ? defaultHandle.getId() : "";
            if (defaulSimId != null && !defaulSimId.equals(mDefaultSimId)) {
                mIsSimChange = true;
                mDefaultSimId = defaulSimId;
            }
            //Bkav QuangNDb update list profile khi ho tro eSim
            if (ESimUtils.isSupportEsim()) {
                mProfileListCache = ESimDbController.getAllSim();
                //Bkav QuangNDb list cache sim thuong co the bang null khi thao sim ra
                if (mSubscriptionInfoListCache != null && !mSubscriptionInfoListCache.isEmpty()) {
                    mSubscriptionInfoListCache.clear();
                }
            } else {
                mProfileListCache.clear();
                //Bkav QuangNDb cache list sub info sim thuong
                mSubscriptionInfoListCache = mSubscriptionManager.getActiveSubscriptionInfoList();
            }
        }
    }

    //Bkav QuangNDb clear cache sim icon khi co su kien sim thay doi
    public void clearSimIconCache() {
        mSimIconCache.clear();
    }

    public void setSimChange() {
        mIsSimChange = true;
    }

    //Bkav QuangNDb check ca 2 slot deu cam sim
    public boolean isHasSimOnAllSlot() {
        return mIsMutilSim;
    }


    public boolean isSimChange() {
        if (mIsSimChange) {
            mIsSimChange = false;
            return true;
        } else {
            return false;
        }
    }

    // Anhdts luu static class nay
    private static BtalkCallLogCache sInstance;
}
