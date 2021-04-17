package bkav.android.btalk.esim;

public class ZaloProfile implements ISimProfile {

    @Override
    public String getNameSimProfile() {
        return "Zalo";
    }

    @Override
    public String getNickNameProfile() {
        return "Zalo";
    }

    @Override
    public byte[] getSimIdProfile() {
        return new byte[0];
    }

    @Override
    public boolean getSimProfileState() {
        return true;
    }

    @Override
    public int getSlotSim() {
        return 2;
    }

    @Override
    public int getProfileIndex() {
        return 0;
    }
}
