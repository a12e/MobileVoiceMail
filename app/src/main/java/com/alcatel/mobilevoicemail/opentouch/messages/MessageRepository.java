package com.alcatel.mobilevoicemail.opentouch.messages;

import android.os.AsyncTask;
import android.util.Log;

import com.alcatel.mobilevoicemail.opentouch.OpenTouchClient;

import org.json.JSONArray;
import org.json.JSONObject;

public class MessageRepository {

    private static MessageRepository mInstance = null;

    public static MessageRepository getInstance() {
        if(mInstance == null)
            mInstance = new MessageRepository();
        return mInstance;
    }

    private class FetchReceivedMessagesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Log.i(getClass().getSimpleName(), "Fetching messages");

            try {
                JSONObject response = OpenTouchClient.getInstance().getJson("/1.0/messaging/mailboxes");
                JSONArray mailBoxes = response.getJSONArray("mailboxes");
                for(int i = 0; i < mailBoxes.length(); i++) {
                    // Get all messages from this mailbox
                    int mailboxId = mailBoxes.getJSONObject(i).getInt("id");
                    Log.i(getClass().getSimpleName(), "Fetching all messages from mailbox " + mailBoxes.getJSONObject(i).getString("name"));

                    response = OpenTouchClient.getInstance().getJson("/1.0/messaging/mailboxes/" + mailboxId + "/voicemails");
                    Log.i(getClass().getSimpleName(), "Messages list " + response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void fetchReceivedMessages() {
        new FetchReceivedMessagesTask().execute();
    }
}
