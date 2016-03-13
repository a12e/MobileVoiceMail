package com.alcatel.mobilevoicemail;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alcatel.mobilevoicemail.opentouch.OpenTouchClient;

import java.util.ArrayList;

// Activité où on affiche les résultats de la requete pour chercher un contact de l'opentouch
public class SearchContactResultsActivity extends Activity {

    ListView listview;
    static ArrayList resultsSearch = new ArrayList(); // ArrayList contenant les résultats de la recherche du contact

    public static ArrayList getResultsSearch() {
        return resultsSearch;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_resultcontact);
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            showResults(query);
        }
    }

    private void showResults(String query) { // Query contient ce qu'on a recherché comme lastname
        OpenTouchClient.getInstance().getContactOpenTouch(query);
        listview = (ListView)findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_search_contacts, R.id.listText, resultsSearch);
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
