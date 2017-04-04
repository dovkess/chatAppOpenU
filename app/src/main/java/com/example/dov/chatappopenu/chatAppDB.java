package com.example.dov.chatappopenu;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.dov.chatappopenu.dbContractClass.dbContract;

/**
 * Created by dov on 04/04/2017.
 */

public class chatAppDB extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "chatApp.db";


    public chatAppDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(dbContract.CREATE_CONTACTS);
        db.execSQL(dbContract.CREATE_CHAT_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}
}
