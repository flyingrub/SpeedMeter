package fly.speedmeter.grub;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import fly.speedmeter.grub.R;

/**
 * Created by Ronan on 29/06/2014.
 */
public class InfosView extends Activity {

    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_infos );
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        TextView Donate = (TextView) findViewById(R.id.donate);
        Donate.setOnClickListener(myhandler1);

    }
    View.OnClickListener myhandler1 = new View.OnClickListener() {
        public void onClick(View v) {
            String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6DE8SFMJKRVK2";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }
    };
}
