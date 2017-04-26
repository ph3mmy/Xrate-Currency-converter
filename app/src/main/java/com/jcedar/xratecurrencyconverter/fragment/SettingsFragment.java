package com.jcedar.xratecurrencyconverter.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.jcedar.xratecurrencyconverter.R;

/**
 * Created by OLUWAPHEMMY on 12/7/2016.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = SettingsFragment.class.getSimpleName();
    SharedPreferences sharedPreferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        onSharedPreferenceChanged(sharedPreferences, "pref_frequency");
        onSharedPreferenceChanged(sharedPreferences, "pref_currency");
        onSharedPreferenceChanged(sharedPreferences, "pref_auto_update");
        onSharedPreferenceChanged(sharedPreferences, "pref_wifi_only");
        onSharedPreferenceChanged(sharedPreferences, "pref_theme");

        final CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.pref_key_auto_update));
        final ListPreference freqListPref = (ListPreference) findPreference(getString(R.string.pref_key_frequency));
        checkBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (!checkBoxPreference.isChecked()){
                    freqListPref.setShouldDisableView(true);
                }
                return false;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        Preference preference =findPreference(s);
        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(sharedPreferences.getString(s,""));
            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);

        } else if (preference instanceof CheckBoxPreference) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
            boolean check = checkBoxPreference.isChecked();
//            preference.d
        }

        else {
                        preference.setSummary(sharedPreferences.getString(s,""));
                    }

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
