package bkav.android.btalk.esim;

public interface ISimProfile {
    String getNameSimProfile();
    String getNickNameProfile();
    byte[] getSimIdProfile();
    boolean getSimProfileState();
    int getSlotSim();
    //Bkav QuangNDb lay so sim cua profile
    int getProfileIndex();

    default int getColor() {
        return 0;
    }

    default String getSimNameSetting() {
        return null;
    }
    //HienDTk: update lai mau cho button sen messgase
    default int updateColor(){
        return 0;
    }
}
