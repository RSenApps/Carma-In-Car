package com.RSen.InCar;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class OtherCommandsFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.other_commands, container, false);
		rootView.findViewById(R.id.commandListButton).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						AlertDialog.Builder builder = new Builder(v
								.getContext());
						builder.setMessage(R.string.command_help);
						builder.setTitle("Command List");
						builder.show();
					}
				});
		rootView.findViewById(R.id.start).getBackground()
				.setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
		rootView.findViewById(R.id.start).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						getActivity().finish();
					}
				});
		return rootView;
	}
}
