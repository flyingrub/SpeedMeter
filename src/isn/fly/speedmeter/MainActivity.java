package isn.fly.speedmeter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus.Listener;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity implements LocationListener, Listener{
	private LocationManager mLocationManager;
    
	double CurrentSpeed = 0;
	double MaxSpeed = 0;
	long timeWhenStopped = 0;
	int Running = 0; // 0:Stopped 1:Running 2:Paused
	
	Chronometer chrono;
	
	double currentLon=0 ;
	double currentLat=0 ;
	double lastLon = 0;
	double lastLat = 0;
	double distanceAB = 0;
	double distanceKm = 0;
	double distanceM = 0;
	double averageSpeed = 0;
	long time;

	protected static TextView gpsSpeed;
	protected static TextView gpsAccuracy;
	protected static TextView gpsSats;
	protected static TextView gpsSpeedMax;
	protected static TextView gpsDistance;
	protected static TextView gpsDistanceUnit;
	protected static TextView gpsAverageSpeed;
	
    
    /* Called when the activity is first created. */
    @Override
    public void onCreate( Bundle savedInstanceState ){
    	super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        
        chrono=(Chronometer)findViewById(R.id.chrono);
        chrono.setText("--:--:--");
        
        // Get system services for event delivery
    	mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    	// Initialise the views.
    	gpsSpeed = (TextView) findViewById(R.id.gpsSpeed);
    	gpsSats = (TextView) findViewById(R.id.gpsSats);
    	gpsSpeedMax = (TextView) findViewById(R.id.gpsSpeedMax);
    	gpsAccuracy = (TextView) findViewById(R.id.gpsAccuracy);
    	
    	
    	// on récupère les anciennes données
		if (savedInstanceState != null){ 
			Running = savedInstanceState.getInt("SavedRunning");
			timeWhenStopped = savedInstanceState.getLong("SavedtimeWhenStopped");
			distanceM = savedInstanceState.getLong("SaveddistanceM");
			gpsSpeed.setText(savedInstanceState.getString("SavedgpsSpeed"));
			gpsSpeedMax.setText(savedInstanceState.getString("SavedgpsSpeedMax"));
			gpsAccuracy.setText(savedInstanceState.getString("SavedgpsAccuracy"));
			gpsSats.setText(savedInstanceState.getString("SavedgpsSats"));
			chrono.setText(savedInstanceState.getString("Savedchrono"));
			if (Running == 1){
				chrono.start();
				chrono.setBase(SystemClock.elapsedRealtime()+ timeWhenStopped);
				}
    	}
        
        chrono.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {

            @Override
            public void onChronometerTick(Chronometer chrono) {
                time = SystemClock.elapsedRealtime() - chrono.getBase();
                int h   = (int)(time /3600000);
                int m = (int)(time  - h*3600000)/60000;
                int s= (int)(time  - h*3600000- m*60000)/1000 ;
                String hh = h < 10 ? "0"+h: h+"";
                String mm = m < 10 ? "0"+m: m+"";
                String ss = s < 10 ? "0"+s: s+"";
                chrono.setText(hh+":"+mm+":"+ss);
            }
        });
    }
    
    protected void onSaveInstanceState(Bundle savedInstanceState) {
    	  super.onSaveInstanceState(savedInstanceState);
    	  if (Running == 1){
    	  timeWhenStopped = chrono.getBase() - SystemClock.elapsedRealtime();
    	  }
    	  savedInstanceState.putInt("SavedRunning", Running);
    	  savedInstanceState.putLong("SavedtimeWhenStopped", timeWhenStopped);
    	  savedInstanceState.putDouble("SaveddistanceM", distanceM);
    	  savedInstanceState.putString("SavedgpsSpeedMax", gpsSpeedMax.getText().toString());
    	  savedInstanceState.putString("SavedgpsSpeed", gpsSpeed.getText().toString());
    	  savedInstanceState.putString("SavedgpsAccuracy", gpsAccuracy.getText().toString());
    	  savedInstanceState.putString("SavedgpsSats", gpsSats.getText().toString());
    	  savedInstanceState.putString("Savedchrono", chrono.getText().toString());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem play = menu.findItem(R.id.action_play);
    	if (!(Running == 1)){
    		play.setIcon(R.drawable.ic_action_play);
			
    	} else {
    		play.setIcon(R.drawable.ic_action_pause);
    	}
		return super.onPrepareOptionsMenu(menu);

    }
  
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_play:
                startRun();
                return true;
            case R.id.action_reset:
                resetRun();
                return true;
            case R.id.action_settings:
                goToSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
    protected void onResume() {
        super.onResume();
        
    	// Teste si le Gps est activé, si non il renvoie vers la classe PermissionsGps.
    	if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
    	    /* on lance notre activity (qui est une dialog) */
    	    Intent localIntent = new Intent(this, PermissionGps.class);
    	    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	    startActivity(localIntent);
    	}
    	
        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        if (mLocationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        } else {
            Log.w("MainActivity", "No GPS location provider found. GPS data display will not be available.");
        }
        mLocationManager.addGpsStatusListener(this);
    }
    
    /* Remove the locationlistener updates when Activity is stoped */
    @Override
    protected void onStop() {
    	mLocationManager.removeUpdates(this);
    	mLocationManager.removeGpsStatusListener(this);
        super.onStop();
    }
    
    /**
     * Called when the status of the GPS changes. Updates GPS display.
     */
    public void onGpsStatusChanged (int event) {
    	switch (event) {
    	case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
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
    	break;
    	case GpsStatus.GPS_EVENT_STOPPED:
        	// Teste si le Gps est activé, si non il renvoie vers la classe PermissionsGps.
        	if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        	    /* on lance notre activity (qui est une dialog) */
        	    Intent localIntent = new Intent(this, PermissionGps.class);
        	    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	    startActivity(localIntent);
        	}
        }

    }
    
    /**
     * Called when the location changes. Updates GPS display.
     */
    public void onLocationChanged(Location location) {
    	// Called when a new location is found by the location provider.
    	
        //Get last location
        Location loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        lastLat = loc.getLatitude();
        lastLon = loc.getLongitude();
        
        if (Running == 1){
        	//get the current lat and long
        	currentLat = location.getLatitude();
        	currentLon = location.getLongitude();

        	Location locationA = new Location("a");
        		locationA.setLatitude(lastLat);
        		locationA.setLongitude(lastLon);
           
        	Location locationB = new Location("b");
        		locationB.setLatitude(currentLat);
        		locationB.setLongitude(currentLon);
           
        	distanceAB = locationA.distanceTo(locationB);
        	distanceM = distanceM + distanceAB;
        	distanceKm = distanceM / 1000f;
        	if (distanceKm < 1){
        		gpsDistance.setText(String.format("%.0f", distanceM));
        		gpsDistanceUnit.setText(R.string.gps_distance_unit1);
        	}else{
        		gpsDistance.setText(String.format("%.0f", distanceKm));
        		gpsDistanceUnit.setText(R.string.gps_distance_unit2);
        	}
        	averageSpeed = distanceM * time * 3.7 / 1000;
        	gpsDistance.setText(String.format("%.0f", averageSpeed));
        }
           
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
    
    
    public void startRun(){
    	if (Running == 1){ // Was Running
    		Toast.makeText(getApplicationContext(), R.string.pause, Toast.LENGTH_SHORT).show();
    		Running=2;
    		invalidateOptionsMenu();
			timeWhenStopped = chrono.getBase() - SystemClock.elapsedRealtime();
			chrono.stop();
    	}else if (Running == 2){ // Was Paused
    		Running=1;
    		invalidateOptionsMenu();
    		Toast.makeText(getApplicationContext(), R.string.resume, Toast.LENGTH_SHORT).show();
    		chrono.setBase(SystemClock.elapsedRealtime()+ timeWhenStopped);
    	    chrono.start();
    	}else if (Running == 0){ // was Stopped
    		Running=1;
    		invalidateOptionsMenu();
    		Toast.makeText(getApplicationContext(), R.string.start, Toast.LENGTH_SHORT).show();
    	    chrono.setBase(SystemClock.elapsedRealtime());
    	    chrono.start();

    	}
    
    }
    public void saveRun(){
    	if (Running == 0){
    		Toast.makeText(getApplicationContext(), R.string.start_first, Toast.LENGTH_SHORT).show();
    	}else{
    		Toast.makeText(getApplicationContext(), R.string.stop, Toast.LENGTH_SHORT).show();
    		Running=0;
    		invalidateOptionsMenu();
    		chrono.stop();
    	}
    }
    
    public void resetRun() {
    	if (Running == 0 | Running == 2){
    		Toast.makeText(getApplicationContext(), R.string.reset, Toast.LENGTH_SHORT).show();
    		chrono.setBase(SystemClock.elapsedRealtime());
    		chrono.setText("--:--:--");
    		distanceKm=0;
    	}else{
    		Toast.makeText(getApplicationContext(), R.string.stop_first, Toast.LENGTH_SHORT).show();
    	}
	}
    
    
    public void goToSettings(){
	    Intent localIntent = new Intent(this, Settings.class);
	    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    startActivity(localIntent);
    }
}


