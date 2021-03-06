package com.alcatel.mobilevoicemail.opentouch;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.alcatel.mobilevoicemail.App;
import com.alcatel.mobilevoicemail.opentouch.exceptions.AuthenticationException;
import com.alcatel.mobilevoicemail.opentouch.exceptions.ProtocolException;

import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class OpenTouchClient {

    private static OpenTouchClient mInstance = null;
    private String mBaseUrl = "https://tps-opentouch.u-strasbg.fr/api/rest";
    //private String mBaseUrl = "https://192.168.1.251:4430/api/rest";
    private String mLoginName = null;
    private CookieManager mCookieStore;
    private Mailbox mDefaultMailbox;

    private OpenTouchClient() {
        mCookieStore = new CookieManager();
        CookieHandler.setDefault(mCookieStore);
        HttpsURLConnection.setFollowRedirects(false);
        TrustEveryone.trustEveryone();
    }

    public static OpenTouchClient getInstance() {
        if (mInstance == null)
            mInstance = new OpenTouchClient();
        return mInstance;
    }

    public String getLoginName() {
        return mLoginName;
    }

    public Mailbox getDefaultMailbox() {
        return mDefaultMailbox;
    }

    public HttpsURLConnection createHTTPSConnection(String method, String relativeUrl) throws IOException {
        URL url = new URL(mBaseUrl + relativeUrl);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setRequestMethod(method);
        return urlConnection;
    }

    public static String readFromStream(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append(newLine);
        }
        return out.toString();
    }

    public static void writeToStream(HttpsURLConnection connection, String data) throws IOException {
        OutputStream os = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(data);
        writer.flush();
        writer.close();
    }

    public static void printHTTPHeaders(String tag, HttpsURLConnection connection) {
        Log.d(tag, "==== REQUEST " + connection.getURL().toString());
        for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
            Log.d(tag, entry.getKey() + ": " + entry.getValue().toString());
        }
    }

    public JSONObject getJson(String relativeUrl)
            throws JSONException, IOException, HttpException, ProtocolException {
        return requestJson("GET", relativeUrl, "");
    }

    public JSONObject requestJson(String httpVerb, String relativeUrl, String postData)
            throws IOException, JSONException, HttpException, ProtocolException {
        HttpsURLConnection connection = createHTTPSConnection(httpVerb, relativeUrl);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        // We accept incoming data
        connection.setDoInput(true);
        if (!postData.isEmpty()) {
            // We open the stream to the server
            connection.setDoOutput(true);
            writeToStream(connection, postData);
        }

        connection.connect();
        String response = "{}";

        if(connection.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT
           && connection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
            try {
                BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
                response = readFromStream(is);
            } catch (FileNotFoundException fnfe) {
                Log.e(getClass().getSimpleName(), "The server returned an empty response :(");
                Log.e(getClass().getSimpleName(), "HTTP response code is " + connection.getResponseCode());
            }
        }

        if(connection.getResponseCode() != HttpURLConnection.HTTP_OK
                && connection.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT
                && connection.getResponseCode() != HttpURLConnection.HTTP_CREATED
                && response.equals("{}")) {
            BufferedInputStream is = new BufferedInputStream(connection.getErrorStream());
            response = readFromStream(is);
            JSONObject error = new JSONObject(response);
            Log.e(getClass().getSimpleName(), "httpStatus = " + error.getString("httpStatus"));
            Log.e(getClass().getSimpleName(), "helpMessage = " + error.getString("helpMessage"));
            Log.e(getClass().getSimpleName(), "type = " + error.getString("type"));
            Log.e(getClass().getSimpleName(), "innerMessage = " + error.getString("innerMessage"));
            throw new ProtocolException(error.getString("innerMessage"));
        }

        connection.disconnect();

        // Convert to JSON
        return new JSONObject(response);
    }

    class LoginTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... params) {
            try {
                if (params.length != 2) {
                    throw new IllegalArgumentException();
                }
                String email = params[0];
                String password = params[1];

                mCookieStore.getCookieStore().removeAll();

                // Phase 1
                HttpsURLConnection connection1 = createHTTPSConnection("GET", "/authenticate");
                connection1.setDoInput(true);
                connection1.connect();
                connection1.getContent();
                connection1.disconnect();

                // We expect a 302
                if (connection1.getResponseCode() != 302) {
                    throw new ProtocolException("Expected a 302 response code");
                }
                String location = connection1.getHeaderField("Location");

                // Phase 2
                URL url2 = new URL(location);
                HttpsURLConnection connection2 = (HttpsURLConnection) url2.openConnection();
                String encoded = Base64.encodeToString((email + ":" + password).getBytes("UTF-8"), Base64.NO_WRAP);
                connection2.setRequestProperty("Authorization", "Basic " + encoded);
                connection2.setDoInput(true);
                connection2.connect();

                if (connection2.getResponseCode() == 401) {
                    BufferedInputStream is = new BufferedInputStream(connection2.getErrorStream());
                    String result = readFromStream(is);
                    Log.d(getClass().getSimpleName(), "RESULT " + result);
                    throw new AuthenticationException("Erreur d'authentification");
                }

                connection2.disconnect();

                requestJson("POST", "/1.0/sessions", "{\"application\": \"MobileVoiceMail\"}");

                JSONObject logins = getJson("/1.0/logins");
                mLoginName = logins.getJSONArray("loginNames").get(0).toString();
                Log.i(getClass().getSimpleName(), "Now connected with login name " + mLoginName);

                // Fetch all mailboxes, and set the first as default mailbox
                JSONArray mailBoxes = getJson("/1.0/messaging/mailboxes").getJSONArray("mailboxes");
                Log.i(getClass().getSimpleName(), "Mailboxes = " + mailBoxes.toString());
                int defaultMailboxId = mailBoxes.getJSONObject(0).getInt("id");
                mDefaultMailbox = new Mailbox(defaultMailboxId);

                // Connection successful
                App.getContext().sendBroadcast(new Intent("LOGIN_SUCCESS"));

                new Thread(new Runnable() {
                    public void run() {
                        Subscribe subscriber = new Subscribe();
                        subscriber.subscribe();
                    }
                }, "Subscriber Thread").start();
                return null;
            } catch (Exception e) {
                // Connection error
                Log.e(getClass().getSimpleName(), "Connection error");
                Log.e(getClass().getSimpleName(), e.toString());
                App.getContext().sendBroadcast(new Intent("LOGIN_ERROR"));
            }
            return null;
        }
    }

    class LogOutTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... params) {
            try {
                // Disconnect
                HttpsURLConnection disconnect1 = createHTTPSConnection("DELETE", "/1.0/sessions");
                disconnect1.setRequestProperty("Accept", "application/json, application/xml, text/json, text/x-json, text/javascript, text/xml");
                disconnect1.setRequestProperty("Content-Type", "application/json");
                disconnect1.connect();
                mCookieStore.getCookieStore().removeAll();

                // We expect a 204
                if (disconnect1.getResponseCode() != 204) {
                    throw new ProtocolException("Expected a 204 response code");
                }

                Log.i(getClass().getSimpleName(), "Logout success");
                App.getContext().sendBroadcast(new Intent("LOGOUT_SUCCESS"));
                return null;
            } catch (Exception e) {
                // Connection error
                Log.e(getClass().getSimpleName(), "Logout error");
                Log.e(getClass().getSimpleName(), e.toString());
                App.getContext().sendBroadcast(new Intent("LOGOUT_ERROR"));
            }
            return null;
        }
    }

    public void login(String email, String password) {
        Log.i(getClass().getSimpleName(), "Starting login");
        new LoginTask().execute(email, password);
    }

    public void logout() {
        Log.i(getClass().getSimpleName(), "Starting logout");
        new LogOutTask().execute();
    }

    // Execute la requete afin de recuperer la liste des contacts contentant ce nom de famille
}