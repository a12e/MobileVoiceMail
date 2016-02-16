package com.alcatel.mobilevoicemail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import com.alcatel.mobilevoicemail.opentouch.OpenTouchClient;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ThreadsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threads);

        final Intent itent = new Intent(this, RecordMessageActivity.class);
        final Button recordButton = (Button)findViewById(R.id.record_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(itent);
            }
        });

        //Gestion de la déconnexion
        final Intent itent_deconnexion = new Intent(this, LoginActivity.class);
        Button mDeconnect = (Button) findViewById(R.id.deconnexion);
        mDeconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenTouchClient.getInstance().logout();
                startActivity(itent_deconnexion);
            }
        });
    }

}
