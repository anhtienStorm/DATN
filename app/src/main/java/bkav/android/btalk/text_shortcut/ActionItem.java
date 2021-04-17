
package bkav.android.btalk.text_shortcut;

import android.graphics.drawable.Drawable;

import bkav.android.btalk.messaging.datamodel.data.QuickResponseData;

public class ActionItem {
    private Drawable mIcon;
    private String mTitle;
    private int mActionId = -1;
    private QuickResponseData mQuickResponseData = null;
    private boolean mSticky;

    /**
     * Constructor
     * @param icon Icon to use
     */
    public ActionItem(Drawable icon, QuickResponseData quickResponseData) {
        if (quickResponseData == null) {
            return;
        }
        this.mQuickResponseData = quickResponseData;
        this.mTitle = quickResponseData.getResponse();
        this.mIcon = icon;
    }

    /**
     * Get action title
     * 
     * @return action title
     */
    public String getTitle() {
        return this.mTitle;
    }

    /**
     * Get action icon
     * 
     * @return {@link Drawable} action icon
     */
    public Drawable getIcon() {
        return this.mIcon;
    }

    /**
     * @return Our action id
     */
    public int getActionId() {
        return mActionId;
    }

    /**Bkav QuangNDb get quick response data*/
    public QuickResponseData getQuickResponseData() {
        return mQuickResponseData;
    }

    /**
     * @return true if button is sticky, menu stays visible after press
     */
    public boolean isSticky() {
        return mSticky;
    }


}
