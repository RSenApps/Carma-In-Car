package com.RSen.InCar;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

public class WaveController implements SensorEventListener {
	private WaveControlListener listener;
	private SensorManager mSensorManager;
	private Sensor mProximity;

	private double distanceThreshold;
	private static final int timeToCompleteWave = 3000;
	// false = far, true = near
	private boolean lastMeasurement = false;
	private int numberOfChangesSoFar = 0; // need 4 total to complete wave

	public WaveController(Context context, WaveControlListener listener) {
		this.listener = listener;
		mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (mProximity == null) {
			Toast.makeText(context,
					"Sorry your device does not support Wave control",
					Toast.LENGTH_LONG).show();
			return;
		}
		distanceThreshold = mProximity.getMaximumRange() * .9;
		mSensorManager.registerListener(this, mProximity,
				SensorManager.SENSOR_DELAY_UI);
	}

	public void stop() {
		try {
			mSensorManager.unregisterListener(this);
		} catch (Exception e) {
		}
	}

	@Override
	public final void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public final void onSensorChanged(SensorEvent event) {

		float distance = event.values[0];
		if (distance > distanceThreshold && lastMeasurement) {
			lastMeasurement = false;
			numberOfChangesSoFar++;
		} else if (distance <= distanceThreshold && !lastMeasurement) {
			lastMeasurement = true;
			numberOfChangesSoFar++;
		}
		if (numberOfChangesSoFar >= 4) {
			listener.waveControlActivated();
			reset();
		} else if (numberOfChangesSoFar == 1) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(timeToCompleteWave);
					} catch (InterruptedException e) {
					}
					Log.d("wave", "resetting");
					reset();
				}
			}).start();
		}
		Log.d("wave", "number of changes:" + numberOfChangesSoFar);
	}

	private void reset() {
		numberOfChangesSoFar = 0;
		lastMeasurement = false;
	}

}
