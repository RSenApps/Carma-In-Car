package com.RSen.InCar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;

public class CorrectionPhrasesActivity extends Activity {
	HashMap<String, String> savedList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_correction_phrases);
		// Show the Up button in the action bar.
		setupActionBar();

		final ListView listview = (ListView) findViewById(R.id.listview);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String json = prefs.getString("correction_phrases_json", null);
		final ArrayList<String> list = new ArrayList<String>();
		if (json != null) {
			Gson gson = new Gson();
			savedList = gson.fromJson(json, HashMap.class);
			Set<String> keySet = savedList.keySet();
			for (String key : keySet) {
				String item = key + " -> " + savedList.get(key);
				list.add(item);
			}
		} else {
			savedList = new HashMap<String, String>();
		}
		if (savedList.size() <= 0) {
			list.add("No phrases yet...");
		}
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		adapter.setNotifyOnChange(true);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				String item = list.get(position);
				if (!item.matches("No phrases yet...")) {
					Set<String> keySet = savedList.keySet();
					for (String key : keySet) {
						if (item.startsWith(key)) {
							editDialog(adapter, key, savedList.get(key));
						}
					}

				} else {
					addDialog(adapter);
				}
			}

		});
		findViewById(R.id.add).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (savedList.size() <= 0) {
					list.remove("No phrases yet...");
				}
				addDialog(adapter);
			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.correction, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.help) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setMessage(R.string.correction_help);
			builder.setTitle("Correction Phrases");
			builder.show();
		}
		return super.onMenuItemSelected(featureId, item);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	private void addDialog(final ArrayAdapter<String> adapter) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Add Correction Phrase");
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.corrective_phrases_dialog,
				null);
		builder.setView(view);
		builder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String oldPhrase = ((EditText) view
								.findViewById(R.id.oldPhrase)).getText()
								.toString();
						String newPhrase = ((EditText) view
								.findViewById(R.id.newPhrase)).getText()
								.toString();
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						savedList.put(oldPhrase, newPhrase);
						Gson gson = new Gson();
						prefs.edit()
								.putString("correction_phrases_json",
										gson.toJson(savedList)).commit();
						String item = oldPhrase + " -> " + newPhrase;
						adapter.remove(item); // incase attempting to make
												// duplicate
						adapter.add(item);
					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		builder.show();
	}

	private void editDialog(final ArrayAdapter<String> adapter,
			final String oldPhrase, final String newPhrase) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Edit Correction Phrase");
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.corrective_phrases_dialog,
				null);
		final EditText oldEdit = (EditText) view.findViewById(R.id.oldPhrase);
		final EditText newEdit = (EditText) view.findViewById(R.id.newPhrase);
		oldEdit.setText(oldPhrase);
		newEdit.setText(newPhrase);
		builder.setView(view);
		builder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String item = oldPhrase + " -> " + newPhrase;
						adapter.remove(item);
						String oldPhrase = oldEdit.getText().toString();
						String newPhrase = newEdit.getText().toString();
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						savedList.put(oldPhrase, newPhrase);
						Gson gson = new Gson();
						prefs.edit()
								.putString("correction_phrases_json",
										gson.toJson(savedList)).commit();
						item = oldPhrase + " -> " + newPhrase;
						adapter.add(item);
					}
				});
		builder.setNeutralButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		builder.setNegativeButton("Delete",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						savedList.remove(oldPhrase);
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						Gson gson = new Gson();
						prefs.edit()
								.putString("correction_phrases_json",
										gson.toJson(savedList)).commit();
						String item = oldPhrase + " -> " + newPhrase;
						adapter.remove(item);
					}
				});
		builder.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
