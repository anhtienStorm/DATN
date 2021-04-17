package bkav.android.btalk.mutilsim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.contacts.common.CallUtil;
import com.android.dialer.calllog.PhoneAccountUtils;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.TelecomUtil;
import com.android.ex.chips.ChipsUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.fragments.dialpad.BtalkDialpadFragment;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by anhdt on 20/07/2017.
 *
 */

public class SimUltil {

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static LinearLayout showSimChooseView(View viewAnchor, View rootView, LinearLayout container, BtalkDialpadFragment fragment) {
        if (rootView instanceof RelativeLayout) {
            Context context = rootView.getContext();
            if (container == null) {
                container = inflateViewContainer(viewAnchor, rootView, fragment);
            }
            animateOpen(context, container);
        }
        return container;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static LinearLayout inflateViewContainer(View viewAnchor, View rootView, BtalkDialpadFragment fragment) {
        Context context = rootView.getContext();
        LinearLayout container = new LinearLayout(context);

        int[] posAnchor = new int[2];

        viewAnchor.getLocationOnScreen(posAnchor);

        boolean viewInRight = true;

        if (posAnchor[0] > 1600) {
            viewInRight = false;
        }

        RelativeLayout.LayoutParams paramContainer =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramContainer.addRule(RelativeLayout.ABOVE, viewAnchor.getId());

        if (viewInRight) {
            paramContainer.addRule(RelativeLayout.ALIGN_START, viewAnchor.getId());
        } else {
            paramContainer.addRule(RelativeLayout.ALIGN_END, viewAnchor.getId());
        }

        container.setLayoutParams(paramContainer);
        container.setOrientation(LinearLayout.VERTICAL);
        inflateSimRow(context, container, true, fragment);
        inflateSimRow(context, container, false, fragment);

        View arrow = addArrowView(container, true, viewInRight, viewAnchor.getWidth() * 4 / 7,
                context.getResources().getDimensionPixelSize(R.dimen.margin_sim_row), context);
        arrow.setPivotX(arrow.getWidth() / 2);
        arrow.setPivotY(0);
        arrow.setElevation(container.getChildAt(0).getElevation());
        ((RelativeLayout) rootView).addView(container);
        container.requestFocus();
        return container;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void inflateSimRow(Context context, LinearLayout container, final boolean isSimDefault, final BtalkDialpadFragment fragment) {
        LayoutInflater inflater = LayoutInflater.from(context);
        BtalkSimLineChooser simRow = (BtalkSimLineChooser) inflater.inflate(R.layout.btalk_sim_row, container, false);

        PhoneAccountHandle handleDefault = isSimDefault ? TelecomUtil.getDefaultOutgoingPhoneAccount(context,
                PhoneAccount.SCHEME_TEL) : getHandleNotDefaultSim(context);
        Drawable drawableSim = BtalkCallLogCache.getCallLogCache(context).getAccountIcon(handleDefault);
        simRow.applyInfo(drawableSim, isSimDefault ? getDefaultSimName(context, handleDefault) :
                getNotDefaultSimName(context));
        simRow.setPhone(isSimDefault ? getDefaultSimPhone(context, handleDefault) :
                getNotDefaultSimPhone(context, handleDefault));
        if (isSimDefault) {
            ((LinearLayout.LayoutParams) simRow.getLayoutParams()).bottomMargin =
                    context.getResources().getDimensionPixelSize(R.dimen.sim_spacing_row);
            ((LinearLayout.LayoutParams) simRow.getLayoutParams()).topMargin =
                    context.getResources().getDimensionPixelSize(R.dimen.margin_sim_row);
        }
        simRow.setVisibility(View.INVISIBLE);
        simRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.actionChooseSimCall(isSimDefault);
            }
        });
        container.addView(simRow);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static ViewGroup inflateSimPopupSearch(Context context, ViewGroup container, boolean isSimDefault) {
        ImageView iconView = (ImageView) container.getChildAt(0);
        TextView labelView = (TextView) container.getChildAt(1);

        PhoneAccountHandle handleDefault = isSimDefault ? TelecomUtil.getDefaultOutgoingPhoneAccount(context,
                PhoneAccount.SCHEME_TEL) : getHandleNotDefaultSim(context);
        Drawable drawableSim = BtalkCallLogCache.getCallLogCache(context).getAccountIcon(handleDefault);
        labelView.setText(isSimDefault ? getDefaultSimName(context, handleDefault) :
                getNotDefaultSimName(context));
        iconView.setImageDrawable(drawableSim);
        return container;
    }

    private static View addArrowView(LinearLayout container, boolean isAboveIcon, boolean isLeftAligned, int horizontalOffset, int verticalOffset, Context context) {
        int size = context.getResources().getDimensionPixelSize(R.dimen.size_triangle_sim);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        if (isLeftAligned) {
            layoutParams.gravity = Gravity.START;
            layoutParams.leftMargin = horizontalOffset;
        } else {
            layoutParams.gravity = Gravity.END;
            layoutParams.rightMargin = horizontalOffset;
        }
        if (isAboveIcon) {
            layoutParams.bottomMargin = verticalOffset;
        } else {
            layoutParams.topMargin = verticalOffset;
        }
        View arrowView = new View(context);

        arrowView.setBackground(getShapeDrawable(context, size, R.color.background_sim_row, isAboveIcon));
        // arrowView.setElevation(container.getElevation());
        container.addView(arrowView, isAboveIcon ? container.getChildCount() : 0, layoutParams);
        return arrowView;
    }

    public static ShapeDrawable getShapeDrawable(Context context, int size, int resourceColor, boolean isAboveIcon) {
        ShapeDrawable arrowDrawable = new ShapeDrawable(TriangleShape.create(
                size, size, !isAboveIcon));
        arrowDrawable.getPaint().setColor(ContextCompat.getColor(context, resourceColor));
        return arrowDrawable;
    }

    private static void animateOpen(final Context context, final View container) {
        container.setVisibility(View.VISIBLE);

        final AnimatorSet shortcutAnims = new AnimatorSet();

        final long duration = context.getResources().getInteger(
                R.integer.config_sim_line_open_duration);
        final long arrowScaleDuration = context.getResources().getInteger(
                R.integer.config_sim_arrow_open_duration);
        final long arrowScaleDelay = duration - arrowScaleDuration;
        final long stagger = context.getResources().getInteger(
                R.integer.config_sim_line_open_stagger);
        final TimeInterpolator fadeInterpolator = new LogAccelerateInterpolator(100, 0);

        // Animate shortcuts
        DecelerateInterpolator interpolator = new DecelerateInterpolator();
        for (int i = 0; i < 2; i++) {
            final BtalkSimLineChooser lineFirst = (BtalkSimLineChooser) ((LinearLayout) container).getChildAt(i);
            lineFirst.setVisibility(View.INVISIBLE);
            lineFirst.setAlpha(0);

            Animator anim = lineFirst.createOpenAnimation(true, true);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    lineFirst.setVisibility(View.VISIBLE);
                }
            });
            anim.setDuration(duration);
            anim.setStartDelay(stagger * (1 - i));
            anim.setInterpolator(interpolator);
            shortcutAnims.play(anim);

            Animator fadeAnim = new BtalkViewPropertyAnimator(lineFirst).alpha(1);
            fadeAnim.setInterpolator(fadeInterpolator);
            // We want the shortcut to be fully opaque before the arrow starts animating.
            fadeAnim.setDuration(arrowScaleDelay);
            shortcutAnims.play(fadeAnim);
        }

        // Animate the arrow
        View arrow = ((LinearLayout) container).getChildAt(2);
        arrow.setScaleX(0);
        arrow.setScaleY(0);
        Animator arrowScale = new BtalkViewPropertyAnimator(arrow).scaleX(1).scaleY(1);
        arrowScale.setStartDelay(arrowScaleDelay);
        arrowScale.setDuration(arrowScaleDuration);
        shortcutAnims.play(arrowScale);

        shortcutAnims.start();
    }

    public static void animateClose(final Context context, final View container) {
        if (!container.isShown()) {
            return;
        }

        final AnimatorSet shortcutAnims = new AnimatorSet();
        int numOpenShortcuts = 0;
        for (int i = 0; i < 2; i++) {
            if (((BtalkSimLineChooser) ((LinearLayout) container).getChildAt(i)).isOpenOrOpening()) {
                numOpenShortcuts++;
            }
        }
        final long duration = context.getResources().getInteger(
                R.integer.config_sim_line_close_duration);
        final long arrowScaleDuration = context.getResources().getInteger(
                R.integer.config_sim_arrow_close_duration);
        final long stagger = context.getResources().getInteger(
                R.integer.config_sim_line_close_stagger);
        final TimeInterpolator fadeInterpolator = new LogAccelerateInterpolator(100, 0);

        int firstOpenShortcutIndex = 0;
        for (int i = firstOpenShortcutIndex; i < firstOpenShortcutIndex + numOpenShortcuts; i++) {
            final BtalkSimLineChooser view = (BtalkSimLineChooser) ((LinearLayout) container).getChildAt(i);
            Animator anim;
            if (view.willDrawIcon()) {
                anim = view.createCloseAnimation(true, true, duration);
                anim.setStartDelay(stagger * i);

                Animator fadeAnim = new BtalkViewPropertyAnimator(view).alpha(0);
                // Don't start fading until the arrow is gone.
                fadeAnim.setStartDelay(stagger * i + arrowScaleDuration);
                fadeAnim.setDuration(duration - arrowScaleDuration);
                fadeAnim.setInterpolator(fadeInterpolator);
                shortcutAnims.play(fadeAnim);
            } else {
                // The view is being dragged. Animate it such that it collapses with the drag view
                anim = view.collapseToIcon();
                anim.setDuration(duration);

                // Scale and translate the view to follow the drag view.
                Point iconCenter = view.getIconCenter();
                view.setPivotX(iconCenter.x);
                view.setPivotY(iconCenter.y);

                BtalkViewPropertyAnimator anim2 = new BtalkViewPropertyAnimator(view)
                        .scaleX(0.2f)
                        .scaleY(0.2f);
                anim2.setDuration(duration);
                shortcutAnims.play(anim2);
            }
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.INVISIBLE);
                }
            });
            shortcutAnims.play(anim);
        }
        Animator arrowAnim = new BtalkViewPropertyAnimator(((LinearLayout) container).getChildAt(2))
                .scaleX(0).scaleY(0).setDuration(arrowScaleDuration);
        arrowAnim.setStartDelay(0);
        shortcutAnims.play(arrowAnim);

        shortcutAnims.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                container.setVisibility(View.INVISIBLE);
            }
        });
        shortcutAnims.start();
    }

    /**
     * Anhdts hien thi tootip sim
     */
    public static LinearLayout showTooltipSim(View rootView) {
        Context context = rootView.getContext();
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER_HORIZONTAL);

        RelativeLayout.LayoutParams paramContainer =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramContainer.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        paramContainer.addRule(RelativeLayout.CENTER_HORIZONTAL);

        int size = context.getResources().getDimensionPixelSize(R.dimen.size_triangle_tooltip);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        View arrowView = new View(context);
        arrowView.setBackground(getShapeDrawable(context, size, R.color.dialpad_icon_tint, false));
        // arrowView.setElevation(container.getElevation());
        container.addView(arrowView, layoutParams);
        container.setLayoutParams(paramContainer);

        TextView textView = new TextView(context);
        textView.setText(R.string.tooltip_choose_sim);
        textView.setTextSize(10);
        textView.setTextColor(ContextCompat.getColor(context, R.color.btalk_white_bg));
        textView.setBackground(ContextCompat.getDrawable(context, R.drawable.background_ripple_corner));
        textView.getBackground().setTint(ContextCompat.getColor(context, R.color.dialpad_icon_tint));
        textView.setPadding(20, 10, 20, 10);

        textView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        container.addView(textView);

        ((RelativeLayout) rootView).addView(container);

        return container;
    }

    /**
     * Anhdts lay sim mac dinh cuoc goi
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static int getDefaultSimCell(Context context) {

        List<PhoneAccountHandle> subscriptionAccountHandles =
                PhoneAccountUtils.getSubscriptionPhoneAccounts(context);
        PhoneAccountHandle accountDefault = TelecomUtil.getDefaultOutgoingPhoneAccount(context,
                PhoneAccount.SCHEME_TEL);

        if (subscriptionAccountHandles != null){
            if (subscriptionAccountHandles.size() < 2 || accountDefault == null) {
                return -1;
            }

            if (subscriptionAccountHandles.get(0).getId().equals(accountDefault.getId())) {
                return 0;
            } else if (subscriptionAccountHandles.get(1).getId().equals(accountDefault.getId())) {
                return 1;
            }
        }

        Object tm = context.getSystemService(TELEPHONY_SERVICE);
        Method method_getDefaultSim;
        int defaultSim = -1;
        try {
            method_getDefaultSim = tm.getClass().getDeclaredMethod("getDefaultSim");
            method_getDefaultSim.setAccessible(true);
            defaultSim = (Integer) method_getDefaultSim.invoke(tm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultSim;
    }

    /**
     * Anhdts get slot follow accountHandle
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static int getSlotSimByAccount(Context context, PhoneAccountHandle accountHandle) {
        List<PhoneAccountHandle> subscriptionAccountHandles =
                PhoneAccountUtils.getSubscriptionPhoneAccounts(context);
        PhoneAccountHandle accountDefault = TelecomUtil.getDefaultOutgoingPhoneAccount(context,
                PhoneAccount.SCHEME_TEL);
        if (subscriptionAccountHandles.size() < 2 || accountDefault == null) {
            return -1;
        }

        if (subscriptionAccountHandles.get(0).getId().equals(accountHandle.getId())) {
            return 0;
        } else if (subscriptionAccountHandles.get(1).getId().equals(accountHandle.getId())) {
            return 1;
        }
        return -1;
    }

    /**
     * Anhdts lay sim mac dinh cuoc goi
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private static String getDefaultSimName(Context context, PhoneAccountHandle handleDefault) {
        PhoneAccount account = PhoneAccountUtils.getAccountOrNull(context, handleDefault);
        if (account == null) {
            return "";
        }
        if (ChipsUtil.isRunningNOrLater()) {
            return String.valueOf(account.getLabel());
        }
        return "";
    }

    /**
     * Anhdts lay sim mac dinh cuoc goi
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String getSimName(Context context, PhoneAccountHandle handle) {
        PhoneAccount account = PhoneAccountUtils.getAccountOrNull(context, handle);
        if (account == null) {
            return "";
        }
        if (ChipsUtil.isRunningNOrLater()) {
            return String.valueOf(account.getLabel());
        }
        return "";
    }

    /**
     * Anhdts get handle sim not default
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static PhoneAccountHandle getHandleNotDefaultSim(Context context) {
        List<PhoneAccountHandle> subscriptionAccountHandles =
                PhoneAccountUtils.getSubscriptionPhoneAccounts(context);
        if (subscriptionAccountHandles.size() > 1) {
            PhoneAccountHandle handleDefault = TelecomUtil.getDefaultOutgoingPhoneAccount(context,
                    PhoneAccount.SCHEME_TEL);
            if (subscriptionAccountHandles.contains(handleDefault)) {
                subscriptionAccountHandles.remove(handleDefault);
                return subscriptionAccountHandles.get(0);
            }
        }
        return null;
    }

    /**
     * Anhdts get name sim  not default
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String getNotDefaultSimName(Context context) {
        List<PhoneAccountHandle> subscriptionAccountHandles =
                PhoneAccountUtils.getSubscriptionPhoneAccounts(context);
        if (subscriptionAccountHandles.size() > 1) {
            PhoneAccountHandle handleDefault = TelecomUtil.getDefaultOutgoingPhoneAccount(context,
                    PhoneAccount.SCHEME_TEL);
            if (subscriptionAccountHandles.contains(handleDefault)) {
                subscriptionAccountHandles.remove(handleDefault);
                PhoneAccount account = PhoneAccountUtils.getAccountOrNull(context, subscriptionAccountHandles.get(0));
                if (account == null) {
                    return "";
                }
                if (ChipsUtil.isRunningNOrLater()) {
                    return String.valueOf(account.getLabel());
                }
                return "";
            }
        }
        return "";
    }

    /**
     * Anhdts get phone sim  not default
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private static String getNotDefaultSimPhone(Context context, PhoneAccountHandle handleDefault) {
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        List<SubscriptionInfo> subInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        for (SubscriptionInfo subscriptionInfo : subInfoList) {
            if (handleDefault != null && handleDefault.getId() != null) {
                if (!handleDefault.getId().equals("" + subscriptionInfo.getSubscriptionId())) {
                    return subscriptionInfo.getNumber();
                }
            }
        }
        return "";
    }

    /**
     * Anhdts lay so sim mac dinh cuoc goi
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private static String getDefaultSimPhone(Context context, PhoneAccountHandle handleDefault) {
        String number = null;

        SubscriptionManager subscriptionManager = (SubscriptionManager)context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        List<SubscriptionInfo> subInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        for (SubscriptionInfo subscriptionInfo : subInfoList) {
            if (handleDefault != null && handleDefault.getId() != null) {
                if (handleDefault.getId().equals("" + subscriptionInfo.getSubscriptionId())) {
                    number = subscriptionInfo.getNumber();
                    if (number == null) {
                        Method getPhone;
                        Class<?>[] parameter = new Class[1];
                        parameter[0] = int.class;
                        try {
                            TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
                            getPhone = TelephonyManager.class.getDeclaredMethod("getLine1Number", parameter);
                            Object ob_phone;
                            Object[] obParameter = new Object[1];
                            obParameter[0] = subscriptionInfo.getSubscriptionId();
                            ob_phone = getPhone.invoke(tm, obParameter);
                            if (ob_phone != null) {
                                number = ob_phone.toString();
                            }
                        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
        return number;
    }

    public static void callWithSimMode(Context context, boolean isDefault, String number) {
//        Intent intent = new Intent(Intent.ACTION_CALL).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setData(Uri.parse("tel:" + number));

        // Bkav TrungTh thay lai intit,khong hieu sao intent tren khong kiem tra duoc so du tai khoan
        final Intent intent = CallUtil.getCallIntent(number);
        intent.putExtra("com.android.phone.force.slot", true);
        intent.putExtra("Cdma_Supp", true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PhoneAccountHandle handle = isDefault ? TelecomUtil.getDefaultOutgoingPhoneAccount(context,
                    PhoneAccount.SCHEME_TEL) : getHandleNotDefaultSim(context);
            intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", handle);
        }
        DialerUtils.sendBroadcastCount(context, DialerUtils.convertTabSeletedForCall(BtalkActivity.TAB_SELECT), 0);
        DialerUtils.startActivityWithErrorToast(context, intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void callWithSlotSim(Context context, String id, String number) {
        final Intent intent = CallUtil.getCallIntent(number);
        intent.putExtra("com.android.phone.force.slot", true);
        intent.putExtra("Cdma_Supp", true);
        intent.putExtra("esim_disable", true);
        List<PhoneAccountHandle> subscriptionAccountHandles =
                PhoneAccountUtils.getSubscriptionPhoneAccounts(context);
        for (PhoneAccountHandle handle : subscriptionAccountHandles) {
            if (handle.getId().equals(id)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", handle);
                }
                DialerUtils.sendBroadcastCount(context, DialerUtils.convertTabSeletedForCall(BtalkActivity.TAB_SELECT), 0);
                DialerUtils.startActivityWithErrorToast(context, intent);
            }
        }
    }

    // Bkav HuyNQN Kiem tra xem so duoc goi co la ma MMI hay khong
    public static boolean isPotentialMMICode(Uri handle) {
        return (handle != null && handle.getSchemeSpecificPart() != null
                && handle.getSchemeSpecificPart().contains("#"));
    }

    // Bkav HuyNQN Kiem tra xem so duoc goi co la ma MMI hay khong
    public static boolean isPotentialInCallMMICode(Uri handle) {
        if (handle != null && handle.getSchemeSpecificPart() != null &&
                handle.getScheme() != null &&
                handle.getScheme().equals(PhoneAccount.SCHEME_TEL)) {

            String dialedNumber = handle.getSchemeSpecificPart();
            return (dialedNumber.equals("0") ||
                    (dialedNumber.startsWith("1") && dialedNumber.length() <= 2) ||
                    (dialedNumber.startsWith("2") && dialedNumber.length() <= 2) ||
                    dialedNumber.equals("3") ||
                    dialedNumber.equals("4") ||
                    dialedNumber.equals("5"));
        }
        return false;
    }
}
