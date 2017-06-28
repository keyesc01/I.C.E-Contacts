package com.example.kkeys.icecontact;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
/**
 * Created by Ciarán Keyes on 28/06/217.
 */
public class MainActivity extends AppCompatActivity {

    ImageButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initital_activity);

        button = (ImageButton)findViewById(R.id.button);
    }

    public void onButtonClick(View view){
        if(view.getId() == R.id.button){
            Intent i = new Intent(this, ContactActivity.class);
            startActivity(i);
        }
    }
}
