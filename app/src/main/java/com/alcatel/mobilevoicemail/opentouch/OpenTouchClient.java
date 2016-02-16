package com.alcatel.mobilevoicemail.opentouch;

import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.alcatel.mobilevoicemail.App;
import com.alcatel.mobilevoicemail.opentouch.exceptions.AuthenticationException;
import com.alcatel.mobilevoicemail.opentouch.exceptions.ProtocolException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class OpenTouchClient {

    private static OpenTouchClient mInstance = null;
    private String mBaseUrl = "https://tps-opentouch.u-strasbg.fr/api/rest";

    private OpenTouchClient() {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        HttpsURLConnection.setFollowRedirects(false);
    }

    public static OpenTouchClient getInstance() {
        if(mInstance == null)
            mInstance = new OpenTouchClient();
        return mInstance;
    }

    private HttpsURLConnection createHTTPSConnection(String method, String relativeUrl) throws IOException {
        URL url = new URL(mBaseUrl + relativeUrl);
        HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
        urlConnection.setRequestMethod(method);
        return urlConnection;
    }

    protected static String fromStream(InputStream in) throws IOException {
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

    protected static void printHTTPHeaders(String tag, HttpsURLConnection connection) {
        Log.d(tag, "==== REQUEST " + connection.getURL().toString());
        for(Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
            Log.d(tag, entry.getKey() + ": " + entry.getValue().toString());
        }
    }

    class LoginTask extends ExAsyncTask<String, Void, Void> {
        public LoginTask(ExceptionHandler eh) {
            super(eh);
        }

        public Void main(Object... params) throws Exception {
            if(params.length != 2) {
                throw new IllegalArgumentException();
            }
            String email = (String)params[0];
            String password = (String)params[1];

            // Phase 1
            HttpsURLConnection connection1 = createHTTPSConnection("GET", "/authenticate");
            connection1.setDoInput(true);
            connection1.connect();
            connection1.getContent();
            printHTTPHeaders(getClass().getSimpleName(), connection1);
            connection1.disconnect();

            // We expect a 302
            if(connection1.getResponseCode() != 302) {
                throw new ProtocolException("Expected a 302 response code");
            }
            String location = connection1.getHeaderField("Location");

            // Phase 2
            URL url2 = new URL(location);
            HttpsURLConnection connection2 = (HttpsURLConnection)url2.openConnection();
            String encoded = Base64.encodeToString((email + ":" + password).getBytes("UTF-8"), Base64.NO_WRAP);
            connection2.setRequestProperty("Authorization", "Basic " + encoded);
            connection2.setDoInput(true);
            connection2.connect();
            printHTTPHeaders(getClass().getSimpleName(), connection2);

            if(connection2.getResponseCode() == 401) {
                BufferedInputStream is = new BufferedInputStream(connection2.getErrorStream());
                String result = fromStream(is);
                Log.d(getClass().getSimpleName(), "RESULT " + result);
                throw new AuthenticationException("Erreur d'authentification");
            }

            connection2.disconnect();

            return null;
        }

        @Override
        protected void onSuccess(Void aVoid) {
            App.getContext().sendBroadcast(new Intent("LOGIN_SUCCESS"));
        }
    }

    public void login(String email, String password, ExceptionHandler eh) {
        Log.i(getClass().getSimpleName(), "Starting login");
        new LoginTask(eh).execute(email, password);
    }

}
