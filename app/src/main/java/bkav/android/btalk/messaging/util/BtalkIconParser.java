package bkav.android.btalk.messaging.util;

import android.text.Spannable;
import android.text.SpannableStringBuilder;

import com.android.messaging.Factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.ui.mediapicker.emoticon.EmojiSpan;

/**
 * BKav QuangNDb
 * Copy tu btalk cu sang, dung de match icon voi text
 */
public class BtalkIconParser {

    // Singleton stuff
    private static BtalkIconParser sInstance;

    public static BtalkIconParser getInstance() {
        if (sInstance == null) {
            sInstance = new BtalkIconParser();
        }
        return sInstance;
    }

    private final String[] mSmileyTexts;

    private final Pattern mPattern;

    private final HashMap<String, Integer> mSmileyToRes;

    private BtalkIconParser() {
        mSmileyTexts = getSmileyTextsUnicode();
        mSmileyToRes = buildSmileyToRes();
        mPattern = buildPattern();
    }

    private String[] getSmileyTextsUnicode() {
        final int[] listTextEmoticon = Factory.get().getApplicationContext().getResources().getIntArray(R.array.btalk_list_unicode_emoji);
        final int size = listTextEmoticon.length;
        final String[] listTextUnicode = new String[size];
        for (int i = 0; i < size; i++) {
            listTextUnicode[i] = new String(Character.toChars(listTextEmoticon[i]));
        }
        return listTextUnicode;
    }

    static class Smileys {

        private static final int[] sIconIds = {
                R.drawable.emoji1, //0
                R.drawable.emoji2, //1
                R.drawable.emoji3, //2
                R.drawable.emoji4, //3
                R.drawable.emoji5, //4
                R.drawable.emoji6, //5
                R.drawable.emoji7, //6
                R.drawable.emoji8, //7
                R.drawable.emoji9, //8
                R.drawable.emoji10, //9
                R.drawable.emoji11, //10
                R.drawable.emoji12, //11
                R.drawable.emoji13, //12
                R.drawable.emoji14, //13
                R.drawable.emoji15, //14
                R.drawable.emoji16, //15
                R.drawable.emoji17, //16
                R.drawable.emoji18, //17
                R.drawable.emoji19, //18
                R.drawable.emoji20, //19
                R.drawable.emoji21, //20
                R.drawable.emoji22, //21
                R.drawable.emoji23, //22
                R.drawable.emoji24, //23
                R.drawable.emoji25, //24
                R.drawable.emoji26, //25
                R.drawable.emoji27, //26
                R.drawable.emoji28, //27
                R.drawable.emoji29, //28
                R.drawable.emoji30, //29
                R.drawable.emoji31, //29
                R.drawable.emoji32, //29
                R.drawable.emoji33, //29
                R.drawable.emoji34, //29
                R.drawable.emoji35, //29
                R.drawable.emoji36, //29
                R.drawable.emoji37, //29
                R.drawable.emoji38, //29
                R.drawable.emoji39, //29
                R.drawable.emoji40, //29
                R.drawable.emoji41, //29
                R.drawable.emoji42, //29
                R.drawable.emoji43, //29
                R.drawable.emoji44, //29
                R.drawable.emoji45, //29
                R.drawable.emoji46, //29
                R.drawable.emoji47, //29
                R.drawable.emoji48, //29
                R.drawable.emoji49, //29
                R.drawable.emoji50, //29
                R.drawable.emoji51, //29
                R.drawable.emoji52, //29
                R.drawable.emoji53, //29
                R.drawable.emoji54, //29
                R.drawable.emoji55, //29
                R.drawable.emoji56, //29
                R.drawable.emoji57, //29
        };


        // QuyetDV: Mang cac icon smiley Quick Reply
        private static final int[] sIconSmileyQuickReply = {
//                R.drawable.emo_im_happy_small, //0
//                R.drawable.emo_im_sad_small, //1
//                R.drawable.emo_im_big_grin_small, //2
//                R.drawable.emo_im_winking_small, //3
//                R.drawable.emo_im_love_struck_small, //4
//                R.drawable.emo_im_kiss_small, //5
//                R.drawable.emo_im_blushing_small, //6
//                R.drawable.emo_im_angry_small, //7
        };

        public static int EMOJI1 = 0;

        public static int EMOJI2 = 1;

        public static int EMOJI3 = 2;

        public static int EMOJI4 = 3;

        public static int EMOJI5 = 4;

        public static int EMOJI6 = 5;

        public static int EMOJI7 = 6;

        public static int EMOJI8 = 7;

        public static int EMOJI9 = 8;

        public static int EMOJI10 = 9;

        public static int EMOJI11 = 10;

        public static int EMOJI12 = 11;

        public static int EMOJI13 = 12;

        public static int EMOJI14 = 13;

        public static int EMOJI15 = 14;

        public static int EMOJI16 = 15;

        public static int EMOJI17 = 16;

        public static int EMOJI18 = 17;

        public static int EMOJI19 = 18;

        public static int EMOJI20 = 19;

        public static int EMOJI21 = 20;

        public static int EMOJI22 = 21;

        public static int EMOJI23 = 22;

        public static int EMOJI24 = 23;

        public static int EMOJI25 = 24;

        public static int EMOJI26 = 25;

        public static int EMOJI27 = 26;

        public static int EMOJI28 = 27;

        public static int EMOJI29 = 28;

        public static int EMOJI30 = 29;
        public static int EMOJI31 = 30;
        public static int EMOJI132 = 31;
        public static int EMOJI33 = 32;
        public static int EMOJI134 = 33;
        public static int EMOJI135 = 34;
        public static int EMOJI136 = 35;
        public static int EMOJI37 = 36;
        public static int EMOJI38 = 37;
        public static int EMOJI39 = 38;
        public static int EMOJI40 = 39;
        public static int EMOJI41 = 40;
        public static int EMOJI42 = 41;
        public static int EMOJI43 = 42;
        public static int EMOJI44 = 43;
        public static int EMOJI45 = 44;
        public static int EMOJI46 = 45;
        public static int EMOJI47 = 46;
        public static int EMOJI48 = 47;
        public static int EMOJI49 = 48;
        public static int EMOJI50 = 49;
        public static int EMOJI51 = 50;
        public static int EMOJI52 = 51;
        public static int EMOJI53 = 52;
        public static int EMOJI54 = 53;
        public static int EMOJI55 = 54;
        public static int EMOJI56 = 55;
        public static int EMOJI57 = 56;

        public static int getSmileyResource(int which) {
            return sIconIds[which];
        }

        public static int getSmileyQuickReplyResource(int which) {
            return sIconSmileyQuickReply[which];
        }
    }


    // NOTE: if you change anything about this array, you must make the
    // corresponding change
    // to the string arrays: default_smiley_texts and default_smiley_names in
    // res/values/arrays.xml
    public static final int[] DEFAULT_SMILEY_RES_IDS = {
            Smileys.getSmileyResource(Smileys.EMOJI1), // 0
            Smileys.getSmileyResource(Smileys.EMOJI2), // 1
            Smileys.getSmileyResource(Smileys.EMOJI3), // 2
            Smileys.getSmileyResource(Smileys.EMOJI4), // 3
            Smileys.getSmileyResource(Smileys.EMOJI5), // 4
            Smileys.getSmileyResource(Smileys.EMOJI6), // 5
            Smileys.getSmileyResource(Smileys.EMOJI7), // 6
            Smileys.getSmileyResource(Smileys.EMOJI8), // 7
            Smileys.getSmileyResource(Smileys.EMOJI9), // 8
            Smileys.getSmileyResource(Smileys.EMOJI10), // 9
            Smileys.getSmileyResource(Smileys.EMOJI11), // 10
            Smileys.getSmileyResource(Smileys.EMOJI12), // 11
            Smileys.getSmileyResource(Smileys.EMOJI13), // 12
            Smileys.getSmileyResource(Smileys.EMOJI14), // 13
            Smileys.getSmileyResource(Smileys.EMOJI15), // 14
            Smileys.getSmileyResource(Smileys.EMOJI16), // 15
            Smileys.getSmileyResource(Smileys.EMOJI17), // 16
            Smileys.getSmileyResource(Smileys.EMOJI18), // 17
            Smileys.getSmileyResource(Smileys.EMOJI19), // 18
            Smileys.getSmileyResource(Smileys.EMOJI20), // 19
            Smileys.getSmileyResource(Smileys.EMOJI21), // 20
            Smileys.getSmileyResource(Smileys.EMOJI22), // 21
            Smileys.getSmileyResource(Smileys.EMOJI23), // 22
            Smileys.getSmileyResource(Smileys.EMOJI24), // 23
            Smileys.getSmileyResource(Smileys.EMOJI25), // 24
            Smileys.getSmileyResource(Smileys.EMOJI26), // 25
            Smileys.getSmileyResource(Smileys.EMOJI27), // 26
            Smileys.getSmileyResource(Smileys.EMOJI28), // 27
            Smileys.getSmileyResource(Smileys.EMOJI29), // 28
            Smileys.getSmileyResource(Smileys.EMOJI30), // 29
            Smileys.getSmileyResource(Smileys.EMOJI31), // 29
            Smileys.getSmileyResource(Smileys.EMOJI132), // 29
            Smileys.getSmileyResource(Smileys.EMOJI33), // 29
            Smileys.getSmileyResource(Smileys.EMOJI134), // 29
            Smileys.getSmileyResource(Smileys.EMOJI135), // 29
            Smileys.getSmileyResource(Smileys.EMOJI136), // 29
            Smileys.getSmileyResource(Smileys.EMOJI37), // 29
            Smileys.getSmileyResource(Smileys.EMOJI38), // 29
            Smileys.getSmileyResource(Smileys.EMOJI39), // 29
            Smileys.getSmileyResource(Smileys.EMOJI40), // 29
            Smileys.getSmileyResource(Smileys.EMOJI41), // 29
            Smileys.getSmileyResource(Smileys.EMOJI42), // 29
            Smileys.getSmileyResource(Smileys.EMOJI43), // 29
            Smileys.getSmileyResource(Smileys.EMOJI44), // 29
            Smileys.getSmileyResource(Smileys.EMOJI45), // 29
            Smileys.getSmileyResource(Smileys.EMOJI46), // 29
            Smileys.getSmileyResource(Smileys.EMOJI47), // 29
            Smileys.getSmileyResource(Smileys.EMOJI48), // 29
            Smileys.getSmileyResource(Smileys.EMOJI49), // 29
            Smileys.getSmileyResource(Smileys.EMOJI50), // 29
            Smileys.getSmileyResource(Smileys.EMOJI51), // 29
            Smileys.getSmileyResource(Smileys.EMOJI52), // 29
            Smileys.getSmileyResource(Smileys.EMOJI53), // 29
            Smileys.getSmileyResource(Smileys.EMOJI54), // 29
            Smileys.getSmileyResource(Smileys.EMOJI55), // 29
            Smileys.getSmileyResource(Smileys.EMOJI56), // 29
            Smileys.getSmileyResource(Smileys.EMOJI57), // 29
    };


    public static final int DEFAULT_SMILEY_TEXTS = R.array.btalk_list_icon_texts;

    /**
     * Builds the hashtable we use for mapping the string version of a smiley
     * (e.g. ":-)") to a resource ID for the icon version.
     */
    private HashMap<String, Integer> buildSmileyToRes() {
        if (DEFAULT_SMILEY_RES_IDS.length != mSmileyTexts.length) {
            // Throw an exception if someone updated DEFAULT_SMILEY_RES_IDS
            // and failed to update arrays.xml
            throw new IllegalStateException("Smiley resource ID/text mismatch");
        }

        HashMap<String, Integer> smileyToRes = new HashMap<>(mSmileyTexts.length);
        for (int i = 0; i < mSmileyTexts.length; i++) {
            smileyToRes.put(mSmileyTexts[i], DEFAULT_SMILEY_RES_IDS[i]);
        }

        return smileyToRes;
    }

    /**
     * Builds the regular expression we use to find smileys in
     *
     */
    private Pattern buildPattern() {
        // Set the StringBuilder capacity with the assumption that the average
        // smiley is 3 characters long.
        StringBuilder patternString = new StringBuilder(mSmileyTexts.length * 3);

        // Build a regex that looks like (:-)|:-(|...), but escaping the smilies
        // properly so they will be interpreted literally by the regex matcher.
        patternString.append('(');
        for (String s : mSmileyTexts) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        // Replace the extra '|' with a ')'
        patternString.replace(patternString.length() - 1, patternString.length(), ")");

        return Pattern.compile(patternString.toString());
    }

    /**
     * Bkav QuangNDB add icon theo kich thuoc cua co chu
     */
    public CharSequence addSmileySpansWithTextSize(CharSequence text, int lineHeight) {
        if (text == null) {
            return null;
        }
        final SpannableStringBuilder builder = new SpannableStringBuilder(text);
        final Matcher matcher = mPattern.matcher(text);
        boolean isMatch = false;
        while (matcher.find()) {
            final int resId = mSmileyToRes.get(matcher.group());
            builder.setSpan(new EmojiSpan(resId, lineHeight), matcher.start(), matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            isMatch = true;
        }
        return isMatch ? builder : null;
    }

    /**
     * Bkav QuangNDB replace unicode emoji bang anh custom
     */
    public void replaceWithImages(final Spannable text, final int emojiSize) {
        if (text == null) {
            return;
        }
        final EmojiSpan[] existingSpans = text.getSpans(0, text.length(), EmojiSpan.class);
        final List<Integer> existingSpanPositions = new ArrayList<>(existingSpans.length);
        final int size = existingSpans.length;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < size; i++) {
            existingSpanPositions.add(text.getSpanStart(existingSpans[i]));
        }
        final Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            if (!existingSpanPositions.contains(matcher.start())) {
                final int resId = mSmileyToRes.get(matcher.group());
                text.setSpan(new EmojiSpan(resId, emojiSize), matcher.start(), matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
}
