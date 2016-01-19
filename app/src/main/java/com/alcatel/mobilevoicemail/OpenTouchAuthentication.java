package com.alcatel.mobilevoicemail;

import android.os.Bundle;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;

public class OpenTouchAuthentication {


    public void connectOpenTouch(final String user, final String userPass) throws JSONException {

        //TODO GERER LE CAS URL PRIV OU URL PUB OU OSEF?

        // ETAPE 1 AUTHENTICATE
        System.out.println("1 et 2)Authenticate");// ATTENTION DANGER PAS LINEAIRE faire Wait
        OpentouchClient.get("/authenticate", null, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //super.onFailure(statusCode, headers, responseString, throwable);
                if (statusCode == 302) {
                    String loginUrl = null;
                    for (Header h : headers)
                        if (h.getName().equals("Location"))
                            loginUrl = h.getValue();
                    loginUrl = loginUrl.substring(loginUrl.indexOf('/', loginUrl.indexOf('/', loginUrl.indexOf('/') + 1) + 1));//on coupe au 3eme / car on a une base donc faut couper le debut

                    loginUrl = "/../.." + loginUrl;
                    OpentouchClient.setBasicAuth(user, userPass);
                    OpentouchClient.get(loginUrl, null, new JsonHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            System.out.println("-------------FAil 1");
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                            System.out.println("------------FAil 2");
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            System.out.println("point1222");
                            System.out.println("status" + statusCode);
                            System.out.println("header" + Arrays.toString(headers));
                            if (statusCode == 401) {
                                // System.out.println("INFO" + Arrays.toString(headers));
                                System.out.println("3:Il faut s'authentifier, on a recu 401");
                            } else if (statusCode == 302) {
                                String cookieInfo = null;
                                for (Header h : headers)
                                    if (h.getName().equals("Set-Cookie"))
                                        cookieInfo = h.getValue();

                                cookieInfo = cookieInfo.substring(10);
                                cookieInfo = cookieInfo.substring(0, cookieInfo.length() - 8);

                                System.out.println("infocookie:" + cookieInfo);
                                BasicClientCookie newCookie = new BasicClientCookie("AlcUserId", cookieInfo);
                                newCookie.setVersion(1);
                                newCookie.setDomain("https://tps-opentouch.u-strasbg.fr");
                                newCookie.setPath("/");

                                OpentouchClient.get("/authenticate?version=1.0", null, new JsonHttpResponseHandler() {
                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                        System.out.println("derniere etape authentification");
                                        System.out.println("status " + statusCode);
                                        System.out.println("header " + Arrays.toString(headers));
                                    }

                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        System.out.println("Bienvenue dans Opentouch, l'authentification est validé");
                                        System.out.println("status " + statusCode);
                                        System.out.println("header " + Arrays.toString(headers));
                                        System.out.println("res " + response.toString());
                                    }
                                });

                            } else {
                                throw new ProtocolException(); // TODO pas message 401
                            }
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            System.out.println("------------succ 1");
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            System.out.println("------------succ 2");
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            System.out.println("------------succ 3");
                        }
                    });
                } else {
                    throw new ProtocolException(); // pas de 302 message
                }
            }
        });
    }





    public void postRequest(String urlFourni,RequestParams params) throws JSONException {
        OpentouchClient.post(urlFourni, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println(">>>>>>>>>POST SUCCESS1<<<<<<<<<");
                System.out.println(response.toString());
                System.out.println(">>>>>>>>>POST FINSUCCESS1<<<<<<<<<");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                System.out.println(">>>>>>>>>POST ERROR SYSTEM OBJECT<<<<<<<<<");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                System.out.println(">>>>>>>>>POST ERROR SYSTEM ARRAY<<<<<<<<<");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                JSONObject firstEvent = null;
                try {
                    firstEvent = timeline.getJSONObject(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String tweetText = null;
                try {
                    tweetText = firstEvent.getString("text");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println("=========POST PASSE ICI======");
                System.out.println(tweetText);
                System.out.println("=========POST FIN PASSE ICI======");
            }
        });
    }


    protected void onCreate(Bundle savedInstanceState) {
    }
}
