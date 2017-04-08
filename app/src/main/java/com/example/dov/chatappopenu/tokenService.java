package com.example.dov.chatappopenu;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        int state = ContextCompat.checkSelfPermission(tokenService.this, android.Manifest.permission.READ_PHONE_STATE);
        int internet = ContextCompat.checkSelfPermission(tokenService.this, Manifest.permission.INTERNET);
        if (state == PackageManager.PERMISSION_GRANTED && internet == PackageManager.PERMISSION_GRANTED){
            TelephonyManager tMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            String mPhoneId = tMgr.getDeviceId();

            String url = "???";

            JSONObject jobj = new JSONObject();
            try {
                jobj.put("pid", mPhoneId);
                jobj.put("token", refreshedToken);
            }
            catch (Exception e){}

            RequestQueue queue = Volley.newRequestQueue(tokenService.this);

            JsonObjectRequest jRequest = new JsonObjectRequest(Request.Method.PUT, url, jobj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Integer res_status = Integer.parseInt(response.getString("status"));
                        if (res_status == 0) {}
                    } catch (JSONException e) {}
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {}
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    return params;
                }
            };
            jRequest.setShouldCache(false);
            queue.add(jRequest);
        }
    }
}
