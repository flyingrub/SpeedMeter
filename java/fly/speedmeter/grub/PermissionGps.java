package fly.speedmeter.grub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import fly.speedmeter.grub.R;

public class PermissionGps extends Activity {

	
    private void createGpsDisabledAlert() {
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        localBuilder
            .setMessage(R.string.activate_gps)
            .setCancelable(false)
            .setPositiveButton(R.string.go_to_gps,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        PermissionGps.this.showGpsOptions();
                    }
                }
            );
        localBuilder.create().show();
    }

    private void showGpsOptions() {
        startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
        finish();
    }

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView( R.layout.activity_main );
        createGpsDisabledAlert();
    }
}