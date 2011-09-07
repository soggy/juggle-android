package gy.sog.Juggler;

import android.hardware.SensorManager;

public class Hand {
    protected int samples;
    protected int lastaccelinx, lastorientinx;
    protected double[][] acceldata;
    protected double[][] orientdata;
    //protected Balls[]   balls;

    public Hand(int s) {
        samples = s;
        lastaccelinx = 0;
        lastorientinx = 0;
        acceldata  = new double[4][samples]; // time, x, y, z
        orientdata = new double[4][samples]; // time, azimuth, pitch, roll
        for (int i=0; i<4; i++) {
            for (int j = 0; j<samples; j++) {
                acceldata[i][j] = 0.0;
                orientdata[i][j] = 0.0;
            }
        }
    }

    public int detectAction() {
        // look for an inflection point, where the change in magnitude 
        // of the acceleration goes from increasing to decreasing or vice versa, and the magnitude difference is > some 
        // threshold
        int previnx = lastaccelinx - 1;
        if (previnx < 0) previnx += samples;
        if (acceldata[1][lastaccelinx] - acceldata[1][previnx] > 5)
            return 1;
	return 0;
    }

    public void updateSensorData(int sensor, float t, float[] values) {
	switch (sensor) {
	case SensorManager.SENSOR_ACCELEROMETER:
            acceldata[0][lastaccelinx] = t;
            acceldata[1][lastaccelinx] = values[0];
            acceldata[2][lastaccelinx] = values[1];
            acceldata[3][lastaccelinx] = values[2];
            lastaccelinx = (lastaccelinx + 1) % samples;
	    break;
	case SensorManager.SENSOR_ORIENTATION:
            orientdata[0][lastorientinx] = t;
            orientdata[1][lastorientinx] = values[0];
            orientdata[2][lastorientinx] = values[1];
            orientdata[3][lastorientinx] = values[2];
            lastorientinx = (lastorientinx + 1) % samples;
	    break;
	default:
	    break;
	}
    }
}
