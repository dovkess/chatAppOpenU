package com.example.dov.chatappopenu;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * This class is the Activity that shows the chat with eatch contact.
 */
public class chatWin extends Activity {
    ListView messageList; // listView of messages with user
    ArrayList<String> listItems; // The actual messages
    ArrayAdapter<String> adapter; // The adapter between the view and the messages
    EditText messageText; // The field to enter data
    Button send; // send a message
    String phoneNumber; // contacts' phone number
    String name; // contacts' name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this).registerReceiver(mHandler,
                new IntentFilter("com.example.dov.chatappopenu_FCM_MESSAGE"));
        setContentView(R.layout.activity_chat_win);

        messageList = (ListView) findViewById(R.id.message_list);
        Intent intPhone = getIntent();
        phoneNumber = intPhone.getStringExtra("phone");
        name = intPhone.getStringExtra("name");

        listItems = new ArrayList<String>();

        chatAppDB cdb = new chatAppDB(getApplicationContext());
        SQLiteDatabase db = cdb.getReadableDatabase();

        String[] projection = {dbContractClass.dbContract.MESSAGE};
        String selection = dbContractClass.dbContract.CONTACT_NUMBER + " = '" + phoneNumber + "'";

        Cursor c = db.query(
                dbContractClass.dbContract.CHATT_DATA_TABLE_NAME,
                projection,
                selection,
                null,
                null,
                null,
                null
        );
        try{
            c.moveToFirst();
            do{
                listItems.add(c.getString(0));
            }while(c.moveToNext());
        }catch(Exception e){
        }finally {
            c.close();
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        messageList.setSelection(adapter.getCount()-1);
        messageList.setAdapter(adapter);
        messageText = (EditText) findViewById(R.id.enter_message_view);
        send = (Button) findViewById(R.id.send_button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageText.getText().toString();
                new sendMessageSync().execute(message, phoneNumber);
                addItems(message, "Me");
                messageText.setText("");
            }
        });
    }

    // This function adds a message to the DB and to the adapter.
    public void addItems(String message, String sender) {
        String fullMsg = sender + ": " + message;
        listItems.add(fullMsg);
        adapter.notifyDataSetChanged();
        messageList.setSelection(adapter.getCount()-1);

        chatAppDB db = new chatAppDB(getApplicationContext());
        SQLiteDatabase wdb = db.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(dbContractClass.dbContract.CONTACT_NAME, name);
        values.put(dbContractClass.dbContract.MESSAGE, fullMsg);
        values.put(dbContractClass.dbContract.CONTACT_NUMBER, phoneNumber);

        long newRowId = wdb.insert(dbContractClass.dbContract.CHATT_DATA_TABLE_NAME, null, values);
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
                                Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_LONG);
                            }
                        } catch (JSONException e) {}
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
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
                        return "application/json; charset=UTF-8"; //+ getParamsEncoding();
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
            String broadName = intent.getStringExtra("name");
            if(broadName.equals(name)) {
                addItems(msg, broadName);
            }
        }
    };

    @Override
    protected void onPause(){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mHandler);
    }
}
