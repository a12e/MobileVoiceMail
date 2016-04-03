package com.alcatel.mobilevoicemail;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alcatel.mobilevoicemail.opentouch.Identifier;
import com.alcatel.mobilevoicemail.opentouch.OpenTouchClient;

import org.json.JSONObject;

import java.util.ArrayList;

// Activité où on affiche les résultats de la requete pour chercher un contact de l'opentouch
public class SearchContactResultsActivity extends Activity {

    ListView listview;
    ArrayList resultsSearch = new ArrayList(); // ArrayList contenant les résultats de la recherche du contact

    /*public void getResultsSearch() {
        Intent resultsSearchIntent = new Intent("RESULT_SEARCH");
        resultsSearchIntent.putExtra("resultsSearch", resultsSearch);
        App.getContext().sendBroadcast(resultsSearchIntent);

    }*/

   /* public void SearchContactResultsActivity(){

    }*/


    public void onCreate(Bundle savedInstanceState) {
        BroadcastReceiver mMessageUploadedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ArrayList result = new ArrayList(intent.getStringArrayListExtra("resultsSearchUpdated"));
                showResults(result);
            }
        };
        App.getContext().registerReceiver(mMessageUploadedReceiver, new IntentFilter("RESULT_SEARCH_UPDATED"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultcontact);
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


    /*private void showResults(String query) { // Query contient ce qu'on a recherché comme lastname
        Log.i(getClass().getSimpleName(), "Starting getting contacts from Opentouch");
        new GetContactsOpenTouchTask().execute(query);
        listview = (ListView)findViewById(R.id.listView);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_search_contacts, R.id.listText, resultsSearch);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new ListClickHandler());
    }*/

    private void showResults(ArrayList list) { // Query contient ce qu'on a recherché comme lastname
        Log.d(getClass().getSimpleName(), "test *-**-*-*-*-*--*-*-****-*--*-*-*-*" + String.valueOf(list));
        listview = (ListView)findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_search_contacts, R.id.listText, list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new ListClickHandler());
    }


    public class ListClickHandler implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
            TextView listText = (TextView) view.findViewById(R.id.listText);
            String text = listText.getText().toString();
            System.out.println("Ajouter le contact: " + text);
        }
    }
}
