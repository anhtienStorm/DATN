package bkav.android.btalk.fragments.dialpad;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.style.TtsSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.phone.common.dialpad.DialpadView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.util.BtalkTypefaces;

/**
 * Created by anhdt on 13/04/2017.
 *
 */

public class BtalkDialpadView extends DialpadView {

    public BtalkDialpadView(Context context) {
        super(context);
    }

    public BtalkDialpadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BtalkDialpadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // Anhdts custom lai hieu ung an ban phim
    @Override
    protected int getRippleDrawableId() {
        return R.drawable.btalk_ripple_dialpad;
    }

    @Override
    protected void onFinishInflate() {
        setupKeypad();
        mRateContainer = (ViewGroup)findViewById(R.id.rate_container);
        mIldCountry = (TextView) mRateContainer.findViewById(R.id.ild_country);
        mIldRate = (TextView)mRateContainer.findViewById(R.id.ild_rate);
    }

    @Override
    protected void setNumberViewTypeface(TextView numberView) {
        numberView.setTypeface(BtalkTypefaces.sRobotoRegularFont);
    }

    @Override
    public void setCanDigitsBeEdited(boolean canBeEdited) {
        mCanDigitsBeEdited = canBeEdited;
    }

    @Override
    public void animateShow() {

    }

    protected final int[] mButtonUnder = new int[]{R.id.zero_under, R.id.one_under, R.id.two_under, R.id.three_under,
            R.id.four_under, R.id.five_under, R.id.six_under, R.id.seven_under, R.id.eight_under, R.id.nine_under,
            R.id.star_under,
            R.id.pound_under};

    @Override
    protected void setupKeypad() {
        final int[] letterIds = new int[]{
                R.string.dialpad_0_letters,
                R.string.dialpad_1_letters,
                R.string.dialpad_2_letters,
                R.string.dialpad_3_letters,
                R.string.dialpad_4_letters,
                R.string.dialpad_5_letters,
                R.string.dialpad_6_letters,
                R.string.dialpad_7_letters,
                R.string.dialpad_8_letters,
                R.string.dialpad_9_letters,
                R.string.dialpad_star_letters,
                R.string.dialpad_pound_letters
        };

        int[] buttonId = findViewById(R.id.zero_under) != null ? mButtonUnder : mButtonIds;

        final Resources resources = getContext().getResources();

        BtalkDialpadKeyButton dialpadKey;
        TextView numberView, descriptionView;

        final Locale currentLocale = resources.getConfiguration().locale;
        final NumberFormat nf;
        // We translate dialpad numbers only for "fa" and not any other locale
        // ("ar" anybody ?).
        if ("fa".equals(currentLocale.getLanguage())) {
            nf = DecimalFormat.getInstance(resources.getConfiguration().locale);
        } else {
            nf = DecimalFormat.getInstance(Locale.ENGLISH);
        }

        for (int i = 0; i < mButtonIds.length; i++) {
            dialpadKey = (BtalkDialpadKeyButton)findViewById(buttonId[i]);
            numberView = (TextView) dialpadKey.findViewById(R.id.dialpad_key_number);
            descriptionView = dialpadKey.findViewById(R.id.text_diapad_key);

            final String numberString;
            final CharSequence numberContentDescription;
            if (buttonId[i] == buttonId[11]) {
                numberString = resources.getString(R.string.dialpad_pound_number);
                numberContentDescription = numberString;
            } else if (buttonId[i] == buttonId[10]) {
                numberString = resources.getString(R.string.dialpad_star_number);
                numberContentDescription = numberString;
            } else {
                numberString = nf.format(i);
                // The content description is used for Talkback key presses. The number is
                // separated by a "," to introduce a slight delay. Convert letters into a verbatim
                // span so that they are read as letters instead of as one word.
                String letters = resources.getString(letterIds[i]);
                Spannable spannable =
                        Spannable.Factory.getInstance().newSpannable(numberString + "," + letters);
                spannable.setSpan(
                        (new TtsSpan.VerbatimBuilder(letters)).build(),
                        numberString.length() + 1,
                        numberString.length() + 1 + letters.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                numberContentDescription = spannable;
            }
            if (resources.getString(R.string.dialpad_star_number).equals(numberString)) {
                numberView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dialpad_star, 0, 0, 0);
            }else {
                numberView.setText(numberString);
            }
            numberView.setElegantTextHeight(false);
            setNumberViewTypeface(numberView); // Bkav TrungTH them vao de set font chu
            dialpadKey.setContentDescription(numberContentDescription);
            // Bkav TienNAb: set text duoi moi phim so
            if (R.string.dialpad_pound_letters == letterIds[i]) {
                descriptionView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dialpad_semicolon, 0, 0, 0);
            }else {
                descriptionView.setText(letterIds[i]);
            }

        }

        final BtalkDialpadKeyButton one = (BtalkDialpadKeyButton)findViewById(buttonId[9]);
        one.setLongHoverContentDescription(
                resources.getText(R.string.description_voicemail_button));

        final BtalkDialpadKeyButton zero = (BtalkDialpadKeyButton)findViewById(buttonId[10]);
        zero.setLongHoverContentDescription(
                resources.getText(R.string.description_image_button_plus));

    }

    private boolean navigationBarIsVisible() {
        Display d = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth > displayWidth) || (realHeight > displayHeight);
    }
}
