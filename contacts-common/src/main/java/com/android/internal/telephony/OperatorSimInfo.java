package com.android.internal.telephony;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Created by trungth on 03/03/2017.
 */

public class OperatorSimInfo {
    public OperatorSimInfo(Context context) {

    }

    public boolean isOperatorFeatureEnabled() {
        return false;
    }

    public boolean isSimTypeOperator(int slotIndex) {
        return false;
    }

    public Drawable getOperatorDrawable() {
        return null;
    }

    public Drawable getGenericSimDrawable() {
        return null;
    }

    public String getOperatorDisplayName() {
        return null;
    }

    public String getOperatorNameForSubId(int subId) {
            return null;
    }
}
