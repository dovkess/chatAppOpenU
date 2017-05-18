package com.example.dov.chatappopenu;

import android.*;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneNumberUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;

public class MessageReceived extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String message = data.get("msg");
            String from = data.get("From");
            String name = "NoName";

            chatAppDB cdb = new chatAppDB(getApplicationContext());
            SQLiteDatabase db = cdb.getReadableDatabase();
            String[] projection = {dbContractClass.dbContract.CONTACT_NAME};
            String selection = dbContractClass.dbContract.CONTACT_NUMBER + " = '" + from + "'";
            Cursor c = db.query(dbContractClass.dbContract.KNOWN_CNTCT_TABLE_NAME, projection, selection,
                    null, null, null, null);
            try {
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    name = c.getString(0);
                    c.close();
                }else{
                    name = findNameByNumber(from);
                }
            }catch (Exception e){
            }finally {
                c.close();
            }

            Intent broadInt = new Intent ("com.example.dov.chatappopenu_FCM_MESSAGE");
            broadInt.putExtra("msg", message);
            broadInt.putExtra("name", name);
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            lbm.sendBroadcast(broadInt);

            // add message to DB
            String fullMsg = name + ": " + message;
            SQLiteDatabase wdb = cdb.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(dbContractClass.dbContract.CONTACT_NAME, name);
            values.put(dbContractClass.dbContract.MESSAGE, fullMsg);
            values.put(dbContractClass.dbContract.CONTACT_NUMBER, from);
            long newRowId = wdb.insert(dbContractClass.dbContract.CHATT_DATA_TABLE_NAME, null, values);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setSmallIcon(R.drawable.ic_home_black_24dp);
            mBuilder.setContentTitle("new message from: " + name);
            mBuilder.setContentText("Message content: " + message);
            mBuilder.setAutoCancel(true);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmSound);

            Intent resultIntent = new Intent(this, chatWin.class);
            resultIntent.putExtra("phone", from);
            resultIntent.putExtra("name", name);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(1, mBuilder.build());
        }
    }

    public String findNameByNumber(String number){
        String ret_val = null;
        number = PhoneNumberUtils.normalizeNumber(number);

        if (ContextCompat.checkSelfPermission(MessageReceived.this, android.Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, null, null, null);
            c.moveToFirst();
            do {
                String phone1Hash = PhoneNumberUtils.toCallerIDMinMatch(number);
                String phone2Hash = PhoneNumberUtils.toCallerIDMinMatch(c.getString(1));
                if(phone1Hash.equals(phone2Hash)){
                    ret_val = c.getString(0);
                    return ret_val;
                }
            } while (c.moveToNext());
            c.close();
        }
        return ret_val;
    }
}
