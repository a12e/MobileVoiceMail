package com.alcatel.mobilevoicemail;


import com.loopj.android.http.*;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLSocketFactory;

public class OpentouchClient {
    private static final String BASE_URL = "https://tps-opentouch.u-strasbg.fr/api/rest";
    private static AsyncHttpClient mClient;

    static PersistentCookieStore myCookieStore;
    public static PersistentCookieStore getMyCookieStore() {
        return myCookieStore;
    }


    public static void initialize(){
        mClient = new AsyncHttpClient();
        mClient.setLoggingLevel(LogInterface.DEBUG);
        mClient.setEnableRedirects(false);
        KeyStore trustStore = null;

        myCookieStore = new PersistentCookieStore(App.getApplication());


        if (myCookieStore == null) {
            System.out.println("Warning Context is null. Cookies wont work");
        }
        myCookieStore.clear();
        mClient.setCookieStore(myCookieStore);
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            mClient.setSSLSocketFactory(new OpentouchSSLFactory(trustStore));
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        TrustEveryone.trustEveryone();
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        mClient.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        mClient.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public static void setBasicAuth(String user, String pass){
        mClient.setBasicAuth(user,pass);
    }
}
