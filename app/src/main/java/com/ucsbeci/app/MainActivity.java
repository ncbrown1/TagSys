package com.ucsbeci.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    private String mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = getIntent().getExtras().getString("Username");
        setContentView(R.layout.activity_main);

        Button b1 = (Button) findViewById(R.id.checkbtn);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CheckActivity.class);
                intent.putExtra("Username", mUser);
                startActivity(intent);
            }
        });

        Button b2 = (Button) findViewById(R.id.tagbtn);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TagActivity.class);
                startActivity(intent);
            }
        });

        Button b3 = (Button) findViewById(R.id.devicebtn);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DeviceActivity.class);
                startActivity(intent);
            }
        });

        Button b4 = (Button) findViewById(R.id.locbtn);
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LocActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_about);
            // dialog.getWindow().setBackgroundDrawableResource(R.id.color);
            dialog.setTitle("About the App");
            dialog.show();
            return true;
        } else if (id == R.id.action_howto) {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_howto);
            dialog.setTitle("Application Work Flow");
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

}
