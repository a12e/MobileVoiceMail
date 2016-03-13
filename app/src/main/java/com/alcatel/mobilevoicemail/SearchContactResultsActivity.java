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

public class SearchContactResultsActivity extends Activity {

    ListView listview;
    // String[] foody;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_resultcontact);
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        System.out.println("trigger0");

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            System.out.println("trigger1");

            showResults(query);
        }
    }
    private void showResults(String query) {
        // Query your data set and show results
        System.out.println("trigger2!"+query);
        // faire logique ici : si pas de contact afficher : aucun contact trouvé... sinon afficher les contacts ! ez wp
        listview = (ListView)findViewById(R.id.listView);
        // Exemple avec son choix : String[] foody = {query};
        String[] foody = {"pizza", "burger", "chocolate", "ice-cream", "banana", "apple"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_view_row, R.id.listText, foody);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new ListClickHandler());
    }


    public class ListClickHandler implements AdapterView.OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
            TextView listText = (TextView) view.findViewById(R.id.listText);
            String text = listText.getText().toString();
            System.out.println("Vous avez choisi " + text);

        }
    }


}
