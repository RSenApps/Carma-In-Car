package com.RSen.InCar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.gson.Gson;

public class TaskerActivity extends Activity {
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
		String json = prefs.getString("tasker_json", null);
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
			list.add("No commands yet...");
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
				if (!item.matches("No commands yet...")) {
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
					list.remove("No commands yet...");
				}
				addDialog(adapter);
			}

		});
		// load current commands
		CommandRouter.cleanCommands();
		new MuteExecuter();
		new ControlMusicExecuter();
		new NavigationExecuter();
		new CallExecuter();
		new ETAExecuter();
		new SendETAExecuter();
		new SetDestinationExecuter();
		new TimeToCurrentDestinationExecuter();
		new DistanceToCurrentDestinationExecuter();
		new SMSExecuter();
		new CancelExecuter();
		new ShutDownExecuter();
		new EmailExecuter();
		new PlayMusicExecuter();
		new FeedbackExecuter();
		new UnMuteExecuter();

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
		builder.setTitle("Add Tasker Command");
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.tasker_dialog, null);
		final Spinner commandList = (Spinner) view
				.findViewById(R.id.commandListButton);
		SpinnerAdapter spinnerAdapter = getTaskerCommandsAdapter();
		if (spinnerAdapter == null) {
			Toast.makeText(
					this,
					"You have no Tasker tasks... Please ensure that Tasker>Misc>Allow External"
							+ "Access is enabled", Toast.LENGTH_SHORT).show();
			return;
		}
		commandList.setAdapter(spinnerAdapter);
		builder.setView(view);
		builder.setPositiveButton("Done", null); // handled in onShow so that i
		// can control dismiss
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		final AlertDialog alertDialog = builder.create();
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {

				Button b = alertDialog
						.getButton(DialogInterface.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						String commandPhrase = ((EditText) view
								.findViewById(R.id.commandPhrase)).getText()
								.toString();
						String taskerCommand = (String) ((Spinner) view
								.findViewById(R.id.commandListButton))
								.getSelectedItem();
						if (commandPhrase.trim().length() == 0) {
							Toast.makeText(TaskerActivity.this,
									"Please enter command phrase...",
									Toast.LENGTH_SHORT).show();
							return;
						}
						String overridenCommand = CommandRouter
								.checkForCommand(commandPhrase);
						if (overridenCommand != null) {
							Toast.makeText(
									TaskerActivity.this,
									"WARNING: this command overrides "
											+ overridenCommand,
									Toast.LENGTH_LONG).show();
						}
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						savedList.put(commandPhrase, taskerCommand);
						Gson gson = new Gson();
						prefs.edit()
								.putString("tasker_json",
										gson.toJson(savedList)).commit();
						String item = commandPhrase + " -> " + taskerCommand;
						adapter.remove(item); // incase attempting to make
						// duplicate
						adapter.add(item);
						alertDialog.dismiss();
					}
				});
			}
		});
		alertDialog.show();
	}

	private SpinnerAdapter getTaskerCommandsAdapter() {
		Cursor c = getContentResolver().query(
				Uri.parse("content://net.dinglisch.android.tasker/tasks"),
				null, null, null, null);
		if (c != null) {
			ArrayList<String> list = new ArrayList<String>();
			while (c.moveToNext()) {
				list.add(c.getString(c.getColumnIndex("name")));
			}
			return new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_dropdown_item, list);
		}
		return null;
	}

	private void editDialog(final ArrayAdapter<String> adapter,
			final String oldCommandPhrase, final String oldTaskerCommand) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Edit Tasker Command");
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.tasker_dialog, null);
		final EditText commandPhraseEdit = (EditText) view
				.findViewById(R.id.commandPhrase);
		final Spinner commandList = (Spinner) view
				.findViewById(R.id.commandListButton);
		commandPhraseEdit.setText(oldCommandPhrase);
		SpinnerAdapter spinnerAdapter = getTaskerCommandsAdapter();
		if (spinnerAdapter == null) {
			Toast.makeText(this, "You have no Tasker tasks...",
					Toast.LENGTH_SHORT).show();
			return;
		}
		commandList.setAdapter(spinnerAdapter);
		builder.setView(view);
		builder.setPositiveButton("Done", null); // in onShow so i have control
													// of dismiss
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
						savedList.remove(oldCommandPhrase);
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						Gson gson = new Gson();
						prefs.edit()
								.putString("tasker_json",
										gson.toJson(savedList)).commit();
						String item = oldCommandPhrase + " -> "
								+ oldTaskerCommand;
						adapter.remove(item);
					}
				});
		final AlertDialog alertDialog = builder.create();
		alertDialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				Button b = alertDialog
						.getButton(DialogInterface.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						String item = oldCommandPhrase + " -> "
								+ oldTaskerCommand;
						adapter.remove(item);
						String commandPhrase = commandPhraseEdit.getText()
								.toString();
						String taskerCommand = (String) ((Spinner) view
								.findViewById(R.id.commandListButton))
								.getSelectedItem();
						if (commandPhrase.trim().length() == 0) {
							Toast.makeText(TaskerActivity.this,
									"Please enter command phrase...",
									Toast.LENGTH_SHORT).show();
							return;
						}

						String overridenCommand = CommandRouter
								.checkForCommand(commandPhrase);
						if (overridenCommand != null) {
							Toast.makeText(
									TaskerActivity.this,
									"WARNING: this command overrides "
											+ overridenCommand,
									Toast.LENGTH_LONG).show();
						}
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						savedList.put(commandPhrase, taskerCommand);
						Gson gson = new Gson();
						prefs.edit()
								.putString("tasker_json",
										gson.toJson(savedList)).commit();
						item = commandPhrase + " -> " + taskerCommand;
						adapter.add(item);
						alertDialog.dismiss();
					}

				});
			}
		});

		alertDialog.show();

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
