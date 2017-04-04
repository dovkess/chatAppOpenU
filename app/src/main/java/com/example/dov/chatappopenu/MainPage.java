package com.example.dov.chatappopenu;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MainPage extends AppCompatActivity {

    final int PERMISSION_READ_CONTACTS = 1;
    private TextView mTextMessage;
    private EditText mSearchContact;
    private ListView mListView;
    BottomNavigationView navigation;
    ArrayList<String> contacts;
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
                            Toast.makeText(getApplicationContext(), "item clicked : \n" + pickedName, Toast.LENGTH_SHORT).show();
                            getPhoneFromeName(pickedName);
                        }
                    });
                    return true;
                case R.id.known_contacts_item:
                    //mTextMessage.setText(R.string.title_dashboard);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        mSearchContact = (EditText) findViewById(R.id.search_contact_field);
        contacts = new ArrayList<String>();
        mListView = (ListView) findViewById(R.id.list_view);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);

        fillContacts("");
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.all_contacts_item);
        mSearchContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String lookup = editable.toString();
                fillContacts(lookup);
            }
        });
    }

    public void fillContacts(String lookup){
        contacts = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(MainPage.this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainPage.this, new String[]{
                    Manifest.permission.READ_CONTACTS}, PERMISSION_READ_CONTACTS);
        }
        else {
            Uri contentURI = ContactsContract.Contacts.CONTENT_URI;
            String _ID = ContactsContract.Contacts._ID;
            String displayName = ContactsContract.Contacts.DISPLAY_NAME;
            ContentResolver cr = getContentResolver();
            c = cr.query(contentURI, null, null, null, null);
            c.moveToFirst();
            while (c.moveToNext()) {
                String name = c.getString(c.getColumnIndex(displayName));
                if(lookup == "")
                    contacts.add(name);
                else{
                    String lookupLower = lookup.toLowerCase();
                    String lower = name.toLowerCase();
                    if(lower.contains(lookupLower)){
                        contacts.add(name);
                    }
                }
            }
            Collections.sort(contacts);
        }
        navigation.setSelectedItemId(navigation.getSelectedItemId());
    }

    public void getPhoneFromeName(String name){
        if (ContextCompat.checkSelfPermission(MainPage.this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED){
            String ret = null;
            String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + name +"%'";
            String[] projection = new String[] {ContactsContract.Data._ID, ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor c = getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, selection, null, null);
            c.moveToFirst();
            if (c.moveToNext()) {
                ret = c.getString(0);
            }
            c.close();
        }
    }
}
