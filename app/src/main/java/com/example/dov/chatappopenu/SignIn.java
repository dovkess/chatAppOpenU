package com.example.dov.chatappopenu;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import static android.telephony.PhoneNumberUtils.stripSeparators;


/**
 * A login screen that offers login via email/password.
 */
public class SignIn extends Activity{
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPhoneView;
    private EditText mUserView;
    private EditText mCountryCode;
    private Spinner mCarrier;
    private View mProgressView;
    private View mLoginFormView;
    String countryCode;
    String mPhoneId;
    final int PERMISSION_READ_STATE = 0;
    final int PERMISSION_INTERNET = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mUserView = (EditText) findViewById(R.id.user_name_view);
        Context mAppContext = getApplicationContext();
        if (ContextCompat.checkSelfPermission(SignIn.this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED){
            TelephonyManager tMgr = (TelephonyManager) mAppContext.getSystemService(Context.TELEPHONY_SERVICE);
            mPhoneId = tMgr.getDeviceId();
            countryCode = tMgr.getSimCountryIso();
            mCountryCode = (EditText) findViewById(R.id.country_code);
            String country_code_num = ISOCountryToPrefix.getPhone(countryCode);
            mCountryCode.setText(country_code_num);
            mCarrier = (Spinner) findViewById(R.id.carrier);
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, (CountryToProvider.getProvider(country_code_num)));
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mCarrier.setAdapter(spinnerArrayAdapter);
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
        String local_number = mPhoneView.getText().toString();
        String country_code = mCountryCode.getText().toString();
        String carrier = mCarrier.getSelectedItem().toString();
        String phone_number = country_code + carrier + stripSeparators(local_number);
        String user_name = mUserView.getText().toString();

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
        if(TextUtils.isEmpty(phone_number)){
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        }
        if(TextUtils.isEmpty(user_name))
            user_name = "looser";

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(email, phone_number, mPhoneId, user_name);
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
        private final String mUser;

        UserLoginTask(String email, String phone_number, String phone_id, String user) {
            mEmail = email;
            mPhoneNumber = phone_number;
            mPhoneId = phone_id;
            mUser = user;
        }

        public byte[] getByteBody(){
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SignIn.this);
            String token = prefs.getString("token", "-1");
            String stringBytes = "{\n\t\"token\": \"" + token + "\",\n\t\"name\": \"" + mUser + "\", \n\t\"id\": \"" + mPhoneId +
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
                String url = "http://app9443.cloudapp.net:8080/ChatApp/webresources/SignUp/registerUser";

                // Request a Json response from the provided URL.
                StringRequest jRequest = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Intent mainPageIntent = new Intent(SignIn.this, MainPage.class);
                            JSONObject jresponse = new JSONObject(response);
                            Integer res_status = Integer.parseInt(jresponse.getString("status"));
                            if (res_status == 0 || res_status == -1) { // 0 -- user created correctly. -1 -- user exists
                                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SignIn.this);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("uid", mPhoneId);
                                editor.putString("phone", mPhoneNumber);
                                editor.apply();
                                startActivity(mainPageIntent);
                            }
                        }catch(Exception e){}
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("failed to connect", error.toString());
                    }
                }){
                @Override
                public byte[] getBody() throws AuthFailureError {
                    return getByteBody();
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
