package gy.sog.Juggler;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class JugglerPreferenceActivity extends PreferenceActivity
{
    static public final String PREF_SERVER_SEND_DATA = "PREF_SERVER_SEND_DATA";
    static public final String PREF_SERVER_IP = "PREF_SERVER_IP";
    static public final String PREF_SERVER_PORT = "PREF_SERVER_PORT";

    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.userpreferences);
    }
}
