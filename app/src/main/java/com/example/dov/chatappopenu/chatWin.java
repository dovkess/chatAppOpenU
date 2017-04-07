package com.example.dov.chatappopenu;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

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
        String phoneNumber = intPhone.getStringExtra("phone");
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

            }
        });
    }

    public void addItems(String message) {
        listItems.add("Me: " + message);
        adapter.notifyDataSetChanged();
    }


}
