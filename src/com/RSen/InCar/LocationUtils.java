package com.RSen.InCar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.google.gson.Gson;

public class LocationUtils {
	private static Location getBestLocation(Context context) {
		Location gpslocation = getLocationByProvider(
				LocationManager.GPS_PROVIDER, context);
		Location networkLocation = getLocationByProvider(
				LocationManager.NETWORK_PROVIDER, context);

		// if we have only one location available, the choice is easy
		if (gpslocation == null) {
			return networkLocation;
		}
		if (networkLocation == null) {
			return gpslocation;
		}

		// a locationupdate is considered 'old' if its older than the configured
		// update interval. this means, we didn't get a
		// update from this provider since the last check
		long old = 600000; // 10 minutes
		boolean gpsIsOld = (gpslocation.getTime() < old);
		boolean networkIsOld = (networkLocation.getTime() < old);

		// gps is current and available, gps is better than network
		if (!gpsIsOld) {
			return gpslocation;
		}

		// gps is old, we can't trust it. use network location
		if (!networkIsOld) {
			return networkLocation;
		}

		// both are old return the newer of those two
		if (gpslocation.getTime() > networkLocation.getTime()) {
			return gpslocation;
		} else {
			return networkLocation;
		}
	}

	/**
	 * get the last known location from a specific provider (network/gps)
	 */
	private static Location getLocationByProvider(String provider,
			Context context) {
		Location location = null;

		LocationManager locationManager = (LocationManager) context
				.getApplicationContext().getSystemService(
						Context.LOCATION_SERVICE);

		try {
			if (locationManager.isProviderEnabled(provider)) {

				location = locationManager.getLastKnownLocation(provider);

			}
		} catch (IllegalArgumentException e) {

		}
		return location;
	}

	public static String getTimeToDestination(String destination,
			Context context) {
		DistanceMatrixData distanceMatrixData = getDistanceMatrixData(
				destination, context);
		if (distanceMatrixData == null) {
			return null;
		}
		if (!distanceMatrixData.status.matches("OK")) {
			return null;
		}
		return distanceMatrixData.rows.get(0).elements.get(0).duration.text;
	}

	public static String getETA(String destination, Context context) {
		int seconds = (int) getETATime(destination, context);
		if (seconds == -1) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, seconds);
		SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getTimeInstance();

		sdf.applyPattern("h:mm a");
		return sdf.format(cal.getTime());
	}

	public static long getETATime(String destination, Context context) {
		DistanceMatrixData distanceMatrixData = getDistanceMatrixData(
				destination, context);
		if (distanceMatrixData == null) {
			return -1;
		}
		if (!distanceMatrixData.status.matches("OK")) {
			return -1;
		}
		long seconds = distanceMatrixData.rows.get(0).elements.get(0).duration.value;
		return seconds;

	}

	public static String getDistanceToDestination(String destination,
			Context context) {
		DistanceMatrixData distanceMatrixData = getDistanceMatrixData(
				destination, context);
		if (distanceMatrixData == null) {
			return null;
		}
		if (!distanceMatrixData.status.matches("OK")) {
			return null;
		}
		return distanceMatrixData.rows.get(0).elements.get(0).distance.text;
	}

	private static DistanceMatrixData getDistanceMatrixData(String destination,
			Context context) {
		Location location = getBestLocation(context);
		String json = "";
		try {
			String urlString = "http://maps.googleapis.com/maps/api/distancematrix/json?origins="
					+ location.getLatitude()
					+ ","
					+ location.getLongitude()
					+ "&destinations="
					+ URLEncoder.encode(destination)
					+ "&units=imperial" + "&sensor=true";

			URL url = new URL(urlString);

			InputStream is = url.openConnection().getInputStream();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));

			String line = null;
			while ((line = reader.readLine()) != null) {
				json += line + "\n";
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (json.matches("null\n")) {
			return null;
		}

		Gson gson = new Gson();
		return gson.fromJson(json, DistanceMatrixData.class);
	}

	public static String[] validateLocationWithWebService(
			String locationString, Context context) {
		Location location = getBestLocation(context);
		String json = "";
		try {
			String urlString = "http://tangoincar.appspot.com/retrievecoordinates?query="
					+ URLEncoder.encode(locationString)
					+ "&location="
					+ location.getLatitude() + "," + location.getLongitude();

			URL url = new URL(urlString);

			InputStream is = url.openConnection().getInputStream();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));

			String line = null;
			while ((line = reader.readLine()) != null) {
				json += line + "\n";
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (json.matches("null\n")) {
			return null;
		}

		Gson gson = new Gson();
		String[] response = gson.fromJson(json, String[].class);
		return response;
	}
}
