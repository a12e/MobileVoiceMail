package com.alcatel.mobilevoicemail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;

import com.alcatel.mobilevoicemail.opentouch.Identifier;
import com.alcatel.mobilevoicemail.opentouch.OpenTouchClient;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Dylan on 20/03/2016.
 */
//tache qui s'occupe de recuperer le contact recherché dans la liste de contacts de l'opentouch
class GetContactsOpenTouchTask extends AsyncTask<String, Void, Void> {
    ArrayList resultArray = new ArrayList();

    protected Void doInBackground(String... params) {
        if(params.length != 1) {
            throw new IllegalArgumentException("Only one parameter");
        }
        try {
            OpenTouchClient.getInstance().requestJson("POST", "/1.0/directory/search", "{\"directory\":null,\"limit\":0,\"filter\":{\"field\":\"lastName\",\"operand\":\"" + params[0] + "\",\"operation\":\"CONTAIN\"}}");

            JSONObject contacts = OpenTouchClient.getInstance().getJson("/1.0/directory/search");
            int sizeListContacts = contacts.getJSONArray("resultElements").length();
            int i = 0;

            for(i = 0;i<sizeListContacts;i++){
                String firstname = contacts.getJSONArray("resultElements").getJSONObject(i).getJSONArray("contacts").getJSONObject(0).getString("firstName");
                String lastname = contacts.getJSONArray("resultElements").getJSONObject(i).getJSONArray("contacts").getJSONObject(0).getString("lastName");
                resultArray.add(firstname + " " + lastname); // Ajoute le nom/prenom à la liste affichant le resultat
            }
            Intent resultsSearchIntent = new Intent("RESULT_SEARCH_UPDATED");
            resultsSearchIntent.putExtra("resultsSearchUpdated", resultArray);
            App.getContext().sendBroadcast(resultsSearchIntent);


            return null;
        } catch (Exception e) {
            // Connection error
            Log.e(getClass().getSimpleName(), "Get contacts error");
            Log.e(getClass().getSimpleName(), e.toString());
            e.printStackTrace();
            App.getContext().sendBroadcast(new Intent("GETCONTACTS_ERROR"));
        }
        return null;
    }
}