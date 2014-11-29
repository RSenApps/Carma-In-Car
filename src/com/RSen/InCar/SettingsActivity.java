package com.RSen.InCar;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen() {

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);

		// Add 'data and sync' preferences, and a corresponding header.
		// fakeHeader = new PreferenceCategory(this);
		// fakeHeader.setTitle(R.string.pref_header_data_sync);
		// getPreferenceScreen().addPreference(fakeHeader);
		// addPreferencesFromResource(R.xml.pref_data_sync);

		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		// bindPreferenceSummaryToValue(findPreference("notifications_text"));
		// bindPreferenceSummaryToValue(findPreference("notifications_call"));
		findPreference("correction_phrases").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						startActivity(new Intent(getApplicationContext(),
								CorrectionPhrasesActivity.class));
						return true;
					}
				});
		findPreference("commutes").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						startActivity(new Intent(getApplicationContext(),
								CommutesActivity.class));
						return true;
					}
				});
		if (!TaskerIntent.taskerInstalled(this)) {
			findPreference("tasker").setEnabled(false);
		}
		findPreference("tasker").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						startActivity(new Intent(getApplicationContext(),
								TaskerActivity.class));
						return true;
					}
				});

	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return false;
	}
	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	/*
	 * @TargetApi(Build.VERSION_CODES.HONEYCOMB) public static class
	 * GeneralPreferenceFragment extends PreferenceFragment {
	 * 
	 * @Override public void onCreate(Bundle savedInstanceState) {
	 * super.onCreate(savedInstanceState);
	 * //addPreferencesFromResource(R.xml.pref_general);
	 * 
	 * // Bind the summaries of EditText/List/Dialog/Ringtone preferences // to
	 * their values. When their values change, their summaries are // updated to
	 * reflect the new value, per the Android Design // guidelines.
	 * //bindPreferenceSummaryToValue(findPreference("example_text"));
	 * //bindPreferenceSummaryToValue(findPreference("example_list")); } }
	 */

	/*
	 * @TargetApi(Build.VERSION_CODES.HONEYCOMB) public static class
	 * DataSyncPreferenceFragment extends PreferenceFragment {
	 * 
	 * @Override public void onCreate(Bundle savedInstanceState) {
	 * super.onCreate(savedInstanceState);
	 * addPreferencesFromResource(R.xml.pref_data_sync);
	 * 
	 * // Bind the summaries of EditText/List/Dialog/Ringtone preferences // to
	 * their values. When their values change, their summaries are // updated to
	 * reflect the new value, per the Android Design // guidelines.
	 * bindPreferenceSummaryToValue(findPreference("sync_frequency")); } }
	 */
}
