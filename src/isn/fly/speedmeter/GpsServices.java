package isn.fly.speedmeter;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import isn.fly.speedmeter.MainActivity;

public class GpsServices extends Service implements LocationListener, Listener{
	private LocationManager mLocationManager;
	
	MainActivity MainAc = new MainActivity();
	
    int Running = 0;
	boolean firstime = true;
	double currentLon=0 ;
	double currentLat=0 ;
	double lastLon = 0;
	double lastLat = 0;
	double distance = 0;
	double distanceKm = 0;
	double distanceM = 0;
	
	Location lastlocation = new Location("last");
	
	
	@Override
	public void onCreate() {
		
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		
		Intent actionStop = new Intent(this, MainActivity.class);
		actionStop.putExtra("stopRun","stopRun");
		PendingIntent actionstop = PendingIntent.getActivity(this, 0, actionStop, 0);
		
		Intent actionPP = new Intent(this, MainActivity.class);
		actionPP.putExtra("startRun","startRun");
		PendingIntent actionpp = PendingIntent.getActivity(this, 0, actionPP, 0);
		
		Notification notification = new Notification.Builder(getBaseContext())
				.setContentTitle("Parcours en cours..."
				.toString()).setContentText("lol")
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pendingIntent)
				.addAction(R.drawable.ic_action_play, "Play/Pause", actionpp)
				.addAction(R.drawable.ic_action_stop, "Stop", actionstop)
				.build();
		
		startForeground(R.string.noti_id, notification);
		
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        } else {
            Log.w("MainActivity", "No GPS location provider found. GPS data display will not be available.");
        }
        mLocationManager.addGpsStatusListener( this);

	}

	public void onLocationChanged(Location location) {
		Running = MainActivity.Running();
		firstime = MainActivity.firstime();
		Log.i("Service", "Location Changed. Runnning = "+ Running +"; Firstime = "+ firstime);
		if (Running == 1){
				//get the current lat and long
				currentLat = location.getLatitude();
				currentLon = location.getLongitude();
	
				if (firstime){
					lastLat = currentLat;
					lastLon = currentLon;
				}
				
				lastlocation.setLatitude(lastLat);
				lastlocation.setLongitude(lastLon);
				distance = lastlocation.distanceTo(location);
				
				if (location.getAccuracy() < distance){
					distanceM = distanceM + distance;
					distanceKm = distanceM / 1000f;
					
					lastLat = currentLat;
					lastLon = currentLon;
				}
				MainAc.updateGpsview(distanceM, distanceKm);
				Log.i("Service", "updateGpsview launched. Distance ="+distanceM);
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		// If we get killed, after returning from here, restart
		return START_STICKY;
	}   
	    
	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}
	
    /* Remove the locationlistener updates when Services is stoped */
	  @Override
	  public void onDestroy() {
	    	mLocationManager.removeUpdates(this);
	    	mLocationManager.removeGpsStatusListener(this);
	    	stopForeground(true);
	  }

	@Override
	public void onGpsStatusChanged(int event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}
