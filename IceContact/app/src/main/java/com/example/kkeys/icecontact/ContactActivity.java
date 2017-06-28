package com.example.kkeys.icecontact;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Created by Ciarán Keyes on 28/06/217.
 */
public class ContactActivity extends AppCompatActivity {

    NotificationCompat.Builder notification;
    private static final int uniqueID = 12345;
    EditText number;
    Button btn;

    private ListView mListView;
    String phoneNumber = null;
    StringBuffer outputName;
    StringBuffer outputNumber;
    TextView textView;
    private ProgressDialog pDialog;
    private Handler updateBarHandler;
    ArrayList<String> contactListName;
    ArrayList<String> contactListNumber;
    Cursor cursor;
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        number = (EditText) findViewById(R.id.editText2);
        number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number.setFocusable(true);
                number.setFocusableInTouchMode(true);
            }
        });

        btn = (Button) findViewById(R.id.emButton);
        notification = new NotificationCompat.Builder(this);
        // dont want the notification to be canceld automaticly.
        notification.setAutoCancel(false);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Reading contacts...");
        pDialog.setCancelable(false);
        pDialog.show();
        mListView = (ListView) findViewById(R.id.listview);
        //number = (EditText) findViewById(R.id.editText2);
        updateBarHandler =new Handler();
        // Since reading contacts takes more time, let's run it on a separate thread.

        new Thread(new Runnable() {
            @Override
            public void run() {
                getContacts();
            }
        }).start();
        btn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                // toast to see if the entered number has been allocated to the notification
                Toast.makeText(ContactActivity.this, "Emergency number : " + number.getText().toString() + ", has been selected", Toast.LENGTH_SHORT).show();

                // setting the notification settings, icon, title etc...
                notification.setSmallIcon(R.drawable.info);
                notification.setTicker("A.M.E");
                notification.setWhen(System.currentTimeMillis());
                notification.setContentTitle("Emergency Number");
                notification.setAutoCancel(false);
                notification.setOngoing(true);
                notification.setContentText(": " + number.getText().toString());
                // intent to do something when clicked in this case make a call
                //Intent call = new Intent(this, MainActivity.class);
                Intent call = new Intent(Intent.ACTION_CALL);
                // when clicked call the number in the notification
                call.setData(Uri.parse("tel:" + number.getText()));
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, call, PendingIntent.FLAG_UPDATE_CURRENT);
                notification.setContentIntent(pendingIntent);

                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(uniqueID, notification.build());
            }
        });

    }
    public void getContacts() {
        contactListName = new ArrayList<String>();
        contactListNumber = new ArrayList<String>();
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        ContentResolver contentResolver = getContentResolver();
        //// make the order alphabetical ascending ///////////////
        cursor = contentResolver.query(CONTENT_URI, null,null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {
            counter = 0;
            while (cursor.moveToNext()) {
                outputName = new StringBuffer();
                outputNumber = new StringBuffer();
                // Update the progress message
                updateBarHandler.post(new Runnable() {
                    public void run() {
                        pDialog.setMessage("Reading contacts : "+ counter++ +"/"+cursor.getCount());
                    }
                });
                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER  )));
                if (hasPhoneNumber > 0) {
                    outputName.append(name);
                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        outputNumber.append(phoneNumber);

                        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {
                                //TODO Do whatever you want with the list data
                                number.setText(contactListNumber.get(position).toString());
                            }
                        });
                    }
                    phoneCursor.close();
                }
                // Add the contact to the ArrayList
                contactListName.add(outputName.toString() + " " + outputNumber.toString());
                contactListNumber.add(outputNumber.toString());

            }
            // ListView has to be updated using a ui thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, contactListName);
                    mListView.setAdapter(adapter);

                }
            });
            // Dismiss the progressbar after 500 millisecondds
            updateBarHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pDialog.cancel();
                }
            }, 500);
        }
    }
    private static void sortList(List<String> aItems){
        Collections.sort(aItems, String.CASE_INSENSITIVE_ORDER);
    }
}