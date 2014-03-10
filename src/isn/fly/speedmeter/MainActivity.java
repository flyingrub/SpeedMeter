package isn.fly.speedmeter;

import android.app.Activity;
import android.content.Context;
//import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus.Listener;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public final class MainActivity extends Activity implements LocationListener, Listener
{

	private LocationManager mLocationManager;
	//private SensorManager mSensorManager;
    
	double CurrentSpeed = 0;
	double MaxSpeed = 0;
	
	protected static TextView gpsSpeed;
	protected static TextView gpsAccuracy;
	protected static TextView gpsSats;
	protected static TextView gpsSpeedMax;
    
    // Called when the activity is first created.
    @Override
    public void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        // Get system services for event delivery
        //mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); 
    	mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    	// Initialise the views.
    	gpsSpeed = (TextView) findViewById(R.id.gpsSpeed);
    	gpsSats = (TextView) findViewById(R.id.gpsSats);
    	gpsSpeedMax = (TextView) findViewById(R.id.gpsSpeedMax);
    	gpsAccuracy = (TextView) findViewById(R.id.gpsAccuracy);

    }
    
    @Override
    protected void onResume() {
        super.onResume();
        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        if (mLocationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            Log.w("MainActivity", "No GPS location provider found. GPS data display will not be available.");
        }
        mLocationManager.addGpsStatusListener(this);
    }
    /**
     * Called when the status of the GPS changes. Updates GPS display.
     */
    public void onGpsStatusChanged (int event) {
    		GpsStatus status = mLocationManager.getGpsStatus(null);
    		int satsInView = 0;
    		int satsUsed = 0;
    		Iterable<GpsSatellite> sats = status.getSatellites();
    		for (GpsSatellite sat : sats) {
    			satsInView++;
    			if (sat.usedInFix()) {
    				satsUsed++;
    			}
    		}
    		gpsSats.setText(String.valueOf(satsUsed) + "/" + String.valueOf(satsInView));
    }
    
    /**
     * Called when the location changes. Updates GPS display.
     */
    public void onLocationChanged(Location location) {
    	// Called when a new location is found by the location provider.
    	
	    	if (location.hasAccuracy()) {
	    		gpsAccuracy.setText(String.format("%.0f", location.getAccuracy()));
	    	} else {
	    		gpsAccuracy.setText(getString(R.string.value_none));
	    	}

	    	if (location.hasSpeed()) {
	    		CurrentSpeed = location.getSpeed() * 3.6;
	    		gpsSpeed.setText(String.format("%.0f", CurrentSpeed));
	    		
	    		if (CurrentSpeed > MaxSpeed) {
	    			MaxSpeed = CurrentSpeed;
	    			gpsSpeedMax.setText(String.format("%.0f", MaxSpeed));
	    		}
	    		
	    	} else {
	    		gpsSpeed.setText(getString(R.string.value_none));
	    		gpsSpeedMax.setText(getString(R.string.value_none));
	    	}

    }
    
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
    public void onAccuracyChanged(int arg0, int arg1) {}
    public void onSensorChanged(int arg0, float[] arg1) {}

    
    @Override
    protected void onStop() {
    	mLocationManager.removeUpdates(this);
    	mLocationManager.removeGpsStatusListener(this);
        super.onStop();
    }
}



