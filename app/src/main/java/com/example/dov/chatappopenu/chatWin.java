package com.example.dov.chatappopenu;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class chatWin extends Activity {
    ListView messageList;
    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;
    EditText messageText;
    Button send;
    String phoneNumber;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(this).registerReceiver(mHandler, new IntentFilter("com.example.dov.chatappopenu_FCM_MESSAGE"));

        setContentView(R.layout.activity_chat_win);

        messageList = (ListView) findViewById(R.id.message_list);
        Intent intPhone = getIntent();
        final String phoneNumber = intPhone.getStringExtra("phone");
        String name = intPhone.getStringExtra("name");

        // TODO: load old messages from DB
        listItems = new ArrayList<String>();

        // TODO: create our own list item view
        //adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_chat_win, android.R.layout.simple_list_item_1, listItems);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        messageList.setAdapter(adapter);
        messageText = (EditText) findViewById(R.id.enter_message_view);
        send = (Button) findViewById(R.id.send_button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageText.getText().toString();
                new sendMessageSync().execute(message, phoneNumber);
                addItems(message, "Me: ");
            }
        });
    }

    public void addItems(String message, String sender) {
        listItems.add(sender + message);
        //setListAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private class sendMessageSync extends AsyncTask<String, Void, Boolean> {
        private byte[] getByteBodey(String message, String phone, String myPhone){
            String toByte = "{\n\t\"from\": \"" + myPhone + "\",\n\t\"to\": \"" + phone + "\",\n\t\"msg\": \"" + message + "\"\n}";
            return toByte.getBytes();
        }
        protected Boolean doInBackground(final String... params) {
            String url = "http://app9443.cloudapp.net:8080/ChatApp/webresources/messaging/sendMessage";

            if (ContextCompat.checkSelfPermission(chatWin.this, Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED) {

                RequestQueue queue = Volley.newRequestQueue(chatWin.this);

                StringRequest sRequest = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jobj = new JSONObject(response);
                            Integer res_status = Integer.parseInt(jobj.getString("status"));
                            if (res_status == 0) {
                                Toast.makeText(getApplicationContext(), "Message recived", Toast.LENGTH_LONG);
                            }
                        } catch (JSONException e) {}
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        String myPhone = prefs.getString("phone", "-1");
                        return getByteBodey(params[0], params[1], myPhone);
                    }
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=" + getParamsEncoding();
                    }

                };
                sRequest.setShouldCache(false);
                queue.add(sRequest);
            }
            return true;
        }
        protected void onPostExecute(Boolean result) {}
    }

    private BroadcastReceiver mHandler = new BroadcastReceiver(){
        @Override
        public void onReceive(Context contenxt, Intent intent){
            String msg = intent.getStringExtra("msg");
            addItems(msg, name);
        }
    };

    @Override
    protected void onPause(){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mHandler);
    }
}
