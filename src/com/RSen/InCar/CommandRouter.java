package com.RSen.InCar;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.language.DoubleMetaphone;

import android.content.Context;

public class CommandRouter {
	private static HashMap<String, SpecialExecuter> commands = new HashMap<String, SpecialExecuter>();
	private static final DoubleMetaphone doubleMetaphone = new DoubleMetaphone();

	public static void cleanCommands() {
		commands = new HashMap<String, SpecialExecuter>();
	}

	public static SpecialExecuter getExecuterForCommand(String mCommand) {
		doubleMetaphone.setMaxCodeLen(20);
		Set<String> keySet = commands.keySet();

		String inputEncoded = doubleMetaphone.encode(mCommand);

		for (String command : keySet) {
			String commandEncoded = doubleMetaphone.encode(command);
			if (inputEncoded.startsWith(commandEncoded)) {
				return commands.get(command);
			}
		}

		return null;
	}

	public static void executeCommand(List<String> inputs, Context context,
			AudioUI uiReference) {
		doubleMetaphone.setMaxCodeLen(20);
		Boolean commandFound = false;
		Set<String> keySet = commands.keySet();
		for (String input : inputs) {
			String inputEncoded = doubleMetaphone.encode(input);

			for (String command : keySet) {
				String commandEncoded = doubleMetaphone.encode(command);
				if (inputEncoded.startsWith(commandEncoded)) {
					SpecialExecuter executer = commands.get(command);
					executer.executeCommand(inputs, context, uiReference);
					commandFound = true;
					break;
				}
			}
			if (commandFound) {
				break;
			}
		}
		if (!commandFound) {
			uiReference.speak("Sorry no command matches: " + inputs.get(0));
			uiReference.resumeState();
		}
	}

	public static Boolean checkIfCommand(List<String> inputs) {
		Set<String> keySet = commands.keySet();
		for (String input : inputs) {
			String inputEncoded = doubleMetaphone.encode(input);

			for (String command : keySet) {
				String commandEncoded = doubleMetaphone.encode(command);
				if (inputEncoded.startsWith(commandEncoded)) {
					return true;
				}
			}
		}
		return false;
	}

	public static String checkForCommand(String input) {
		Set<String> keySet = commands.keySet();
		String inputEncoded = doubleMetaphone.encode(input);

		for (String command : keySet) {
			String commandEncoded = doubleMetaphone.encode(command);
			if (inputEncoded.startsWith(commandEncoded)) {
				return command;
			}
		}
		return null;
	}

	public static void registerCommand(String phrase, SpecialExecuter executer) {
		commands.put(phrase, executer);
	}

	public static void registerMultipleCommands(String[] phrases,
			SpecialExecuter executer) {
		for (String phrase : phrases) {
			registerCommand(phrase, executer);
		}
	}

	/*
	 * private String executeCommand(int commandNumber, String[] inputWords) {
	 * String reply="Error executing command"; String restOfCommand = ""; int
	 * count=0; for (String word : inputWords) { if (count>0) //don't take first
	 * word { restOfCommand += word + " "; } count++; } switch(commandNumber) {
	 * //cancel case 0: reply = "Goodbye"; break;
	 * 
	 * //navigate case 1: if (restOfCommand.startsWith("to")) { restOfCommand =
	 * restOfCommand.substring(3); } Intent i = new Intent(Intent.ACTION_VIEW,
	 * Uri.parse("google.navigation:q=" + restOfCommand));
	 * i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
	 * Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_FROM_BACKGROUND);
	 * reply= "Navigating to " + restOfCommand; context.startActivity(i); break;
	 * //shutdown case 2: Intent intent = new Intent(context, MyService.class);
	 * context.stopService(intent); reply=""; break; //call case 3: Intent
	 * callIntent = new Intent(Intent.ACTION_CALL);
	 * callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
	 * Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_FROM_BACKGROUND);
	 * callIntent.setData(Uri.parse("tel:" + restOfCommand));
	 * context.startActivity(callIntent); } return reply; }
	 */
}
