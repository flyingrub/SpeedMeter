package fly.speedmeter.grub;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

import fly.speedmeter.grub.R;

public class Settings extends Activity {
	
    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_settings );
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }
}
