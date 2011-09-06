package gy.sog.Juggler;

import android.hardware.SensorManager;

public class Hand {
    private int samples = 200;
    protected float[]   timestamps;
    protected float[][] acceldata;
    protected float[][] orientdata;
    //protected Balls[]   balls;

    public int detectAction() {
	return 0;
    }

    public void updateSensorData(int sensor, float[] values) {
	switch (sensor) {
	case SensorManager.SENSOR_ACCELEROMETER:
	    break;
	case SensorManager.SENSOR_ORIENTATION:
	    break;
	default:
	    break;
	}
    }
}
