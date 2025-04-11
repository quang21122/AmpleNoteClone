package com.example.amplenoteclone.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PinManager {
    private static final String PREF_NAME = "NotePinPrefs";
    private static final String PIN_KEY_PREFIX = "pin_";

    private SharedPreferences prefs;

    public PinManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setPin(String noteId, String pin) {
        prefs.edit().putString(PIN_KEY_PREFIX + noteId, pin).apply();
    }

    public String getPin(String noteId) {
        return prefs.getString(PIN_KEY_PREFIX + noteId, null);
    }

    public void removePin(String noteId) {
        prefs.edit().remove(PIN_KEY_PREFIX + noteId).apply();
    }

    public boolean hasPin(String noteId) {
        return prefs.contains(PIN_KEY_PREFIX + noteId);
    }
}