package com.alcatel.mobilevoicemail.opentouch.messages;

import android.os.AsyncTask;
import android.util.Log;

import com.alcatel.mobilevoicemail.opentouch.OpenTouchClient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MessageRepository {

    private static MessageRepository mInstance = null;

    public static MessageRepository getInstance() {
        if(mInstance == null)
            mInstance = new MessageRepository();
        return mInstance;
    }

    private class FetchReceivedMessagesTask extends AsyncTask<Void, Void, List<Integer>> {
        @Override
        protected List<Integer> doInBackground(Void... params) {
            Log.i(getClass().getSimpleName(), "Fetching messages");
            List<Integer> list = new ArrayList<Integer>();

            try {
                HttpsURLConnection connection = OpenTouchClient.getInstance().createHTTPSConnection("GET", "/1.0/messaging/mailboxes");
                connection.setDoInput(true);
                OpenTouchClient.printHTTPHeaders(getClass().getSimpleName(), connection);
                connection.connect();
                OpenTouchClient.printHTTPHeaders(getClass().getSimpleName(), connection);
                if(connection.getResponseCode() == 200) {
                    BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
                    String result = OpenTouchClient.readFromStream(is);
                    Log.d(getClass().getSimpleName(), "RESULT " + result);
                }
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return list;
        }
        @Override
        protected void onPostExecute(List<Integer> integers) {
            super.onPostExecute(integers);
            Log.d(getClass().getSimpleName(), "Mailboxes are : " + integers.toString());
        }
    }

    public void fetchReceivedMessages() {
        new FetchReceivedMessagesTask().execute();
    }
}
