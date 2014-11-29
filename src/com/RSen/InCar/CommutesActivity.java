package com.RSen.InCar;

import java.util.ArrayList;
import java.util.Set;

import android.annotation.SuppressLint;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;

public class CommutesActivity extends Activity {
	CommuteMap savedList;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_correction_phrases);
		// Show the Up button in the action bar.
		setupActionBar();

		final ListView listview = (ListView) findViewById(R.id.listview);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String json = prefs.getString("commutes_json", null);
		final ArrayList<String> list = new ArrayList<String>();
		if (json != null) {
			Gson gson = new Gson();
			savedList = gson.fromJson(json, CommuteMap.class);
			Set<String> keySet = savedList.keySet();
			for (String key : keySet) {
				list.add(key);
			}
		} else {
			savedList = new CommuteMap();
		}
		if (savedList.size() <= 0) {
			list.add("No commutes yet...");
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
				if (!item.matches("No commutes yet...")) {
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
					list.remove("No commutes yet...");
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
			builder.setMessage(R.string.commute_help);
			builder.setTitle("Commutes");
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
		builder.setTitle("Add Commute");
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.commute_dialog, null);
		builder.setView(view);
		builder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String title = ((EditText) view
								.findViewById(R.id.title)).getText().toString();
						String destination = ((EditText) view
								.findViewById(R.id.destination)).getText()
								.toString();
						Boolean readETA = ((CheckBox) view
								.findViewById(R.id.readETA)).isChecked();
						String listenTo = ((EditText) view
								.findViewById(R.id.listenTo)).getText()
								.toString();
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						Commute commute = new Commute(destination, listenTo,
								readETA);
						savedList.put(title, commute);
						Gson gson = new Gson();
						prefs.edit()
								.putString("commutes_json",
										gson.toJson(savedList)).commit();
						adapter.remove(title); // incase attempting to make
												// duplicate
						adapter.add(title);
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
			final String oldTitle, final Commute oldCommute) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Edit Commute");
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.commute_dialog, null);
		final EditText titleEdit = ((EditText) view.findViewById(R.id.title));
		final EditText destinationEdit = ((EditText) view
				.findViewById(R.id.destination));
		final CheckBox readETABox = ((CheckBox) view.findViewById(R.id.readETA));
		final EditText listenToEdit = ((EditText) view
				.findViewById(R.id.listenTo));
		titleEdit.setText(oldTitle);
		destinationEdit.setText(oldCommute.destination);
		readETABox.setChecked(oldCommute.readETA);
		listenToEdit.setText(oldCommute.listenTo);
		builder.setView(view);
		builder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						adapter.remove(oldTitle);
						String title = titleEdit.getText().toString();
						String destination = destinationEdit.getText()
								.toString();
						Boolean readETA = readETABox.isChecked();
						String listenTo = listenToEdit.getText().toString();
						Commute commute = new Commute(destination, listenTo,
								readETA);
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						savedList.put(title, commute);
						Gson gson = new Gson();
						prefs.edit()
								.putString("commutes_json",
										gson.toJson(savedList)).commit();
						adapter.add(title);
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
						savedList.remove(oldTitle);
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						Gson gson = new Gson();
						prefs.edit()
								.putString("commutes_json",
										gson.toJson(savedList)).commit();
						adapter.remove(oldTitle);
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
