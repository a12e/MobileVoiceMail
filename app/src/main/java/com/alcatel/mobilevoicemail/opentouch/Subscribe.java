package com.alcatel.mobilevoicemail.opentouch;

import android.util.Log;

import com.alcatel.mobilevoicemail.opentouch.exceptions.ProtocolException;

import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Dylan on 05/03/2016.
 */
public class Subscribe {

    void subscribe() {
        try {
            String relativeUrl = "/1.0/subscriptions";
            Log.d(getClass().getSimpleName(), "==== POST :" + relativeUrl);

            // Création du JSon object
            JSONArray user_id = new JSONArray();
            user_id.put(OpenTouchClient.getInstance().getLoginName());

            JSONArray telephony = new JSONArray();
            telephony.put("telephony");

            JSONArray unifiedComLog = new JSONArray();
            unifiedComLog.put("unifiedComLog");

            JSONArray vide = new JSONArray();

            JSONObject id1 = new JSONObject();
            id1.put("ids", user_id);
            id1.put("names", telephony);
            id1.put("families", vide);
            id1.put("origins", vide);

            JSONObject id2 = new JSONObject();
            id2.put("ids", user_id);
            id2.put("names", unifiedComLog);
            id2.put("families", vide);
            id2.put("origins", vide);

            JSONArray id = new JSONArray();
            id.put(id1);
            id.put(id2);

            JSONObject selectors = new JSONObject();
            selectors.put("selectors", id);

            JSONObject filter = new JSONObject();
            filter.put("filter", selectors);
            filter.put("mode", "CHUNK");
            filter.put("format", "JSON");
            filter.put("version", "1.0");
            filter.put("timeout", 10);

            // Envoie du JsonObject
            JSONObject response = OpenTouchClient.getInstance().requestJson("POST", relativeUrl, filter.toString());
            Log.d(getClass().getSimpleName(), "======= Response : " + response);
            String subscriptionId = response.getString("subscriptionId");
            String publicPollingUrl = response.getString("publicPollingUrl");
            String privatePollingUrl = response.getString("privatePollingUrl");

            // On enlève les \ de l'url reçu
            String publicPollingUrl2 = publicPollingUrl.replace("\\", "");

            // On envoie un get à la polling url pour lui dire uq'on est prêt à recevoir les notifications
            Log.d(getClass().getSimpleName(), "======= GET : " + publicPollingUrl2);
            URL url = new URL(publicPollingUrl2/*.replace("tps-opentouch.u-strasbg.fr","192.168.1.47")*/);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoInput(true);

            urlConnection.connect();

            // Boucle de récéption des notifications
            final byte[] buffer = new byte[65536];
            BufferedInputStream is = new BufferedInputStream(urlConnection.getInputStream());
            int r;
            while ((r = is.read(buffer)) > 0) {
                Log.d(getClass().getSimpleName(), "======= Subscribe response : " + r );
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
    }
}
