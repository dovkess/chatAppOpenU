package com.example.dov.chatappopenu;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static android.telephony.PhoneNumberUtils.extractNetworkPortion;
import static android.telephony.PhoneNumberUtils.isGlobalPhoneNumber;
import static android.telephony.PhoneNumberUtils.normalizeNumber;
import static android.telephony.PhoneNumberUtils.stripSeparators;

public class MainPage extends AppCompatActivity {

    final int PERMISSION_READ_CONTACTS = 1;
    private TextView mTextMessage;
    private EditText mSearchContact;
    private ListView mListView;
    BottomNavigationView navigation;
    ArrayList<String> contacts;
    ArrayList<String> knownContacts = new ArrayList<String>();
    Cursor c;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.all_contacts_item:
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, R.id.text1, contacts);
                    mListView.setAdapter(arrayAdapter);
                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String pickedName = contacts.get(position);
                            getPhoneFromeName(pickedName);
                        }
                    });
                    return true;
                case R.id.known_contacts_item:
                    ArrayAdapter<String> arrayAdapterKnown = new ArrayAdapter<String>(
                            getApplicationContext(), R.layout.list_item, R.id.text1, knownContacts);
                    mListView.setAdapter(arrayAdapterKnown);
                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String pickedName = knownContacts.get(position);
                            getPhoneFromeName(pickedName);
                        }
                    });
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        int read, state, internet;
        internet = ContextCompat.checkSelfPermission(MainPage.this, Manifest.permission.INTERNET);
        read = ContextCompat.checkSelfPermission(MainPage.this, Manifest.permission.READ_CONTACTS);
        state = ContextCompat.checkSelfPermission(MainPage.this, Manifest.permission.READ_PHONE_STATE);

        if (read != PackageManager.PERMISSION_GRANTED
                || state != PackageManager.PERMISSION_GRANTED
                || internet != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainPage.this, new String[]{
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.INTERNET},
                    PERMISSION_READ_CONTACTS);
         }
        else {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String uid = prefs.getString("uid", "-1");
            if (uid.equals("-1")) {
                Intent signIn = new Intent(this, SignIn.class);
                startActivity(signIn);
            }
            mSearchContact = (EditText) findViewById(R.id.search_contact_field);
            contacts = new ArrayList<String>();
            mListView = (ListView) findViewById(R.id.list_view);
            navigation = (BottomNavigationView) findViewById(R.id.navigation);

            fillContacts("");
            new fillknown().execute();
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
            navigation.setSelectedItemId(R.id.all_contacts_item);
            mSearchContact.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String lookup = editable.toString();
                    fillContacts(lookup);
                }
            });
        }
    }

    public void fillContacts(String lookup) {
        contacts = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(MainPage.this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            Uri contentURI = ContactsContract.Contacts.CONTENT_URI;
            String _ID = ContactsContract.Contacts._ID;
            String displayName = ContactsContract.Contacts.DISPLAY_NAME;
            String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = '1'";
            c = getContentResolver().query(contentURI, null, selection, null, null);
            c.moveToFirst();
            do {
                String name = c.getString(c.getColumnIndex(displayName));
                if (lookup == "")
                    contacts.add(name);
                else {
                    String lookupLower = lookup.toLowerCase();
                    String lower = name.toLowerCase();
                    if (lower.contains(lookupLower)) {
                        contacts.add(name);
                    }
                }
            }while (c.moveToNext());
            Collections.sort(contacts);
        }
        navigation.setSelectedItemId(navigation.getSelectedItemId());
    }

    public void getPhoneFromeName(String name) {
        String number;
        ArrayList<String> contactNumbers = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(MainPage.this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            String ret = null;
            String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = '" + name + "'";
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, selection, null, null);

            c.moveToFirst();
            do {
                contactNumbers.add(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            }
            while (c.moveToNext());
            c.close();
            // TODO: prompt the user for one of the numbers
            if (!contactNumbers.isEmpty()) {
                number = contactNumbers.get(0);
                number = stripSeparators(number);
                if(!number.startsWith("+")){
                    number = "+972" + number;
                } else if(number.startsWith("+9725")){
                    number = number.replace("+9725", "+97205");
                }
                Toast.makeText(getApplicationContext(), "item clicked : \n" + number, Toast.LENGTH_LONG).show();
                new isNumberRegistered().execute(number, name);
            }
        }
    }

    private class isNumberRegistered extends AsyncTask<String, Void, Boolean> {
        private byte[] getByteBodey(String number){
            String str_res = "{\n\t\"phone\": \"" + number + "\"\n}";
            return str_res.getBytes();
        }

        protected Boolean doInBackground(final String... params) {
            String url = "http://app9443.cloudapp.net:8080/ChatApp/webresources/messaging/validateAppUser";

            if (ContextCompat.checkSelfPermission(MainPage.this, Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED) {
                RequestQueue queue = Volley.newRequestQueue(MainPage.this);
                StringRequest jRequest = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jresponse = new JSONObject(response);
                            Integer res_status = Integer.parseInt(jresponse.getString("status"));
                            if (res_status == 0) {
                                Intent startChat = new Intent(MainPage.this, chatWin.class);
                                startChat.putExtra("phone", params[0]);
                                startChat.putExtra("name", params[1]);
                                startActivity(startChat);
                            }
                            else if(res_status == -1){
                                Toast.makeText(getApplicationContext(), "He/She does not have the app", Toast.LENGTH_LONG);
                            }
                        } catch (JSONException e) {
                            e.getStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.getStackTrace();
                    }
                }) {
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        return getByteBodey(params[0]);
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
        protected void onPostExecute(Boolean result) {}
    }

    public class fillknown extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... strings) {
            chatAppDB cdb = new chatAppDB(getApplicationContext());
            SQLiteDatabase db = cdb.getReadableDatabase();

            String[] projection = {
                    dbContractClass.dbContract.CONTACT_NAME
            };
            String selection = " * ";
            String groupBy = dbContractClass.dbContract.CONTACT_NAME;
            Cursor c = db.query(
                    dbContractClass.dbContract.CHATT_DATA_TABLE_NAME, projection, null, null, groupBy, null, null);
            try{
                c.moveToFirst();
                do{
                    knownContacts.add(c.getString(0));
                }while(c.moveToNext());
            }catch (Exception e){
            }finally {
                c.close();
            }

            return null;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == PERMISSION_READ_CONTACTS && grantResults.length > 0){
            Toast.makeText(getApplicationContext(), "GOT_IT", Toast.LENGTH_LONG);
            startActivity(new Intent(MainPage.this, MainPage.class));
        }
        else{
            Toast.makeText(getApplicationContext(), "You stupid ass hole. now you can't run the app.", Toast.LENGTH_LONG);
        }
        return;
    }
}
