package SpeedoMeterApp.main;

import static android.telephony.PhoneStateListener.LISTEN_NONE;

import java.util.Locale;

import com.vonglasow.michael.satstat.R;
import com.vonglasow.michael.satstat.widgets.GpsSnrView;
import com.vonglasow.michael.satstat.widgets.GpsStatusView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

public final class Speedometer extends Activity implements LocationListener, Listener, SensorListener
{
	private LocationManager mLocationManager;
	private SensorManager mSensorManager;
	private Sensor mAccSensor;
	
	
	final int update_interval = 500; // milliseconds
    
    // Data shown to user
    float speed = 0.0f;
    float speed_max = 0.0f;
    
    int num_updates = 0; // GPS update counter
    int no_loc = 0; // Number of null GPS updates
    int no_speed = 0; // Number of GPS updates which don't have speed
    
    LocationManager loc_mgr;
    TextView text;
    
    // Called when the activity is first created.
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );
        
        text = (TextView) findViewById( R.id.speed_text );
        update_speed( 0.0f );
        loc_mgr = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        loc_mgr.requestLocationUpdates( LocationManager.GPS_PROVIDER, update_interval, 0.0f, this );
    }
    
    @SuppressLint("DefaultLocale") void update_speed( float x )
    {
        speed = x;
        if ( x > speed_max )
            speed_max = x;
        String s = String.format(Locale.getDefault(), "\n"
                + "Vitesse actuelle:\n"
                + "%.2f m/s\n"
                + "%.0f km/h\n"
                + "\n"
                + "Vitesse Max:\n"
                + "%.2f m/s\n"
                + "%.0f km/h\n"
                + "\n"
                + "Updates: %d\n"
                + "Noloc: %d\n"
                + "Nospeed: %d\n",
                speed, speed * 3.6f,
                speed_max, speed_max * 3.6f,
                num_updates,
                no_loc,
                no_speed);
        text.setText( s );
    }

    public void onLocationChanged(Location location){
    	if (location.hasAccuracy()){
    		gpsAccuracy.setText(String.format("%.0f", location.getAccuracy()));
    	} else {
    		gpsAccuracy.setText(getString(R.string.value_none));
    	}
        update_speed( location.getSpeed() );
    }

	public void onProviderDisabled(String arg0) {}

	public void onProviderEnabled(String arg0) {}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

	public void onAccuracyChanged(int arg0, int arg1) {}

	public void onSensorChanged(int arg0, float[] arg1) {}
}
