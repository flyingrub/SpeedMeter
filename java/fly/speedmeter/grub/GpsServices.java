package fly.speedmeter.grub;

import android.app.Notification;
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

import fly.speedmeter.grub.R;

public class GpsServices extends Service implements LocationListener, Listener{
	private LocationManager mLocationManager;
	
	MainActivity MainAc = new MainActivity();
	
    static int Running = 0;
	static boolean firstime = true;
	double currentLon=0 ;
	double currentLat=0 ;
	double lastLon = 0;
	double lastLat = 0;
	double distance = 0;
	double distanceKm = 0;
	double distanceM = 0;
	double locCurSpeed = 0;
	double locMaxSpeed = 0;
	
	Location lastlocation = new Location("last");
	
	
	@Override
	public void onCreate() {
		
		Notification notification = new Notification.Builder(getBaseContext())
				.setContentTitle("Parcours en cours..."
				.toString()).setContentText("Cliquez pour acceder a l'application")
				.setSmallIcon(R.drawable.ic_launcher)
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
		Log.i("Service", "Location Changed. Runnning = "+ Running +"; Firstime = "+ firstime);
		if (Running == 1){
				//get the current lat and long
				currentLat = location.getLatitude();
				currentLon = location.getLongitude();
	
				if (firstime){
					lastLat = currentLat;
					lastLon = currentLon;
				}
				firstime=false;
				
				lastlocation.setLatitude(lastLat);
				lastlocation.setLongitude(lastLon);
				distance = lastlocation.distanceTo(location);
				
				if (location.getAccuracy() < distance){
					distanceM = distanceM + distance;
					distanceKm = distanceM / 1000f;
					
					lastLat = currentLat;
					lastLon = currentLon;
				}
				
		        if (location.hasSpeed()) {
		        	locCurSpeed = location.getSpeed() * 3.6;
		        }
		        
		        if (locCurSpeed > locMaxSpeed) {
		        	locMaxSpeed = locCurSpeed;
		        }
		        
				MainAc.updateGpsview(distanceM, distanceKm, locMaxSpeed, locCurSpeed);
				Log.i("Service", "updateGpsview launched. Distance ="+distanceM + "locMaxSpeed");
		}
	}
	
    public static void setfirstime(boolean temp){
    	firstime = temp;
    }
    
    public static void setRunning(int temp){
    	Running = temp;
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
	
    /* Remove the locationlistener updates when Services is stopped */
	  @Override
	  public void onDestroy() {
	    	mLocationManager.removeUpdates(this);
	    	mLocationManager.removeGpsStatusListener(this);
	    	stopForeground(true);
	  }

	@Override
	public void onGpsStatusChanged(int event) {}
	@Override
	public void onProviderDisabled(String provider) {}
	@Override
	public void onProviderEnabled(String provider) {}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
}
