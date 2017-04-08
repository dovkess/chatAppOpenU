package com.example.dov.chatappopenu;

import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class chatWin extends ListActivity {
    //ListView messageList;
    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;
    EditText messageText;
    Button send;
    String phoneNumber;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_win);

        Intent intPhone = getIntent();
        final String phoneNumber = intPhone.getStringExtra("phone");
        String name = intPhone.getStringExtra("name");

        // TODO: load old messages from DB
        listItems = new ArrayList<String>();

        // TODO: create our own list item view
        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        setListAdapter(adapter);

        messageText = (EditText) findViewById(R.id.enter_message_view);
        send = (Button) findViewById(R.id.send_button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageText.toString();
                new sendMessageSync().execute(message, phoneNumber);
            }
        });
    }

    public void addItems(String message) {
        listItems.add("Me: " + message);
        adapter.notifyDataSetChanged();
    }

    private class sendMessageSync extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(final String... params) {
            String url = "http://???";
            JSONObject jobj = new JSONObject();
            try {
                jobj.put("message", params[0]);
                jobj.put("recepiant", params[1]);
            } catch (Exception e) {}

            if (ContextCompat.checkSelfPermission(chatWin.this, Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED) {

                RequestQueue queue = Volley.newRequestQueue(chatWin.this);

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
            return true;
        }
        protected void onPostExecute(Boolean result) {}
    }
}
