package bkav.android.btalk.messaging.util;

import android.accounts.Account;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;

import com.android.contacts.common.lettertiles.LetterTileDrawable;

/**
 * Created by quangnd on 19/05/2017.
 */

public class BtalkLetterTitleDrawable extends LetterTileDrawable {


    public BtalkLetterTitleDrawable(Resources res, Context context, Account account) {
        super(res, context, account);
    }

    @Override
    protected float getTileRatio(Resources res) {
        return 0.45f;
    }

    @Override
    protected Typeface getTypeFace(Resources res) {
        return BtalkTypefaces.sRobotoLightFont;
    }

    @Override
    protected void notShowLetter() {
        // Bkav QuangNDb khong lam gi
    }
}
