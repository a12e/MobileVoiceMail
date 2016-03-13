package com.alcatel.mobilevoicemail;


import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import com.alcatel.mobilevoicemail.opentouch.OpenTouchClient;
import com.alcatel.mobilevoicemail.opentouch.Mailbox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ActionBarActivity {

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    //TODO PRENDRE EN COMPTE LURL AUSSI POUR LAUTHENTIFICATION
    private AutoCompleteTextView mPubUrl;
    private AutoCompleteTextView mPrivUrl;
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREFS_PASS = "userPassword";
    private static final String PREFS_MAIL = "userEmail";
    private static final String PREFS_PUBURL = "userPubUrl";
    private static final String PREFS_PRIVURL = "userPrivUrl";
    private static final String PREFS_REMEMBERME = "rememberMe";

    //TODO PASS EN CLAIR DANS PREF
    //Recuperer information preference
    private String getOnPreference(String prefs) {
        String prefName = null;
        try {
            SharedPreferences myPrefs2 = App.getContext().getSharedPreferences(PREFS_NAME,
                    MODE_PRIVATE);
            prefName = myPrefs2.getString(prefs,"");
        } catch (Exception e) {
            System.out.println(">>ERREUR PREF1");
        }
        return prefName;
    }

    // Modifier valeur sauvegarder sur le telephone
    private void setOnPreference(String prefs,String value) {
        try {
            SharedPreferences myPrefs = App.getContext().getSharedPreferences(PREFS_NAME,
                    MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = myPrefs.edit();
            prefsEditor.putString(prefs, value);
            prefsEditor.commit();
        } catch (Exception e) {
            System.out.println(">>ERREUR PREF2" + e);
        }
    }

    //Enlever les informations sauvegardes, par exemple lorsque rememberme est decoche
    private void cleanPreferences(){
        this.setOnPreference(PREFS_PASS,"");
        this.setOnPreference(PREFS_MAIL,"");
        this.setOnPreference(PREFS_PUBURL,"");
        this.setOnPreference(PREFS_PRIVURL,"");
        this.setOnPreference(PREFS_REMEMBERME,"");
    }

    public void onCheckboxRememberMeClicked(View view) {
        // Is the view now checked? // TODO: Faire un switch plutot
        boolean checked = ((CheckBox)findViewById(R.id.checkbox_rememberme)).isChecked();

        if (checked) {
            System.out.println("CHECKED");
            this.setOnPreference(PREFS_REMEMBERME, "true");
        } else{
            System.out.println("PAS CHECKED");
            this.setOnPreference(PREFS_REMEMBERME, "false");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        CheckBox temp = ((CheckBox)findViewById(R.id.checkbox_rememberme));

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPrivUrl = (AutoCompleteTextView) findViewById(R.id.urlpublic);
        mPubUrl = (AutoCompleteTextView) findViewById(R.id.url_private);
        mPasswordView = (EditText) findViewById(R.id.password);

        if(Objects.equals(this.getOnPreference(PREFS_REMEMBERME), "true")) {
            mPasswordView.setText(this.getOnPreference(PREFS_PASS));
            mEmailView.setText(this.getOnPreference(PREFS_MAIL));
            mPrivUrl.setText(this.getOnPreference(PREFS_PRIVURL));
            mPubUrl.setText(this.getOnPreference(PREFS_PUBURL));
        }

        if (Objects.equals(this.getOnPreference(PREFS_REMEMBERME),"true")){
            temp.setChecked(true);
        }
        else{
            temp.setChecked(false); this.cleanPreferences();
        }

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity.this.setOnPreference(PREFS_PASS, mPasswordView.getText().toString());
                LoginActivity.this.setOnPreference(PREFS_MAIL, mEmailView.getText().toString());
                LoginActivity.this.setOnPreference(PREFS_PUBURL, mPubUrl.getText().toString());
                LoginActivity.this.setOnPreference(PREFS_PRIVURL, mPrivUrl.getText().toString());

                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        this.registerReceiver(mLoginSuccessReceiver, new IntentFilter("LOGIN_SUCCESS"));
        this.registerReceiver(mLoginErrorReceiver, new IntentFilter("LOGIN_ERROR"));
    }




    private void attemptLogin() {
        OpenTouchClient.getInstance().login(
                mEmailView.getText().toString(),
                mPasswordView.getText().toString()
        );
    }

    private BroadcastReceiver mLoginSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startActivity(new Intent(LoginActivity.this, ThreadsActivity.class));
            OpenTouchClient.getInstance().getDefaultMailbox().fetchMessages();
            LoginActivity.this.finish();
        }
    };

    private BroadcastReceiver mLoginErrorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AlertDialog.Builder ab = new AlertDialog.Builder(LoginActivity.this);
            ab.setMessage("Erreur de connexion");
            ab.show();
        }
    };

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(mLoginSuccessReceiver);
        this.unregisterReceiver(mLoginErrorReceiver);
        super.onDestroy();
    }

    void subscribe(String url) {
        try {
            // i.e.: request = "http://example.com/index.php?param1=a&param2=b&param3=c";
            URL object = new URL(url);
            HttpURLConnection urlConnection  = (HttpURLConnection) object.openConnection();

            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestMethod("POST");

            JSONArray user_id = new JSONArray();
            user_id.put("tpvoip0"); // TODO REPLACE tpvoip0 par le login faut faire un get login .... dynamique & co

            JSONArray telephony = new JSONArray();
            telephony.put("telephony");

            JSONArray unifiedComLog = new JSONArray();
            unifiedComLog.put("unifiedComLog");

            JSONArray vide = new JSONArray();

            JSONObject id1 = new JSONObject();
            id1.put("ids",user_id);
            id1.put("names",telephony);
            id1.put("families",vide);
            id1.put("origins",vide);

            JSONObject id2 = new JSONObject();
            id2.put("ids",user_id);
            id2.put("names",unifiedComLog);
            id2.put("families",vide);
            id2.put("origins",vide);

            JSONArray id = new JSONArray();
            id.put(id1);
            id.put(id2);

            JSONObject selector = new JSONObject();
            selector.put("selector", id);

            JSONObject filter = new JSONObject();
            filter.put("filter", selector);
            filter.put("mode","CHUNK");
            filter.put("format","JSON");
            filter.put("version","1.0");
            filter.put("timeout",10);

            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(filter.toString());
            out.close();

            int HttpResult =urlConnection.getResponseCode();
            if(HttpResult == HttpURLConnection.HTTP_OK){
                System.out.println("*=*=*=*=*=*=*=* Succès ! *=*=*=*=*=*=*=*");

            }else{
                System.out.println(urlConnection.getResponseMessage());
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {

            e.printStackTrace();
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

