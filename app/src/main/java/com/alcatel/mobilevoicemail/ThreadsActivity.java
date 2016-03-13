package com.alcatel.mobilevoicemail;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

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
        final Intent LoginIntent = new Intent(this, LoginActivity.class);
        Button mDeconnect = (Button) findViewById(R.id.deconnexion);
        mDeconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenTouchClient.getInstance().logout();
                startActivity(LoginIntent);
            }
        });

        //Recherche de la liste de contact
        Button mContacts = (Button) findViewById(R.id.SearchContact);
        mContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenTouchClient.getInstance().getContactsOpenTouch();
            }
        });
    }


    //Gestion de la recherche contact
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
            onSearchRequested();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSearchRequested() {
        Bundle appData = new Bundle();
        appData.putString("hello", "world");
        startSearch(null, false, null, false);
        return true;
    }
    //FIN Gestion de la recherche contact

}
