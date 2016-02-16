package com.alcatel.mobilevoicemail;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.alcatel.mobilevoicemail.opentouch.ExceptionHandler;
import com.alcatel.mobilevoicemail.opentouch.OpenTouchClient;

import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ActionBarActivity implements LoaderCallbacks<Cursor> {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

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
    private void clean_preferences(){
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


        System.out.println("->mdp" + this.getOnPreference(PREFS_PASS));
        System.out.println("->rememberme" + this.getOnPreference(PREFS_REMEMBERME));
        System.out.println("->puburl " + this.getOnPreference(PREFS_PUBURL));
        System.out.println("->privurl " + this.getOnPreference(PREFS_PRIVURL));
        CheckBox temp = ((CheckBox)findViewById(R.id.checkbox_rememberme));

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPrivUrl = (AutoCompleteTextView) findViewById(R.id.urlpublic);
        mPubUrl = (AutoCompleteTextView) findViewById(R.id.url_private);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);

        if(Objects.equals(this.getOnPreference(PREFS_REMEMBERME), "true")) {
            mPasswordView.setText(this.getOnPreference(PREFS_PASS));
            System.out.println("hey pref " + this.getOnPreference(PREFS_MAIL));
            mEmailView.setText(this.getOnPreference(PREFS_MAIL));
            mPrivUrl.setText(this.getOnPreference(PREFS_PRIVURL));
            mPubUrl.setText(this.getOnPreference(PREFS_PUBURL));
        }

        if (Objects.equals(this.getOnPreference(PREFS_REMEMBERME),"true")){
            temp.setChecked(true); System.out.println("true pref");}
        else{ //TODO jenleve mdp si on decoche dans la memoire..? mais bizarre jenleve au lancement
            temp.setChecked(false); this.clean_preferences(); System.out.println("ici passe");}


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
                //attemptLogin();

                LoginActivity.this.setOnPreference(PREFS_PASS, mPasswordView.getText().toString());
                LoginActivity.this.setOnPreference(PREFS_MAIL, mEmailView.getText().toString());
                LoginActivity.this.setOnPreference(PREFS_PUBURL, mPubUrl.getText().toString());
                LoginActivity.this.setOnPreference(PREFS_PRIVURL, mPrivUrl.getText().toString());
                //OpenTouchAuthentication auth = new OpenTouchAuthentication();

                //auth.connectOpenTouch(mEmailView.getText().toString(),mPasswordView.getText().toString());
                OpenTouchClient.getInstance().login(
                        mEmailView.getText().toString(),
                        mPasswordView.getText().toString(),
                        new ExceptionHandler() {
                            @Override
                            public void handle(Exception e) {
                                // AlertBox
                                e.printStackTrace();
                            }
                        }
                );

                startActivity(new Intent(LoginActivity.this, ThreadsActivity.class));
                LoginActivity.this.finish();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            try {
                mAuthTask = new UserLoginTask(email,password, new URL ("tps-opentouch.u-strasbg.fr"), new URL ("tps-opentouch.u-strasbg.fr"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
        OpentouchClient.initialize();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final URL mPrivateUrl;
        private final URL mPublicUrl;


        UserLoginTask(String email, String password, URL privateUrl, URL publicUrl) {
            mEmail = email;
            mPassword = password;
            mPrivateUrl = privateUrl;
            mPublicUrl = publicUrl;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                HttpURLConnection connection = (HttpURLConnection) mPrivateUrl.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

