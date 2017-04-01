package com.example.dov.chatappopenu;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainPage extends ActionBarActivity {
    final int PICK_CONTACT = 1;
    final int PERMISSION_READ_CONTACTS = 1;
    String phone_number = "";
    private TextView mPhoneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        mPhoneView = (TextView) findViewById(R.id.contact_phone_view);
        Intent chooseContactInt = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(chooseContactInt, PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        String contactNumber = "No number";

        if (reqCode == PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();
            Cursor c = getContentResolver().query(contactData, null, null, null, null);

            if (c.moveToFirst()) {
                String cID = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                if (ContextCompat.checkSelfPermission(MainPage.this, Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainPage.this, new String[]{
                                    Manifest.permission.READ_CONTACTS},
                            PERMISSION_READ_CONTACTS);
                }
                else {
                    Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                                    ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                            new String[]{cID},
                            null);

                    if (cursorPhone.moveToFirst()) {
                        contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    //phone_number = contactNumber;
                    cursorPhone.close();
                    mPhoneView.setText(contactNumber);
                    Toast.makeText(getApplicationContext(), contactNumber, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}