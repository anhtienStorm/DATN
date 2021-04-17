package bkav.android.btalk.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.android.messaging.datamodel.data.MessageData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bkav.android.btalk.messaging.util.UriDeserializer;
import bkav.android.btalk.messaging.util.UriSerializer;

public class PrefUtils {

    private static volatile PrefUtils sInstance;
    public static final int DELAY_CLEAR_TEXT_SEARCH = 500;
    public static final String TAB_CACHE_PHONE = "tab_cache";//Bkav QuangNDb bien luu tab cache preference
    public static final String CONVERSATION_ID = "conversation_id";//Bkav QuangNDb bien luu conversation id preference
    public static final String TIME_PAUSE_APP = "time_pause_app";//Bkav QuangNDb bien luu thoi gian thoat ung dung tu nut home preference
    public static final String KEEP_STATUS_APP = "keep_status_app";//Bkav QuangNDb bien luu trang thai ung dung tu nut home preference
    public static final String DRAFT_DATA = "draft_data";//Bkav QuangNDb luu draft data vao preference
    public static PrefUtils get() {
        if (sInstance == null) {
            sInstance = new PrefUtils();
        }
        return sInstance;
    }
    /**
     * Bkav - Trungth : Luu du lieu vao sharedpreferen
     */
    public void saveBooleanPreferences(Context context, String stringPreference, boolean data) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(stringPreference, data);
        editor.apply();
    }

    /**Bkav QuangNDb luu du lieu int preference*/
    public void saveIntPreferences(Context context, String stringPreference, int data) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(stringPreference, data);
        editor.apply();
    }

    /**Bkav QuangNDb luu du lieu String preference*/
    public void saveStringPreferences(Context context, String stringPreference, String data) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(stringPreference, data);
        editor.apply();
    }

    /**Bkav QuangNDb luu du lieu String preference*/
    public void saveLongPreferences(Context context, String stringPreference, long data) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(stringPreference, data);
        editor.apply();
    }

    /**
     * Bkav - Trungth : Load du lieu tu sharedpreferen
     *
     */
    public boolean loadBooleanPreferences(Context context, String stringPreference,
                                                 boolean defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(stringPreference, defaultValue);
    }

    /**Bkav QuangNDb load int preference*/
    public int loadIntPreferences(Context context, String stringPreference, int defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(stringPreference, defaultValue);
    }

    /**Bkav QuangNDb load String preference*/
    public String loadStringPreferences(Context context, String stringPreference, String defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getString(stringPreference, defaultValue);
    }

    /**Bkav QuangNDb load Long preference*/
    public long loadLongPreferences(Context context, String stringPreference, long defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getLong(stringPreference, defaultValue);
    }


    /**Bkav QuangNDb luu du lieu Draft preference*/
    public void saveDraftPreferences(Context context, String key, MessageData data) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .create();
        String json = gson.toJson(data);
        editor.putString(key, json);
        editor.apply();
    }

    /**Bkav QuangNDb load Draft preference*/
    public MessageData loadDraftPreferences(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriDeserializer())
                .create();
        String json = sharedPreferences.getString(key, "");
        return gson.fromJson(json, MessageData.class);
    }
}
