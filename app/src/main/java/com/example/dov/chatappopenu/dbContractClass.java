package com.example.dov.chatappopenu;

import android.provider.BaseColumns;

/**
 * Created by dov on 04/04/2017.
 */

public class dbContractClass {
    private dbContractClass(){}

    public static class dbContract implements BaseColumns{
        public static final String KNOWN_CNTCT_TABLE_NAME = "known_contacts";
        public static final String CHATT_DATA_TABLE_NAME = "chat_data";
        public static final String CONTACT_NAME = "name";
        public static final String CONTACT_NUMBER = "number";
        public static final String MESSAGE = "message";

        public static final String CREATE_CONTACTS =
                "CREATE TABLE " + KNOWN_CNTCT_TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        CONTACT_NAME + " TEXT," +
                        CONTACT_NUMBER + " TEXT)";
        public static final String CREATE_CHAT_DATA =
                "CREATE TABLE " + CHATT_DATA_TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        CONTACT_NAME + " TEXT," +
                        MESSAGE + " TEXT)";
    }
}
