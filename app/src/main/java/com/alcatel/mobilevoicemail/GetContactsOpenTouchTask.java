package com.alcatel.mobilevoicemail;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.alcatel.mobilevoicemail.opentouch.OpenTouchClient;

import org.json.JSONArray;
import org.json.JSONObject;

//tache qui s'occupe de recuperer le contact recherché dans la liste de contacts de l'opentouch
class GetContactsOpenTouchTask extends AsyncTask<String, Void, Void> {
    protected Void doInBackground(String... params) {
        if(params.length != 1) {
            throw new IllegalArgumentException("Only one parameter please");
        }
        try {
            OpenTouchClient.getInstance().requestJson("POST", "/1.0/directory/search", "{\"directory\":null,\"limit\":0,\"filter\":{\"field\":\"lastName\",\"operand\":\"" + params[0] + "\",\"operation\":\"CONTAIN\"}}");

            JSONArray resultElements = OpenTouchClient.getInstance().getJson("/1.0/directory/search").getJSONArray("resultElements");
            JSONArray contactList = new JSONArray();

            for(int i = 0; i < resultElements.length(); i++) {
                JSONArray phonebookContacts = resultElements.getJSONObject(i).getJSONArray("contacts");
                for(int j = 0; j < phonebookContacts.length(); j++) {
                    contactList.put(phonebookContacts.getJSONObject(j));
                }
            }

            Intent resultsSearchIntent = new Intent(SearchContactResultsActivity.INTENT_SEARCH_RESULT);
            resultsSearchIntent.putExtra(SearchContactResultsActivity.INTENT_EXTRA_RESULT, contactList.toString());
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