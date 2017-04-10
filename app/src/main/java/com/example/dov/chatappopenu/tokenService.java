package com.example.dov.chatappopenu;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dov on 08/04/2017.
 */

public class tokenService extends FirebaseInstanceIdService {
    final String TAG = "myTag";
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(tokenService.this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("token", refreshedToken);
        editor.apply();
        sendRegistrationToServer(refreshedToken);
    }

    private byte[] getByteBodey(String token, String uid){
        String toByte = "{\n\t\"token\": \""+token+"\",\n\t\"id\": \""+uid+"\"\n}";
        return toByte.getBytes();
    }

    private void sendRegistrationToServer(final String tkn) {
        int state = ContextCompat.checkSelfPermission(tokenService.this, android.Manifest.permission.READ_PHONE_STATE);
        int internet = ContextCompat.checkSelfPermission(tokenService.this, Manifest.permission.INTERNET);
        if (state == PackageManager.PERMISSION_GRANTED && internet == PackageManager.PERMISSION_GRANTED){
            TelephonyManager tMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            final String mPhoneId = tMgr.getDeviceId();

            String url = "http://app9443.cloudapp.net:8080/ChatApp/webresources/SignUp/updateToken";
            //String url = "https://httpbin.org/put";
            RequestQueue queue = Volley.newRequestQueue(tokenService.this);

            StringRequest sRequest = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jResponce = new JSONObject(response);
                        Integer res_status = Integer.parseInt(jResponce.getString("status"));
                        if (res_status == 0) {}
                    } catch (JSONException e) {}
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {}
            }) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    return getByteBodey(tkn, mPhoneId);
                }
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=" + getParamsEncoding();
                }
            };
            sRequest.setShouldCache(false);
            queue.add(sRequest);
        }
    }
}
