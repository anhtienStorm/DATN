package bkav.android.btalk.esim.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.android.messaging.Factory;
import com.qualcomm.qti.lpa.UimLpaProfile;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.BtalkExecutors;
import bkav.android.btalk.esim.ISimProfile;
import bkav.android.btalk.esim.Utils.ESimUtils;

public class ESimDbController {

    //Bkav QuangNDb lay ra toan bo sim trong may gom ca sim thuong va eSim
    public static List<ISimProfile> getAllSim() {
        List<ISimProfile> iSimProfileList = new ArrayList<>();
        // Bkav HuyNQN them dieu kien query de fix loi lay ra sim có pos = -1
        Cursor cursor = Factory.get().getApplicationContext().getContentResolver().query(ESimProvider.CONTENT_URI,
                null, ESimProvider.SimTable.SIM_SLOT_ESIM + " >= 0", null, ESimProvider.SimTable.SIM_SLOT_ESIM);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String nickName = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.NICKNAME));
                final String profileName = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.PROFILENAME));
                final String iccid = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.ICCID));
                final String slot = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.SIM_SLOT));
                final int color = cursor.getInt(cursor.getColumnIndex(ESimProvider.SimTable.COLOR));
                final int stateInt = cursor.getInt(cursor.getColumnIndex(ESimProvider.SimTable.PROFILE_STATE));
                final String simNameSetting = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.NAME_SIM));
                final int profileIndex = cursor.getInt(cursor.getColumnIndex(ESimProvider.SimTable.SIM_SLOT_ESIM));
                final boolean state = stateInt == 1;
                if (!ESimUtils.isSlotNotReady(Integer.parseInt(slot))) {
                    ISimProfile profile = new UimLpaProfile(nickName, profileName, iccid.getBytes(),
                            Integer.parseInt(slot), color, state, simNameSetting, profileIndex);
                    iSimProfileList.add(profile);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        return iSimProfileList;
    }

    //Bkav QuangNDb get profile from iccid
    public static ISimProfile getProfileFromIccId(String iccid) {
        Cursor cursor = Factory.get().getApplicationContext().getContentResolver().query(ESimProvider.CONTENT_URI,
                null, ESimProvider.SimTable.ICCID + " like '" + iccid + "'", null, null);
        if (cursor != null && cursor.moveToFirst()) {
            final String nickName = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.NICKNAME));
            final String profileName = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.PROFILENAME));
            final String slot = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.SIM_SLOT));
            final int color = cursor.getInt(cursor.getColumnIndex(ESimProvider.SimTable.COLOR));
            final int stateInt = cursor.getInt(cursor.getColumnIndex(ESimProvider.SimTable.PROFILE_STATE));
            final String simNameSetting = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.NAME_SIM));
            final int profileIndex = cursor.getInt(cursor.getColumnIndex(ESimProvider.SimTable.SIM_SLOT_ESIM));
            final boolean state = stateInt == 1;
            return new UimLpaProfile(nickName, profileName, iccid.getBytes(),
                    Integer.parseInt(slot), color, state, simNameSetting, profileIndex);
        }
        return null;
    }

    //Bkav QuangNDb get iccid active profile slot
    public static String getIccIdActivateProfileWithSlot(int slot, Context context) {
        Cursor cursor = context.getContentResolver().query(ESimProvider.CONTENT_URI,
                null, ESimProvider.SimTable.SIM_SLOT + " = " + slot
                        + " and " + ESimProvider.SimTable.IS_ESIM + " = 1 and "
                        + ESimProvider.SimTable.PROFILE_STATE + " = 1", null, null);
        if (cursor != null && cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.ICCID));
        }
        return "";
    }

    //Bkav QuangNDb lay ra toan bo sim trong may gom ca sim thuong va eSim
    public static List<ISimProfile> getAllSimExcludeActiveDefaultSlot(int defaultSlot) {
        List<ISimProfile> iSimProfileList = new ArrayList<>();
        Cursor cursor = Factory.get().getApplicationContext().getContentResolver().query(ESimProvider.CONTENT_URI,
                null, null, null, ESimProvider.SimTable.SIM_SLOT_ESIM);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String nickName = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.NICKNAME));
                final String profileName = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.PROFILENAME));
                final String iccid = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.ICCID));
                final String slot = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.SIM_SLOT));
                final int color = cursor.getInt(cursor.getColumnIndex(ESimProvider.SimTable.COLOR));
                final int stateInt = cursor.getInt(cursor.getColumnIndex(ESimProvider.SimTable.PROFILE_STATE));
                final String simNameSetting = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.NAME_SIM));
                final int profileIndex = cursor.getInt(cursor.getColumnIndex(ESimProvider.SimTable.SIM_SLOT_ESIM));
                final boolean state = stateInt == 1;
                //Bkav QuangNDb defaultSlot bang -1 trong truong hop lam 1 sim,esim
                if (defaultSlot == -1) {
                    if (!state) {
                        ISimProfile profile = new UimLpaProfile(nickName, profileName, iccid.getBytes(),
                                Integer.parseInt(slot), color, false, simNameSetting, profileIndex);
                        iSimProfileList.add(profile);
                    }
                } else {
                    //Bkav QuangNDb khong trung default slot hoac khong dang trang thai active thi moi add vao
                    if (defaultSlot != Integer.parseInt(slot) || !state) {
                        ISimProfile profile = new UimLpaProfile(nickName, profileName, iccid.getBytes(),
                                Integer.parseInt(slot), color, state, simNameSetting, profileIndex);
                        iSimProfileList.add(profile);
                    }
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return iSimProfileList;
    }


    //Bkav QuangNDb check xem may co dang lap esim hay khong
    public static boolean isEsimExist() {
        boolean isExist = false;
        Cursor cursor = Factory.get().getApplicationContext().getContentResolver().query(ESimProvider.CONTENT_URI,
                null, ESimProvider.SimTable.IS_ESIM + " = 1", null, null);
        if (cursor != null && cursor.getCount() > 0) {
            isExist = true;
            cursor.close();
        }
        return isExist;
    }

    //Bkav QuangNDb check xem may dang co bao nhieu sim dang enable
    public static int countSim() {
        int count = 0;
        Cursor cursor = Factory.get().getApplicationContext().getContentResolver().query(ESimProvider.CONTENT_URI,
                null, null, null, null);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                final String slot = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.SIM_SLOT));
                if (!ESimUtils.isSlotNotReady(Integer.parseInt(slot))) {
                    count++;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return count;
    }

    //Bkav QuangNDb update db trang thai esim sau khi active
    public static void updateDBWhenActivatingEsim(ISimProfile profile) {
        BtalkExecutors.runOnBGThread(new Runnable() {
            @Override
            public void run() {
                // Bkav HuyNQN cap nhat lai sim dang bat thanh tat
                ContentValues contentValues = new ContentValues();
                contentValues.put(ESimProvider.SimTable.PROFILE_STATE, ESimProvider.STATE_OFF);
                Factory.get().getApplicationContext().getContentResolver().update(ESimProvider.CONTENT_URI, contentValues, ESimProvider.SimTable.PROFILE_STATE
                        + " LIKE ?" + " AND " + ESimProvider.SimTable.SIM_SLOT + " LIKE ?", new String[]{"1", String.valueOf(profile.getSlotSim())});

                // Bkav HuyNQN cap nhat sim duoc click thanh bat
                contentValues = new ContentValues();
                contentValues.put(ESimProvider.SimTable.PROFILE_STATE, ESimProvider.STATE_ON);
                Factory.get().getApplicationContext().getContentResolver().update(ESimProvider.CONTENT_URI, contentValues,
                        ESimProvider.SimTable.ICCID + " LIKE ?", new String[]{new String(profile.getSimIdProfile())});
            }
        });
    }

    //Bkav QuangNDb query tat ca profile trong 1 slot
    public static List<ISimProfile> getAllProfileForSlot(int slot) {
        List<ISimProfile> iSimProfileList = new ArrayList<>();
        //Bkav QuangNDb check slot khong active thi return rong luon
        if (ESimUtils.isSlotNotReady(slot)) {
            return iSimProfileList;
        }
        Cursor cursor = Factory.get().getApplicationContext().getContentResolver().query(ESimProvider.CONTENT_URI, null
                , ESimProvider.SimTable.SIM_SLOT + " = ?", new String[]{String.valueOf(slot)}, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                boolean state;
                String nickName = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.NICKNAME));
                String profileName = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.PROFILENAME));
                String iccid = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.ICCID));
                int color = cursor.getInt(cursor.getColumnIndex(ESimProvider.SimTable.COLOR));
                int stateInt = cursor.getInt(cursor.getColumnIndex(ESimProvider.SimTable.PROFILE_STATE));
                String simNameSetting = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.NAME_SIM));
                state = stateInt == 1;
                final int profileIndex = cursor.getInt(cursor.getColumnIndex(ESimProvider.SimTable.SIM_SLOT_ESIM));
                ISimProfile profile = new UimLpaProfile(nickName, profileName, iccid.getBytes(), slot, color, state, simNameSetting, profileIndex);
                iSimProfileList.add(profile);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return iSimProfileList;
    }

    //Bkav QuangNDb lay ra 1 activate profile esim tu 1 slot
    public static ISimProfile getActivateProfileFromSlot(int slot) {
        Cursor cursor = Factory.get().getApplicationContext().getContentResolver().query(ESimProvider.CONTENT_URI, null
                , ESimProvider.SimTable.SIM_SLOT + " = ?", new String[]{String.valueOf(slot)}, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                boolean state;
                String nickName = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.NICKNAME));
                String profileName = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.PROFILENAME));
                String iccid = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.ICCID));
                int color = cursor.getInt(cursor.getColumnIndex(ESimProvider.SimTable.COLOR));
                int stateInt = cursor.getInt(cursor.getColumnIndex(ESimProvider.SimTable.PROFILE_STATE));
                String simNameSetting = cursor.getString(cursor.getColumnIndex(ESimProvider.SimTable.NAME_SIM));
                state = stateInt == 1;
                final int profileIndex = cursor.getInt(cursor.getColumnIndex(ESimProvider.SimTable.SIM_SLOT_ESIM));
                ISimProfile profile = new UimLpaProfile(nickName, profileName, iccid.getBytes(), slot, color, state, simNameSetting, profileIndex);
                if (stateInt == 1) {
                    return profile;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return null;
    }
}
