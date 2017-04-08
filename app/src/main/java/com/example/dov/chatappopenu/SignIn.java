package com.example.dov.chatappopenu;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A login screen that offers login via email/password.
 */
public class SignIn extends Activity{// implements LoaderCallbacks<Cursor> {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPhoneView;
    private EditText mPhoneIdView;
    private EditText mCountryCode;
    private View mProgressView;
    private View mLoginFormView;
    String countryCode;
    final int PERMISSION_READ_STATE = 0;
    final int PERMISSION_INTERNET = 1;
    final String GOOD_RESPONCE = "User Signup Successful.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        String mPhoneId = "";
        mPhoneIdView = (EditText) findViewById(R.id.device_id);
        Context mAppContext = getApplicationContext();
        if (ContextCompat.checkSelfPermission(SignIn.this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED){
            TelephonyManager tMgr = (TelephonyManager) mAppContext.getSystemService(Context.TELEPHONY_SERVICE);
            mPhoneId = tMgr.getDeviceId();
            mPhoneIdView.setText(mPhoneId);
            countryCode = tMgr.getSimCountryIso();
            mCountryCode = (EditText) findViewById(R.id.country_code);
            mCountryCode.setText(ISOCountryToPrefix.getPhone(countryCode));
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();


        mPhoneView = (EditText) findViewById(R.id.phone_number);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        //getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        // Reset errors.
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String phone_number = mPhoneView.getText().toString();
        String phone_id = mPhoneIdView.getText().toString();

        boolean cancel = false;
        View focusView = null;

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
        else if(TextUtils.isEmpty(phone_number)){
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(email, phone_number, phone_id);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_longAnimTime);
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPhoneNumber;
        private final String mPhoneId;

        UserLoginTask(String email, String phone_number, String phone_id) {
            mEmail = email;
            mPhoneNumber = phone_number;
            mPhoneId = phone_id;
        }

        public byte[] getByteBodey(){
            // TODO: change this after long/int fix (replace 123 with actual mPhoneId)
            String stringBytes = "{\n\t\"name\": \"" + "The best name" + "\", \n\t\"id\": \"" + "123" +
                    "\", \n\t\"email\": \"" + mEmail + "\", \n\t\"phone\": \"" + mPhoneNumber +
                    "\"\n\t\n}";
            byte[] bytes = stringBytes.getBytes();
            return bytes;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (ContextCompat.checkSelfPermission(SignIn.this, Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED){
                RequestQueue queue = Volley.newRequestQueue(SignIn.this);
                //String url = "https://httpbin.org/put";
                String url = "http://app9443.cloudapp.net:8080/ChatApp/webresources/SignUp/registerUser";

                // Request a Json response from the provided URL.
                StringRequest jRequest = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Intent mainPageIntent = new Intent(SignIn.this, MainPage.class);
                            JSONObject jresponse = new JSONObject(response);
                            Integer res_status = Integer.parseInt(jresponse.getString("status"));
                            if (res_status == 0) {
                                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SignIn.this);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("uid", mPhoneId);
                                editor.apply();
                                startActivity(mainPageIntent);
                            }
                            else if(res_status == 1)
                                startActivity(mainPageIntent);
                        }catch(Exception e){}
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mPhoneView.setText(error.getMessage());
                    }
                }){
                @Override
                public byte[] getBody() throws AuthFailureError {
                    return getByteBodey();
                }
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=" + getParamsEncoding();
                }
            };
            jRequest.setShouldCache(false);
            queue.add(jRequest);
            }
        return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
