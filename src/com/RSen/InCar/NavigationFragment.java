package com.RSen.InCar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class NavigationFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.navigation,
				container, false);
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(inflater.getContext());
		((EditText) rootView.findViewById(R.id.home)).setText(prefs.getString(
				"home", ""));
		((EditText) rootView.findViewById(R.id.home))
				.addTextChangedListener(new TextWatcher() {
					@Override
					public void afterTextChanged(Editable s) {
						prefs.edit().putString("home", s.toString()).commit();
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
					}
				});
		((EditText) rootView.findViewById(R.id.work)).setText(prefs.getString(
				"work", ""));
		((EditText) rootView.findViewById(R.id.work))
				.addTextChangedListener(new TextWatcher() {
					@Override
					public void afterTextChanged(Editable s) {
						prefs.edit().putString("work", s.toString()).commit();
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
					}
				});
		return rootView;
	}
}
