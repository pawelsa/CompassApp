package pl.pawel.compass.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static java.lang.Math.abs;

public class CompassListener implements SensorEventListener {
    private static final int DEGREE_THRESHOLD = 2;
    private final float[] rotationMatrix = new float[16];
    private final float[] inclinationMatrix = new float[16];
    private final float[] orientationVector = new float[3];
    private final UpdateDegreesListener updateDegreesListener;
    private float[] gravityVector = new float[3];
    private float[] geomagneticVector = new float[3];
    private float degrees = 0f;

    public CompassListener(UpdateDegreesListener updateDegreesListener) {
        this.updateDegreesListener = updateDegreesListener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isEventAndAccuracyNotGoodEnough(event)) {
            return;
        }

        handleNewSensorData(event);

        boolean successfullyUpdatedMatrices = SensorManager.getRotationMatrix(
                rotationMatrix, inclinationMatrix,
                gravityVector, geomagneticVector
        );
        if (successfullyUpdatedMatrices) {
            SensorManager.getOrientation(rotationMatrix, orientationVector);
            float newDegrees = (float) Math.toDegrees(orientationVector[0]);
            if (abs(newDegrees - degrees) > DEGREE_THRESHOLD) {
                degrees = newDegrees;
                updateDegreesListener.update(degrees);
            }
        }
    }

    private void handleNewSensorData(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravityVector = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagneticVector = event.values.clone();
        }
    }

    private boolean isEventAndAccuracyNotGoodEnough(SensorEvent event) {
        return event == null || event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    interface UpdateDegreesListener {
        void update(float degrees);
    }
}