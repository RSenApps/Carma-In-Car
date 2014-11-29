package com.RSen.InCar;

import java.util.List;

import org.apache.commons.codec.language.DoubleMetaphone;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.PhoneNumberUtils;

public class ExecuterUtils {
	private static final String[] confirmationWords = new String[] { "Yes",
			"Yep", "Sure" };
	private static final DoubleMetaphone doubleMetaphone = new DoubleMetaphone();

	public static Boolean checkForConfirmation(List<String> inputs) {
		doubleMetaphone.setMaxCodeLen(15);
		for (String possibleConfirmation : inputs) {
			String possibleConfirmationEncoded = doubleMetaphone
					.encode(possibleConfirmation);
			for (String confirmationWord : confirmationWords) {
				String confirmationEncoded = doubleMetaphone
						.encode(confirmationWord);
				if (possibleConfirmationEncoded.contains(confirmationEncoded)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Boolean validateInternet(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else
			return true;
	}

	public static String getCommand(List<String> inputs, String[] commands) {
		doubleMetaphone.setMaxCodeLen(15);
		for (String input : inputs) {
			String inputEncoded = doubleMetaphone.encode(input);

			for (String command : commands) {
				String commandEncoded = doubleMetaphone.encode(command);
				if (inputEncoded.startsWith(commandEncoded)) {
					return command;
				}
			}
		}
		return null;
	}

	public static String getPhoneNumber(String input, Context context) {
		if (input == null || input.matches("")) {
			return null;
		}
		String commandInfoNumbers = replaceWordsWithNumbers(input);
		try {
			if (PhoneNumberUtils.isGlobalPhoneNumber(commandInfoNumbers)
					&& commandInfoNumbers.matches("^[+]?[0-9]{8,20}$")) {
				return commandInfoNumbers;
			}
		} catch (Exception e) {

		} finally {
			return ContactSearcher.phoneticNameToNumber(context, input);
		}
	}

	public static String getEmail(String input, Context context) {

		return ContactSearcher.phoneticNameToEmail(context, input);
	}

	private static String replaceWordsWithNumbers(String commandInfo) {
		doubleMetaphone.setMaxCodeLen(15);
		String[] commandInfoWords = commandInfo.split(" ");
		String[] numberWords = new String[] { "zero", "one", "two", "three",
				"four", "five", "six", "seven", "eight", "nine" };
		String result = "";
		if (commandInfo.trim().length() == 0) {
			return "";
		}
		for (String word : commandInfoWords) {
			try {
				Integer.parseInt(word);
				result += word;
			} catch (Exception e) {
				// word is not just numbers
				String wordEncoded = doubleMetaphone.encode(word);
				int number = 0;
				for (String numberWord : numberWords) {
					String numberWordEncoded = doubleMetaphone
							.encode(numberWord);
					if (wordEncoded.matches(numberWordEncoded)) {
						result += number;
					}
					number++;
				}
			}
		}
		return result;
	}

	public static String getCommandInfo(List<String> inputs, String[] commands) {
		doubleMetaphone.setMaxCodeLen(15);
		String commandInfo = "";
		for (String input : inputs) {
			String inputEncoded = doubleMetaphone.encode(input);
			String[] inputWords = input.split(" ");
			for (String command : commands) {
				String[] commandWords = command.split(" ");
				String commandEncoded = doubleMetaphone.encode(command);
				if (commandEncoded.matches(inputEncoded)) {
					return null;
				}
				if (commandWords.length > inputWords.length) {
					continue;
				}
				if (inputEncoded.startsWith(commandEncoded)) {

					for (int i = commandWords.length; i < inputWords.length; i++) {
						commandInfo += inputWords[i] + " ";
					}
					return commandInfo;

				}
			}
		}
		return null;

	}

	public static MessageInfo parseMessage(List<String> inputs,
			String[] commands, String defaultRecipient, Context context,
			Boolean email) {
		MessageInfo returnInfo = new MessageInfo();
		String toEncoded = doubleMetaphone.encode("to");
		String messageEncoded = doubleMetaphone.encode("message");
		String subjectEncoded = doubleMetaphone.encode("subject");
		for (String input : inputs) {
			String inputEncoded = doubleMetaphone.encode(input);

			for (String command : commands) {
				String commandEncoded = doubleMetaphone.encode(command);
				if (inputEncoded.startsWith(commandEncoded)) {
					int commandLength = command.length();
					String commandInfo = input.substring(commandLength).trim();
					String[] commandInfoWords = commandInfo.split(" ");
					if (commandInfo.matches("")) {
						continue;
					}
					boolean parsingRecipient = false;
					boolean parsingMessage = false;
					boolean parsingSubject = false;
					for (int index = 0; index < commandInfoWords.length; index++) {
						String wordEncoded = doubleMetaphone
								.encode(commandInfoWords[index]);
						if (wordEncoded.matches(toEncoded)
								&& returnInfo.recipient.matches("")) {
							parsingRecipient = true;
							parsingMessage = false;
							parsingSubject = false;
						} else if (wordEncoded.matches(messageEncoded)
								&& returnInfo.message.matches("")) {
							parsingRecipient = false;
							parsingMessage = true;
							parsingSubject = false;
						} else if (wordEncoded.matches(subjectEncoded) && email) {
							parsingRecipient = false;
							parsingMessage = false;
							parsingSubject = true;
						} else if (parsingRecipient) {
							returnInfo.recipient += commandInfoWords[index];
							if (!email) {
								returnInfo.recipient += " ";
							}
						} else if (parsingMessage) {
							returnInfo.message += commandInfoWords[index] + " ";
						} else if (parsingSubject) {
							returnInfo.subject += commandInfoWords[index] + " ";
						}

					}
					if (!email) {
						String phoneNumber = ExecuterUtils.getPhoneNumber(
								returnInfo.recipient, context);
						if (phoneNumber == null && defaultRecipient != null) {
							returnInfo.recipient = defaultRecipient;
						}
						if (phoneNumber != null) {
							returnInfo.recipient = phoneNumber;
							return returnInfo;
						}
					} else {
						if (!returnInfo.recipient.matches("")) {
							String emailAddress = ExecuterUtils.getEmail(
									returnInfo.recipient, context);

							if (emailAddress != null) {
								returnInfo.recipient = emailAddress;
								return returnInfo;
							}
						}
					}

				}
			}
		}
		return null;
	}
}
