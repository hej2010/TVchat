package se.arctosoft.tvchat.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class Settings {
    private static final String SHARED_PREFERENCES_NAME = "user_preferences";
    private static final String PREF_ADMIN = "a";
    private static final String PREF_ACCEPTED_TERMS = "t";
    private final Context context;

    public Settings(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    public SharedPreferences getSharedPrefs() {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getSharedPrefsEditor() {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
    }

    public boolean isAdmin() {
        return getSharedPrefs().getBoolean(PREF_ADMIN, false);
    }

    public void setIsAdmin(boolean admin) {
        getSharedPrefsEditor().putBoolean(PREF_ADMIN, admin).apply();
    }

    public boolean acceptedTerms() {
        return getSharedPrefs().getBoolean(PREF_ACCEPTED_TERMS, false);
    }

    public void setAcceptedTerms(boolean accepted) {
        getSharedPrefsEditor().putBoolean(PREF_ACCEPTED_TERMS, accepted).apply();
    }

}
