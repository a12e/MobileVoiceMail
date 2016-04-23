package com.alcatel.mobilevoicemail;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alcatel.mobilevoicemail.opentouch.PartyInfo;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

// Activité où on affiche les résultats de la requete pour chercher un contact de l'opentouch
public class SearchContactResultsActivity extends ActionBarActivity {
    public static final String INTENT_SEARCH_RESULT = "SEARCH_RESULT";
    public static final String INTENT_EXTRA_RESULT = "result";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Récéption des intents contenant les résultats de recherche
        App.getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String searchResult = intent.getStringExtra(INTENT_EXTRA_RESULT);
                if(searchResult == null) throw new NullPointerException("Bad search results");
                showResults(searchResult);
            }
        }, new IntentFilter(INTENT_SEARCH_RESULT));

        setContentView(R.layout.activity_resultcontact);
        // Affichage de l'icone "retour" dans l'ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handleIntent(getIntent());
    }

    protected void handleIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i(getClass().getSimpleName(), "Starting getting contacts from Opentouch");
            new GetContactsOpenTouchTask().execute(query);
        }
    }

    private void showResults(String searchResult) {
        Log.d(getClass().getSimpleName(), "Search results = " + searchResult);

        // décodage des résultats de la recherche reçus comme un tableau JSON
        JSONArray jsonResults;
        try {
            jsonResults = new JSONArray(searchResult);

            // on les transforme en PartyInfo
            ArrayList<PartyInfo> results = new ArrayList<>();
            for(int i = 0; i < jsonResults.length(); i++) {
                results.add(PartyInfo.fromJson(jsonResults.getJSONObject(i)));
            }

            ListView listview = (ListView)findViewById(R.id.contacts_list_view);
            // refraîchissement de la liste avec les résultats
            listview.setAdapter(new ContactAdapter(results));
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    PartyInfo partyInfo = (PartyInfo)parent.getItemAtPosition(position);

                    // Ouverture de l'activité du fil des messages avec cette personne
                    Intent openThreadIntent = new Intent(SearchContactResultsActivity.this, ThreadActivity.class);
                    openThreadIntent.putExtra("phoneNumber", partyInfo.getIdentifier().getPhoneNumber());
                    startActivity(openThreadIntent);

                    // On ferme l'activité des résultats
                    SearchContactResultsActivity.this.finish();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class ContactAdapter extends ArrayAdapter<PartyInfo> {
        public ContactAdapter(List<PartyInfo> identifiers) {
            super(SearchContactResultsActivity.this, 0, identifiers);
        }

        // Ce sac contient les vues associées à un item
        class ContactViewBag {
            public TextView name;
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
                        .inflate(R.layout.contact_list_item, parent, false);
            }

            ContactViewBag viewBag = (ContactViewBag)convertView.getTag();
            if(viewBag == null) {
                viewBag = new ContactViewBag();
                viewBag.name = (TextView)convertView.findViewById(R.id.name_text_view);
                viewBag.identifier = (TextView)convertView.findViewById(R.id.identifer_text_view);
            }

            // Remplissage de la vue avec les données du modèle (PartyInfo)
            PartyInfo partyInfo = getItem(position);
            viewBag.name.setText(String.format("%s %s", partyInfo.getFirstName(), partyInfo.getLastName()));
            if(partyInfo.getIdentifier() != null)
                viewBag.identifier.setText(partyInfo.getIdentifier().getDisplayName());
            else
                viewBag.identifier.setText("");

            // nous renvoyons notre vue à l'adapter, afin qu'il l'affiche
            // et qu'il puisse la mettre à recycler lorsqu'elle sera sortie de l'écran
            return convertView;
        }
    }
}
