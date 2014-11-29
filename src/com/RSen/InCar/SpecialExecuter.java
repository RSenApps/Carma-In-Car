package com.RSen.InCar;

import java.util.List;

import android.content.Context;

public interface SpecialExecuter {
	void executeCommand(List<String> inputs, Context context,
			AudioUI uiReference);

	void reply(List<String> reply);
}
