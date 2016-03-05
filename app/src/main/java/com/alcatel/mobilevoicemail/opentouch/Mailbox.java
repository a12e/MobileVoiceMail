package com.alcatel.mobilevoicemail.opentouch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;

import com.alcatel.mobilevoicemail.App;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Mailbox {

    protected int mId;

    public Mailbox(int mailboxId) {
        this.mId = mailboxId;

        BroadcastReceiver mMessageUploadedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ArrayList<Identifier> destinations = new ArrayList<>();
                destinations.add(new Identifier(OpenTouchClient.getInstance().getLoginName()));
                sendMessage(destinations, false, intent.getStringExtra("url"));
            }
        };
        App.getContext().registerReceiver(mMessageUploadedReceiver, new IntentFilter("MESSAGE_UPLOADED"));
    }

    private class FetchMessagesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Log.i(getClass().getSimpleName(), "Fetching messages");

            try {
                // Get all messages from this mailbox
                JSONObject messages = OpenTouchClient.getInstance().getJson("/1.0/messaging/mailboxes/" + mId + "/voicemails");
                Log.i(getClass().getSimpleName(), "Messages list " + messages);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    /* Parameters used for sendMessage */
    private class SendMessageParams {
        ArrayList<Identifier> destinations;
        boolean highPriority;
        String url;
    };

    private class SendMessageTask extends AsyncTask<SendMessageParams, Void, Void> {
        @Override
        protected Void doInBackground(SendMessageParams... params) {
            SendMessageParams parameters = params[0];

            try {
                JSONArray destinations = new JSONArray();
                for(Identifier identifier: parameters.destinations) {
                    destinations.put(identifier.toJson());
                }

                JSONObject request = new JSONObject();
                request.put("destinations", destinations);
                request.put("highPriority", parameters.highPriority);
                request.put("url", parameters.url);

                Log.i(getClass().getSimpleName(), "sendMessage request: " + request.toString());
                JSONObject response = OpenTouchClient.getInstance().requestJson("POST",
                        "/1.0/messaging/mailboxes/" + mId + "/recorder/send", request.toString().replace("\\", ""));
                Log.i(getClass().getSimpleName(), "sendMessage response: " + response.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void fetchMessages() {
        new FetchMessagesTask().execute();
    }

    public void sendMessage(ArrayList<Identifier> destinations, boolean highPriority, String url) {
        SendMessageParams params = new SendMessageParams();
        params.destinations = destinations;
        params.highPriority = highPriority;
        params.url = url;
        new SendMessageTask().execute(params);
    }
}
