package com.alcatel.mobilevoicemail.opentouch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.alcatel.mobilevoicemail.App;
import com.alcatel.mobilevoicemail.opentouch.exceptions.ProtocolException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class Mailbox {
    public static final String INTENT_EXTRA_DESTINATION = "destination";
    public static final String INTENT_EXTRA_MESSAGE_URL = "url";

    protected int mId;
    private ArrayList<Voicemail> mVoicemails;

    public Mailbox(int mailboxId) {
        this.mId = mailboxId;
        this.mVoicemails = new ArrayList<>();

        BroadcastReceiver mMessageUploadedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ArrayList<Identifier> destinations = new ArrayList<>();
                destinations.add((Identifier)intent.getSerializableExtra(INTENT_EXTRA_DESTINATION));
                sendMessage(destinations, false, intent.getStringExtra(INTENT_EXTRA_MESSAGE_URL));
            }
        };
        App.getContext().registerReceiver(mMessageUploadedReceiver, new IntentFilter("MESSAGE_UPLOADED"));
    }

    public ArrayList<Voicemail> getVoicemails() {
        return mVoicemails;
    }

    private class FetchMessagesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Log.i(getClass().getSimpleName(), "Fetching messages for the Mailbox " + mId);

            try {
                // Get all messages from this mailbox
                JSONObject response = OpenTouchClient.getInstance().getJson("/1.0/messaging/mailboxes/" + mId + "/voicemails");
                JSONArray voicemails = response.getJSONArray("voicemails");
                for(int i = 0; i < voicemails.length(); i++) {
                    Voicemail freshVoicemail = Voicemail.fromJson(voicemails.getJSONObject(i));

                    for(Voicemail storedVoicemail: mVoicemails) {
                        if(Objects.equals(freshVoicemail.getId(), storedVoicemail.getId())) {
                            mVoicemails.remove(storedVoicemail);
                        }
                    }

                    mVoicemails.add(freshVoicemail);
                }

                Log.i(getClass().getSimpleName(), "Messages received in mailbox " + mId + " = "
                        + voicemails.toString());
                App.getContext().sendBroadcast(new Intent("MAILBOX_SYNC"));
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
                //TODO THIS IS A TEMPORARY FIX !!
                //request.put("url", "http://130.79.92.110/edison.wav");
                //request.put("url", "http://130.79.92.110/edison.wav");

                String jsonData = request.toString().replace("\\", "");
                //jsonData = jsonData.replace("phoneNumber", "loginName");
                //jsonData = jsonData.replace("1001", "tpvoip1");

                Log.e(getClass().getSimpleName(), jsonData);

                JSONObject response = OpenTouchClient.getInstance().requestJson("POST",
                        "/1.0/messaging/mailboxes/" + mId + "/recorder/send", jsonData);

                Log.i(getClass().getSimpleName(), "Response OK:" + response.toString());
                App.getContext().sendBroadcast(new Intent("MESSAGE_SENT"));
            } catch (Exception e) {
                Intent errorIntent = new Intent("ERROR");
                errorIntent.putExtra("message", e.getMessage());
                App.getContext().sendBroadcast(new Intent("MESSAGE_SENT_ERROR"));
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

    public int getId() {
        return mId;
    }
}
