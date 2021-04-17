package bkav.android.btalk.messaging.util;

import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.provider.ContactsContract;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.PhoneNumberUtils;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.android.contacts.editor.ContactEditorFragment;
import com.android.messaging.Factory;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.UriUtil;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.messaging.ui.conversation.BtalkClickMailDialog;
import bkav.android.btalk.messaging.ui.conversation.BtalkClickPhoneDialog;

/**
 * Created by quangnd on 05/06/2017.
 * Bkav QuangNDb class xu ly link co trong noi dung tin nhan
 */

public class BtalkLinkify {

    /**
     * Bit field indicating that web URLs should be matched in methods that
     * take an options mask
     */
    public static final int WEB_URLS = 0x01;

    /**
     * Bit field indicating that email addresses should be matched in methods
     * that take an options mask
     */
    public static final int EMAIL_ADDRESSES = 0x02;

    /**
     * Bit field indicating that phone numbers should be matched in methods that
     * take an options mask
     */
    public static final int PHONE_NUMBERS = 0x04;

    /**
     * Bit field indicating that street addresses should be matched in methods that
     * take an options mask. Note that this uses the
     * {@link android.webkit.WebView#findAddress(String) findAddress()} method in
     * {@link android.webkit.WebView} for finding addresses, which has various
     * limitations.
     */
    public static final int MAP_ADDRESSES = 0x08;

    /**
     * Bit mask indicating that all available patterns should be matched in
     * methods that take an options mask
     */
    public static final int ALL = WEB_URLS | EMAIL_ADDRESSES | PHONE_NUMBERS | MAP_ADDRESSES;

    /**
     * Don't treat anything with fewer than this many digits as a
     * phone number.
     */
    private static final int PHONE_NUMBER_MINIMUM_DIGITS = 5;

    /**
     * @hide
     */
    @IntDef(flag = true, value = {WEB_URLS, EMAIL_ADDRESSES, PHONE_NUMBERS, MAP_ADDRESSES, ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LinkifyMask {

    }

    /**
     * Filters out web URL matches that occur after an at-sign (@).  This is
     * to prevent turning the domain name in an email address into a web link.
     */
    public static final Linkify.MatchFilter sUrlMatchFilter = new Linkify.MatchFilter() {
        public final boolean acceptMatch(CharSequence s, int start, int end) {
            if (start == 0) {
                return true;
            }

            if (s.charAt(start - 1) == '@') {
                return false;
            }

            return true;
        }
    };

    /**
     * Filters out URL matches that don't have enough digits to be a
     * phone number.
     */
    public static final Linkify.MatchFilter sPhoneNumberMatchFilter = new Linkify.MatchFilter() {
        public final boolean acceptMatch(CharSequence s, int start, int end) {
            int digitCount = 0;

            for (int i = start; i < end; i++) {
                if (Character.isDigit(s.charAt(i))) {
                    digitCount++;
                    if (digitCount >= PHONE_NUMBER_MINIMUM_DIGITS) {
                        return true;
                    }
                }
            }
            return false;
        }
    };

    /**
     * Transforms matched phone number text into something suitable
     * to be used in a tel: URL.  It does this by removing everything
     * but the digits and plus signs.  For instance:
     * &apos;+1 (919) 555-1212&apos;
     * becomes &apos;+19195551212&apos;
     */
    public static final Linkify.TransformFilter sPhoneNumberTransformFilter = new Linkify.TransformFilter() {
        public final String transformUrl(final Matcher match, String url) {
            return Patterns.digitsAndPlusOnly(match);
        }
    };

    /**
     * MatchFilter enables client code to have more control over
     * what is allowed to match and become a link, and what is not.
     * <p>
     * For example:  when matching web URLs you would like things like
     * http://www.example.com to match, as well as just example.com itelf.
     * However, you would not want to match against the domain in
     * support@example.com.  So, when matching against a web URL pattern you
     * might also include a MatchFilter that disallows the match if it is
     * immediately preceded by an at-sign (@).
     */
    public interface MatchFilter {

        /**
         * Examines the character span matched by the pattern and determines
         * if the match should be turned into an actionable link.
         *
         * @param s     The body of text against which the pattern
         *              was matched
         * @param start The index of the first character in s that was
         *              matched by the pattern - inclusive
         * @param end   The index of the last character in s that was
         *              matched - exclusive
         * @return Whether this match should be turned into a link
         */
        boolean acceptMatch(CharSequence s, int start, int end);
    }

    /**
     * TransformFilter enables client code to have more control over
     * how matched patterns are represented as URLs.
     * <p>
     * For example:  when converting a phone number such as (919)  555-1212
     * into a tel: URL the parentheses, white space, and hyphen need to be
     * removed to produce tel:9195551212.
     */
    public interface TransformFilter {

        /**
         * Examines the matched text and either passes it through or uses the
         * data in the Matcher state to produce a replacement.
         *
         * @param match The regex matcher state that found this URL text
         * @param url   The text that was matched
         * @return The transformed form of the URL
         */
        String transformUrl(final Matcher match, String url);
    }

    /**
     * Scans the text of the provided Spannable and turns all occurrences
     * of the link types indicated in the mask into clickable links.
     * If the mask is nonzero, it also removes any existing URLSpans
     * attached to the Spannable, to avoid problems if you call it
     * repeatedly on the same text.
     *
     * @param text Spannable whose text is to be marked-up with links
     * @param mask Mask to define which kinds of links will be searched.
     * @return True if at least one link is found and applied.
     */
    public static final boolean addLinks(@NonNull Spannable text, @LinkifyMask int mask, FragmentManager fragmentManager) {
        if (mask == 0) {
            return false;
        }

        URLSpan[] old = text.getSpans(0, text.length(), URLSpan.class);

        for (int i = old.length - 1; i >= 0; i--) {
            text.removeSpan(old[i]);
        }

        ArrayList<LinkSpec> links = new ArrayList<LinkSpec>();

        if ((mask & WEB_URLS) != 0) {
            gatherLinks(links, text, Patterns.WEB_URL,
                    new String[]{"http://", "https://", "rtsp://"},
                    sUrlMatchFilter, null);
        }

        if ((mask & EMAIL_ADDRESSES) != 0) {
            gatherLinks(links, text, Patterns.EMAIL_ADDRESS,
                    new String[]{"mailto:"},
                    null, null);
        }

        if ((mask & PHONE_NUMBERS) != 0) {
            gatherTelLinks(links, text);
        }

        if ((mask & MAP_ADDRESSES) != 0) {
            gatherMapLinks(links, text);
        }

        // Bkav HuyNQN them loc ma otp
        if ((mask & PHONE_NUMBERS) != 0) {
            gatherOtplLinks(links, text);
        }

        pruneOverlaps(links);

        if (links.size() == 0) {
            return false;
        }

        for (LinkSpec link : links) {
            if (link.isPhoneNumber) {
                applyPhoneNumberLink(link.url, link.start, link.end, text, fragmentManager);
            } else if (link.isMailAddress) {
                applyEmailLink(link.url, link.start, link.end, text, fragmentManager);
            } else if (link.isOTP) {
                applyOtpNumberLink(link.url, link.start, link.end, text, fragmentManager);
            } else {
                applyLink(link.url, link.start, link.end, text);
            }
        }

        return true;
    }

    private FragmentManager mFragmentManager;

    /**
     * Scans the text of the provided TextView and turns all occurrences of
     * the link types indicated in the mask into clickable links.  If matches
     * are found the movement method for the TextView is set to
     * LinkMovementMethod.
     *
     * @param text TextView whose text is to be marked-up with links
     * @param mask Mask to define which kinds of links will be searched.
     * @return True if at least one link is found and applied.
     */
    public static final boolean addLinks(@NonNull TextView text, @LinkifyMask int mask, FragmentManager fragmentManager) {
        if (mask == 0) {
            return false;
        }

        CharSequence t = text.getText();

        if (t instanceof Spannable) {
            if (addLinks((Spannable) t, mask, fragmentManager)) {
                addLinkMovementMethod(text);
                return true;
            }

            return false;
        } else {
            SpannableString s = SpannableString.valueOf(t);

            if (addLinks(s, mask, fragmentManager)) {
                addLinkMovementMethod(text);
                text.setText(s);

                return true;
            }

            return false;
        }
    }

    private static final void addLinkMovementMethod(@NonNull TextView t) {
        MovementMethod m = t.getMovementMethod();

        if ((m == null) || !(m instanceof LinkMovementMethod)) {
            if (t.getLinksClickable()) {
                t.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    /**
     * Applies a regex to the text of a TextView turning the matches into
     * links.  If links are found then UrlSpans are applied to the link
     * text match areas, and the movement method for the text is changed
     * to LinkMovementMethod.
     *
     * @param text    TextView whose text is to be marked-up with links
     * @param pattern Regex pattern to be used for finding links
     * @param scheme  URL scheme string (eg <code>http://</code>) to be
     *                prepended to the links that do not start with this scheme.
     */
    public static final void addLinks(@NonNull TextView text, @NonNull Pattern pattern,
                                      @Nullable String scheme) {
        addLinks(text, pattern, scheme, null, null, null);
    }

    /**
     * Applies a regex to the text of a TextView turning the matches into
     * links.  If links are found then UrlSpans are applied to the link
     * text match areas, and the movement method for the text is changed
     * to LinkMovementMethod.
     *
     * @param text        TextView whose text is to be marked-up with links
     * @param pattern     Regex pattern to be used for finding links
     * @param scheme      URL scheme string (eg <code>http://</code>) to be
     *                    prepended to the links that do not start with this scheme.
     * @param matchFilter The filter that is used to allow the client code
     *                    additional control over which pattern matches are
     *                    to be converted into links.
     */
    public static final void addLinks(@NonNull TextView text, @NonNull Pattern pattern,
                                      @Nullable String scheme, @Nullable Linkify.MatchFilter matchFilter,
                                      @Nullable Linkify.TransformFilter transformFilter) {
        addLinks(text, pattern, scheme, null, matchFilter, transformFilter);
    }

    /**
     * Applies a regex to the text of a TextView turning the matches into
     * links.  If links are found then UrlSpans are applied to the link
     * text match areas, and the movement method for the text is changed
     * to LinkMovementMethod.
     *
     * @param text            TextView whose text is to be marked-up with links.
     * @param pattern         Regex pattern to be used for finding links.
     * @param defaultScheme   The default scheme to be prepended to links if the link does not
     *                        start with one of the <code>schemes</code> given.
     * @param schemes         Array of schemes (eg <code>http://</code>) to check if the link found
     *                        contains a scheme. Passing a null or empty value means prepend defaultScheme
     *                        to all links.
     * @param matchFilter     The filter that is used to allow the client code additional control
     *                        over which pattern matches are to be converted into links.
     * @param transformFilter Filter to allow the client code to update the link found.
     */
    public static final void addLinks(@NonNull TextView text, @NonNull Pattern pattern,
                                      @Nullable String defaultScheme, @Nullable String[] schemes,
                                      @Nullable Linkify.MatchFilter matchFilter, @Nullable Linkify.TransformFilter transformFilter) {
        SpannableString spannable = SpannableString.valueOf(text.getText());

        boolean linksAdded = addLinks(spannable, pattern, defaultScheme, schemes, matchFilter,
                transformFilter);
        if (linksAdded) {
            text.setText(spannable);
            addLinkMovementMethod(text);
        }
    }

    /**
     * Applies a regex to a Spannable turning the matches into
     * links.
     *
     * @param text    Spannable whose text is to be marked-up with links
     * @param pattern Regex pattern to be used for finding links
     * @param scheme  URL scheme string (eg <code>http://</code>) to be
     *                prepended to the links that do not start with this scheme.
     */
    public static final boolean addLinks(@NonNull Spannable text, @NonNull Pattern pattern,
                                         @Nullable String scheme) {
        return addLinks(text, pattern, scheme, null, null, null);
    }

    /**
     * Applies a regex to a Spannable turning the matches into
     * links.
     *
     * @param spannable       Spannable whose text is to be marked-up with links
     * @param pattern         Regex pattern to be used for finding links
     * @param scheme          URL scheme string (eg <code>http://</code>) to be
     *                        prepended to the links that do not start with this scheme.
     * @param matchFilter     The filter that is used to allow the client code
     *                        additional control over which pattern matches are
     *                        to be converted into links.
     * @param transformFilter Filter to allow the client code to update the link found.
     * @return True if at least one link is found and applied.
     */
    public static final boolean addLinks(@NonNull Spannable spannable, @NonNull Pattern pattern,
                                         @Nullable String scheme, @Nullable Linkify.MatchFilter matchFilter,
                                         @Nullable Linkify.TransformFilter transformFilter) {
        return addLinks(spannable, pattern, scheme, null, matchFilter,
                transformFilter);
    }

    /**
     * Applies a regex to a Spannable turning the matches into links.
     *
     * @param spannable       Spannable whose text is to be marked-up with links.
     * @param pattern         Regex pattern to be used for finding links.
     * @param defaultScheme   The default scheme to be prepended to links if the link does not
     *                        start with one of the <code>schemes</code> given.
     * @param schemes         Array of schemes (eg <code>http://</code>) to check if the link found
     *                        contains a scheme. Passing a null or empty value means prepend defaultScheme
     *                        to all links.
     * @param matchFilter     The filter that is used to allow the client code additional control
     *                        over which pattern matches are to be converted into links.
     * @param transformFilter Filter to allow the client code to update the link found.
     * @return True if at least one link is found and applied.
     */
    public static final boolean addLinks(@NonNull Spannable spannable, @NonNull Pattern pattern,
                                         @Nullable String defaultScheme, @Nullable String[] schemes,
                                         @Nullable Linkify.MatchFilter matchFilter, @Nullable Linkify.TransformFilter transformFilter) {
        final String[] schemesCopy;
        if (defaultScheme == null) defaultScheme = "";
        if (schemes == null || schemes.length < 1) {
            schemes = new String[0];
        }

        schemesCopy = new String[schemes.length + 1];
        schemesCopy[0] = defaultScheme.toLowerCase(Locale.ROOT);
        for (int index = 0; index < schemes.length; index++) {
            String scheme = schemes[index];
            schemesCopy[index + 1] = (scheme == null) ? "" : scheme.toLowerCase(Locale.ROOT);
        }

        boolean hasMatches = false;
        Matcher m = pattern.matcher(spannable);

        while (m.find()) {
            int start = m.start();
            int end = m.end();
            boolean allowed = true;

            if (matchFilter != null) {
                allowed = matchFilter.acceptMatch(spannable, start, end);
            }

            if (allowed) {
                String url = makeUrl(m.group(0), schemesCopy, m, transformFilter);

                applyLink(url, start, end, spannable);
                hasMatches = true;
            }
        }

        return hasMatches;
    }

    private static void applyLink(String url, int start, int end, Spannable text) {
        URLSpan span = new URLSpan(url) {
            @Override
            public void onClick(View widget) {
                Uri uri = Uri.parse(getURL());
                Context context = widget.getContext();
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//Bkav QuangNDb them co nay de k loi cho api <23
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.w("URLSpan", "Actvity was not found for intent, " + intent.toString());
                }
            }
        };

        text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void applyEmailLink(final String url, int start, int end, Spannable text, final FragmentManager fragmentManager) {
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String mailAddr = url.replace("mailto:", "");
                final Context context = Factory.get().getApplicationContext();
                final BtalkClickMailDialog clickMailDialog = BtalkClickMailDialog.newInstance(mailAddr);
                clickMailDialog.setListener(new BtalkClickMailDialog.OnOptionClickListener() {
                    @Override
                    public void onSendMail(String mail) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + mail));
                        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(emailIntent);
                        clickMailDialog.dismiss();
                    }

                    @Override
                    public void onCopy(String mail) {
                        final ClipboardManager clipboard = (ClipboardManager) context
                                .getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setPrimaryClip(
                                ClipData.newPlainText(null /* label */, mail));
                        clickMailDialog.dismiss();
                    }
                });
                clickMailDialog.show(fragmentManager, "SHOW");
            }
        };
        text.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void applyPhoneNumberLink(final String url, int start, int end, Spannable text, final FragmentManager fragmentManager) {
        // Bkav QuangNDb sua khi click vao so dien thoai thi hien thi dialog option
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                textView.setContentDescription("clickLink");
                String phoneNum = url.replace("tel:", "");
                String name = getNameFromPhoneNum(phoneNum);
                final BtalkClickPhoneDialog clickPhoneDialog = BtalkClickPhoneDialog.newInstance(name, phoneNum);
                clickPhoneDialog.setListener(new BtalkClickPhoneDialog.OnOptionClickListener() {
                    Context context = Factory.get().getApplicationContext();

                    @Override
                    public void onCall(String phone) {
                        // Bkav HuyNQN su dung logic chon esim
                        UIIntents.get().makeACall(context, fragmentManager,phone);
//                        final Intent intent = new Intent(Intent.ACTION_CALL,
//                                Uri.parse(UriUtil.SCHEME_TEL + phone));
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        context.startActivity(intent);
                        clickPhoneDialog.dismiss();
                    }

                    @Override
                    public void onSendMessage(String phone) {
                        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phone, null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setPackage(Factory.get().getApplicationContext().getPackageName());
                        context.startActivity(intent);
                        clickPhoneDialog.dismiss();
                    }

                    @Override
                    public void onAddContact(String phone) {
                        final Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                        intent.setPackage(Factory.get().getApplicationContext().getPackageName());
                        intent.putExtra(ContactEditorFragment.INTENT_EXTRA_DISABLE_DELETE_MENU_OPTION,
                                true);
                        // Anhdts them vao
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        clickPhoneDialog.dismiss();
                    }

                    @Override
                    public void onCopy(String phone) {
                        final ClipboardManager clipboard = (ClipboardManager) context
                                .getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setPrimaryClip(
                                ClipData.newPlainText(null /* label */, phone));
                        clickPhoneDialog.dismiss();
                    }

                    @Override
                    public void onOpenDigitPad(String phone) {
                        Bundle data = new Bundle();
                        data.putString(BtalkActivity.ARGUMENT_NUMBER, phone);
                        Intent intent = new Intent(BtalkActivity.ACTION_FIX_BEFORE_CALL);
                        intent.putExtras(data);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        clickPhoneDialog.dismiss();
                    }
                });
                clickPhoneDialog.show(fragmentManager, "SHOW");
            }
        };
        text.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static String getNameFromPhoneNum(String phoneNum) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNum));
        String displayName;
        Cursor cursor = null;
        try {
            cursor = Factory.get().getApplicationContext().getContentResolver().query(uri,
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                return displayName;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    private static final String makeUrl(@NonNull String url, @NonNull String[] prefixes,
                                        Matcher matcher, @Nullable Linkify.TransformFilter filter) {
        if (filter != null) {
            url = filter.transformUrl(matcher, url);
        }

        boolean hasPrefix = false;

        for (int i = 0; i < prefixes.length; i++) {
            if (url.regionMatches(true, 0, prefixes[i], 0, prefixes[i].length())) {
                hasPrefix = true;

                // Fix capitalization if necessary
                if (!url.regionMatches(false, 0, prefixes[i], 0, prefixes[i].length())) {
                    url = prefixes[i] + url.substring(prefixes[i].length());
                }

                break;
            }
        }

        if (!hasPrefix && prefixes.length > 0) {
            url = prefixes[0] + url;
        }

        return url;
    }

    private static final void gatherLinks(ArrayList<LinkSpec> links,
                                          Spannable s, Pattern pattern, String[] schemes,
                                          Linkify.MatchFilter matchFilter, Linkify.TransformFilter transformFilter) {
        Matcher m = pattern.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();

            if (matchFilter == null || matchFilter.acceptMatch(s, start, end)) {
                LinkSpec spec = new LinkSpec();
                String url = makeUrl(m.group(0), schemes, m, transformFilter);

                spec.url = url;
                spec.start = start;
                spec.end = end;
                if (schemes[0].equals("mailto:")) {
                    spec.isMailAddress = true;
                }

                links.add(spec);
            }
        }
    }

    private static final void gatherTelLinks(ArrayList<LinkSpec> links, Spannable s) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Iterable<PhoneNumberMatch> matches = phoneUtil.findNumbers(s.toString(),
                Locale.JAPAN.getCountry(), PhoneNumberUtil.Leniency.POSSIBLE, Long.MAX_VALUE);
        for (PhoneNumberMatch match : matches) {
            LinkSpec spec = new LinkSpec();
            spec.url = "tel:" + PhoneNumberUtils.normalizeNumber(match.rawString());
            spec.start = match.start();
            spec.end = match.end();
            spec.isPhoneNumber = true;
            links.add(spec);
        }
    }

    // Bkav HuyNQN thuc hien xac dinh cac ma otp co trong sms
    public static void ckeckOtp(String s, ArrayList<LinkSpec> links, int end) {
        Stack<Character> stackToken = new Stack<>();
        boolean flag = false; // Bkav HuyNQN them co loai bo cac tu co chua ca so va chu cai
        char[] token;
        char[] charArray = s.toCharArray();
        for (char c : charArray) {
            if (checkNumber(c) == 1) {
                stackToken.add(c);
            }else {
                flag = true;
                break;
            }
        }

        if (stackToken.size() >= 4 && !flag) { // Bkav HuyNQN chi lay cac chuoi co do dai tu 4 tro len va ko lay chu
            token = new char[stackToken.size()];
            for (int i = stackToken.size() - 1; i >= 0; i--) {
                token[i] = stackToken.pop();
            }

            LinkSpec spec = new LinkSpec();
            spec.url = "tel:" + String.valueOf(token);
            spec.start = end - s.length() - 1;
            spec.end = end - 1;
            spec.isOTP = true;
            links.add(spec);
        }
    }

    private static int checkNumber(char a) {
        switch (a) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return 1;
            default:
                return 0;
        }
    }

    // Bkav HuyNQN kiem tra cac ma otp trong tin nhan
    private static void gatherOtplLinks(ArrayList<LinkSpec> links, Spannable text) {
        StringTokenizer token = new StringTokenizer(text.toString());
        int end = 0;
        while (token.hasMoreTokens()) {
            String s = token.nextToken();
            end = s.length() +1 +end;
            ckeckOtp(s, links, end);
        }
    }

    private static void applyOtpNumberLink(final String url, int start, int end, Spannable text, final FragmentManager fragmentManager) {
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                textView.setContentDescription("clickLink");
                String phoneNum = url.replace("tel:", "");
                String name = getNameFromPhoneNum(phoneNum);
                final BtalkClickPhoneDialog clickPhoneDialog = BtalkClickPhoneDialog.newInstance(name, phoneNum);
                clickPhoneDialog.setListener(new BtalkClickPhoneDialog.OnOptionClickListener() {
                    Context context = Factory.get().getApplicationContext();

                    @Override
                    public void onCall(String phone) {
                        // Bkav HuyNQN su dung logic chon Esim
                        UIIntents.get().makeACall(context, fragmentManager, phone);
//                        final Intent intent = new Intent(Intent.ACTION_CALL,
//                                Uri.parse(UriUtil.SCHEME_TEL + phone));
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        context.startActivity(intent);
                        clickPhoneDialog.dismiss();
                    }

                    @Override
                    public void onSendMessage(String phone) {
                        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phone, null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setPackage(Factory.get().getApplicationContext().getPackageName());
                        context.startActivity(intent);
                        clickPhoneDialog.dismiss();
                    }

                    @Override
                    public void onAddContact(String phone) {
                        final Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                        intent.setPackage(Factory.get().getApplicationContext().getPackageName());
                        intent.putExtra(ContactEditorFragment.INTENT_EXTRA_DISABLE_DELETE_MENU_OPTION,
                                true);
                        // Anhdts them vao
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        clickPhoneDialog.dismiss();
                    }

                    @Override
                    public void onCopy(String phone) {
                        final ClipboardManager clipboard = (ClipboardManager) context
                                .getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setPrimaryClip(
                                ClipData.newPlainText(null /* label */, phone));
                        clickPhoneDialog.dismiss();
                    }

                    @Override
                    public void onOpenDigitPad(String phone) {
                        Bundle data = new Bundle();
                        data.putString(BtalkActivity.ARGUMENT_NUMBER, phone);
                        Intent intent = new Intent(BtalkActivity.ACTION_FIX_BEFORE_CALL);
                        intent.putExtras(data);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        clickPhoneDialog.dismiss();
                    }
                });
                clickPhoneDialog.show(fragmentManager, "SHOW");
            }
        };
        text.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }


    private static final void gatherMapLinks(ArrayList<LinkSpec> links, Spannable s) {
        String string = s.toString();
        String address;
        int base = 0;
        try {
            while ((address = WebView.findAddress(string)) != null) {
                int start = string.indexOf(address);

                if (start < 0) {
                    break;
                }

                LinkSpec spec = new LinkSpec();
                int length = address.length();
                int end = start + length;

                spec.start = base + start;
                spec.end = base + end;
                string = string.substring(end);
                base += end;

                String encodedAddress = null;

                try {
                    encodedAddress = URLEncoder.encode(address, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    continue;
                }

                spec.url = "geo:0,0?q=" + encodedAddress;
                links.add(spec);
            }
        } catch (UnsupportedOperationException e) {
            // findAddress may fail with an unsupported exception on platforms without a WebView.
            // In this case, we will not append anything to the links variable: it would have died
            // in WebView.findAddress.
            return;
        }
    }

    private static final void pruneOverlaps(ArrayList<LinkSpec> links) {
        Comparator<LinkSpec> c = new Comparator<LinkSpec>() {
            public final int compare(LinkSpec a, LinkSpec b) {
                if (a.start < b.start) {
                    return -1;
                }

                if (a.start > b.start) {
                    return 1;
                }

                if (a.end < b.end) {
                    return 1;
                }

                if (a.end > b.end) {
                    return -1;
                }

                return 0;
            }
        };

        Collections.sort(links, c);

        int len = links.size();
        int i = 0;

        while (i < len - 1) {
            LinkSpec a = links.get(i);
            LinkSpec b = links.get(i + 1);
            int remove = -1;

            if ((a.start <= b.start) && (a.end > b.start)) {
                if (b.end <= a.end) {
                    remove = i + 1;
                } else if ((a.end - a.start) > (b.end - b.start)) {
                    remove = i + 1;
                } else if ((a.end - a.start) < (b.end - b.start)) {
                    remove = i;
                }

                if (remove != -1) {
                    links.remove(remove);
                    len--;
                    continue;
                }

            }

            i++;
        }
    }
}

class LinkSpec {

    String url;
    int start;
    int end;
    boolean isPhoneNumber = false;
    boolean isMailAddress = false;
    boolean isOTP = false; // Bkav HuyNQN check ma otp
}
