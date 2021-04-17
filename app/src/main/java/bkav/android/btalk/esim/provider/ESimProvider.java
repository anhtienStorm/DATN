package bkav.android.btalk.esim.provider;

import android.net.Uri;

public class ESimProvider {
    public static final String AUTHORITY = "bkav.android.esim.simmanager";
    public static final String SIM_TABLE = "sim_info";
    public static final String SIM_TYPE = "sim_type";
    public static final String URL = "content://" + AUTHORITY + "/" + SIM_TABLE;
    public static final Uri CONTENT_URI = Uri.parse(URL);
    public static final int STATE_ON = 1;
    public static final int STATE_OFF = 0;
    public static class SimTable {

        public static final String ID = "_id";

        public static final String ICCID = "iccid";

        public static final String PROFILE_STATE = "profileState"; // Trang thai enable hay dissable

        public static final String NICKNAME_PRESENT = "nicknamePresent";

        public static final String NICKNAME = "nickname";

        public static final String PROFILENAME_PRESENT = "profileNamePresent";

        public static final String PROFILENAME = "profileName";

        public static final String SPNNAME_PRESENT = "spnNamePresent";

        public static final String SPNNAME = "spnName";

        public static final String PROFILE_CLASS = "profileClass";

        public static final String PROFILE_POLICY_MASK = "profilePolicyMask";

        public static final String ICON_TYPE = "iconType";

        public static final String ICON = "icon";

        public static final String SIM_SLOT_ESIM = "esim_slot";

        public static final String SIM_SLOT = "sim_slot";

        public static final String COLOR = "color";

        public static final String NUMBER = "number";

        public static final String NAME_SIM = "name_sim";

        public static final String IS_ESIM = "is_esim";

        public static final String INDEX_SIM = "index_sim"; // luu lai so thu tu cua 1 sim

        public static final String EID = "eid";
    }
}
