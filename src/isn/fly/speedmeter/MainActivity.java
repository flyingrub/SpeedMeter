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
    
	static boolean firstime = true;
	boolean accExist = false;
	double CurrentSpeed = 0;
	double MaxSpeed = 0;
	public long timeWhenStopped = 0;
	private static int Running = 0; // 0:Stopped 1:Running 2:Paused
	double currentLon=0 ;
	double currentLat=0 ;
	double lastLon = 0;
	double lastLat = 0;
	double distance = 0;
	double distanceKm = 0;
	double distanceM = 0;
	double averageSpeed = 0;
	long time;
	int satsInView = 0;
	int satsUsed = 0;
	
	Location lastlocation = new Location("last");
	Chronometer chrono;
	
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
        firstime = true;
        chrono=(Chronometer)findViewById(R.id.chrono);
        chrono.setText("--:--:--");

        // Get system services for event delivery
    	mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	
    	// Initialise the views.
    	gpsSpeed = (TextView) findViewById(R.id.gpsSpeed);
    	gpsSats = (TextView) findViewById(R.id.gpsSats);
    	gpsSpeedMax = (TextView) findViewById(R.id.gpsSpeedMax);
    	gpsAccuracy = (TextView) findViewById(R.id.gpsAccuracy);
    	gpsDistance = (TextView) findViewById(R.id.gpsDistance);
    	gpsDistanceUnit = (TextView) findViewById(R.id.gpsdistanceUnit);
    	gpsAverageSpeed = (TextView) findViewById(R.id.gpsAverageSpeed);
    	
    	// on récupère les anciennes données
		if (savedInstanceState != null){ 
			Running = savedInstanceState.getInt("SavedRunning");
			timeWhenStopped = savedInstanceState.getLong("SavedtimeWhenStopped");
			gpsSpeed.setText(savedInstanceState.getString("SavedgpsSpeed"));
			gpsSpeedMax.setText(savedInstanceState.getString("SavedgpsSpeedMax"));
			gpsAccuracy.setText(savedInstanceState.getString("SavedgpsAccuracy"));
			gpsSats.setText(savedInstanceState.getString("SavedgpsSats"));
			gpsDistance.setText(savedInstanceState.getString("SavedgpsDistance"));
			gpsDistanceUnit.setText(savedInstanceState.getString("SavedgpsDistanceUnit"));
			gpsAverageSpeed.setText(savedInstanceState.getString("SavedgpsAverageSpeed"));
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
    	  savedInstanceState.putString("SavedgpsSpeedMax", gpsSpeedMax.getText().toString());
    	  savedInstanceState.putString("SavedgpsSpeed", gpsSpeed.getText().toString());
    	  savedInstanceState.putString("SavedgpsAccuracy", gpsAccuracy.getText().toString());
    	  savedInstanceState.putString("SavedgpsSats", gpsSats.getText().toString());
    	  savedInstanceState.putString("Savedchrono", chrono.getText().toString());
    	  savedInstanceState.putString("SavedgpsDistance", gpsDistance.getText().toString());
    	  savedInstanceState.putString("SavedgpsDistanceUnit", gpsDistanceUnit.getText().toString());
    	  savedInstanceState.putString("SavedgpsAverageSpeed", gpsAverageSpeed.getText().toString());
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
            case R.id.action_stop:
                stopRun();
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
    
    /*****************************************************************
     * Called when the status of the GPS changes. Updates GPS display.
     *****************************************************************/
    public void onGpsStatusChanged (int event) {
    	switch (event) {
    	case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
    		GpsStatus status = mLocationManager.getGpsStatus(null);
    		satsInView = 0;
    		satsUsed = 0;
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
        		if (Running == 1){
        		startRun(); // met en pause
        		}
        	    Intent localIntent = new Intent(this, PermissionGps.class);
        	    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	    startActivity(localIntent); 
        	}
        }

    }
    
    /****************************************
     * Called when the location changes. 
     ****************************************/
    
    public void onLocationChanged(Location location) {
    	
        if (location.hasSpeed()) {
        	CurrentSpeed = location.getSpeed() * 3.6;
        	gpsSpeed.setText(String.format("%.0f", CurrentSpeed));
        }
        
        if (location.hasAccuracy()) {
        	accExist=true;
        	gpsAccuracy.setText(String.format("%.0f", location.getAccuracy()));
        }else{
        	accExist=false;
        	gpsAccuracy.setText(R.string.value_none);
        }
        
    }
    
    /****************************************
     * Called by the service. 
     ****************************************/
    public void updateGpsview(Double distanceM, Double distanceKm){
    	Log.i("Mainactivity", "updateGpsview done");
		if (distanceKm < 1){
			gpsDistance.setText(String.format("%.0f", distanceM));
			gpsDistanceUnit.setText(R.string.gps_distance_unit1);
		}else{
			gpsDistance.setText(String.format("%.3f", distanceKm));
			gpsDistanceUnit.setText(R.string.gps_distance_unit2);
		}
		
		averageSpeed = (distanceM / (time / 1000)) * 3.7 ;
		Log.i("Main", "averageSpeed=" + averageSpeed);
		gpsAverageSpeed.setText(String.format("%.1f", averageSpeed));
	  
        if (CurrentSpeed > MaxSpeed) {
        	MaxSpeed = CurrentSpeed;
        	gpsSpeedMax.setText(String.format("%.0f", MaxSpeed));
        }
        firstime=false;
    }
        
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
    

    public static int Running(){
    	return Running;
    }
    
    public static boolean firstime(){
    	return firstime;
    }
    
    public void startRun(){
    	if (accExist){
	    	if (Running == 1){ // Was Running
	    		Toast.makeText(getApplicationContext(), R.string.pause, Toast.LENGTH_SHORT).show();
	    		Running=2;
	    		invalidateOptionsMenu();
				timeWhenStopped = chrono.getBase() - SystemClock.elapsedRealtime();
				chrono.stop();
	    	}else if (Running == 2){ // Was Paused
	    		Running=1;
	    		firstime =true;
	    		invalidateOptionsMenu();
	    		Toast.makeText(getApplicationContext(), R.string.resume, Toast.LENGTH_SHORT).show();
	    		chrono.setBase(SystemClock.elapsedRealtime()+ timeWhenStopped);
	    	    chrono.start();
	    	}else if (Running == 0){ // was Stopped
	    		Running=1;
	    		firstime = true;
	    		invalidateOptionsMenu();
	    		Toast.makeText(getApplicationContext(), R.string.start, Toast.LENGTH_SHORT).show();
	    	    chrono.setBase(SystemClock.elapsedRealtime());
	    	    chrono.start();
	    	    startService(new Intent(getBaseContext(), GpsServices.class));
	    	}
    	}else{
    		Toast.makeText(getApplicationContext(), R.string.no_sats, Toast.LENGTH_SHORT).show();
    	}
    }
   
    public void saveRun(){
    	if (Running == 0){
    		Toast.makeText(getApplicationContext(), R.string.start_first, Toast.LENGTH_SHORT).show();
    	}else{

    	}
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
       super.onNewIntent(intent);
       if(intent.getStringExtra("stopRun").equals("stopRun")){
    	   stopRun();
       }
       if(intent.getStringExtra("startRun").equals("startRun")){
    	   startRun();
       }
    }
    
    public void stopRun() {
    	if (Running == 0 | Running == 2){
    		Toast.makeText(getApplicationContext(), R.string.stop, Toast.LENGTH_SHORT).show();
    		
    		chrono.setBase(SystemClock.elapsedRealtime());
    		chrono.setText("--:--:--");
    		timeWhenStopped = 0;
    		distanceM=0;
    		MaxSpeed=0;
    		averageSpeed=0;
    		firstime=true;
    		Running = 0;
    		
    		gpsSpeedMax.setText(R.string.value_none);
    		gpsAverageSpeed.setText(R.string.value_none);
    		gpsDistance.setText(R.string.value_none);
    		stopService(new Intent(getBaseContext(), GpsServices.class));
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


