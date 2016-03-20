package com.alcatel.mobilevoicemail;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.alcatel.mobilevoicemail.opentouch.OpenTouchClient;

import java.util.ArrayList;
import java.util.List;

public class ThreadsActivity extends ActionBarActivity {

    private ListView mThreadsListView;

    private class ThreadsAdapter extends ArrayAdapter<String> {
        public ThreadsAdapter(List<String> objects) {
            super(ThreadsActivity.this, 0, objects);
        }

        // Ce sac contient les vues associées à un item
        class ThreadViewBag {
            public ImageView photo;
            public TextView identifier;
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Android nous fournit un convertView null lorsqu'il nous demande de la créer
            // dans le cas contraire, cela veux dire qu'il nous fournit une vue recyclée
            if(convertView == null) {
                // Nous récupérons notre threads_list_item via un LayoutInflater,
                // qui va charger un layout xml dans un objet View
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.threads_list_item, parent, false);
            }

            ThreadViewBag viewBag = (ThreadViewBag)convertView.getTag();
            if(viewBag == null) {
                viewBag = new ThreadViewBag();
                viewBag.photo = (ImageView)convertView.findViewById(R.id.photo_image_view);
                viewBag.identifier = (TextView)convertView.findViewById(R.id.identifer_text_view);
                convertView.setTag(viewBag);
            }

            // Remplissage de la vue avec les données du modèle
            String item = getItem(position);
            viewBag.identifier.setText(item);

            // nous renvoyons notre vue à l'adapter, afin qu'il l'affiche
            // et qu'il puisse la mettre à recycler lorsqu'elle sera sortie de l'écran
            return convertView;
        }
    }

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


        // List of all threads
        mThreadsListView = (ListView)findViewById(R.id.threads_list_view);
        mThreadsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String)parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), "Clicked on " + item, Toast.LENGTH_LONG).show();
            }
        });

        // Mise à jour de la liste des threads
        updateThreadsListView();

        // Mise à jour automatique de la liste lorsqu'un message est envoyé.
        // on réagit pour cela à l'Intent MESSAGE_UPLOADED
        getApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateThreadsListView();
            }
        }, new IntentFilter("MESSAGE_UPLOADED"));
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
        int id = item.getItemId();

        //Gestion de la recherche de contacts
        if (id == R.id.search) {
            onSearchRequested();
            return true;
        }

        //Gestion de la déconnexion
        if(id == R.id.action_disconnect) {
            final Intent loginIntent = new Intent(this, LoginActivity.class);
            OpenTouchClient.getInstance().logout();
            startActivity(loginIntent);
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

    private void updateThreadsListView() {
        ArrayList<LocalVoicemail> sentVoicemails = SentMailbox.getInstance().getVoicemails();
        Log.d(getClass().getSimpleName(), "Displaying threads of " + sentVoicemails.size() + " sent messages");

        ArrayList<String> peopleNames = new ArrayList<>();
        for(LocalVoicemail voicemail: sentVoicemails) {
            peopleNames.add(voicemail.getDestination().getDisplayName());
        }

        mThreadsListView.setAdapter(new ThreadsAdapter(peopleNames));
    }
}
