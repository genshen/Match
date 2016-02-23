package com.holo.match;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.holo.web.HttpIntentService;
import com.holo.web.tools.AndroidAPI;

public class WebActivity extends AppCompatActivity {
    Intent httpIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AndroidAPI.initContext(this);
        httpIntent = new Intent(this, HttpIntentService.class);
        startService(httpIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(httpIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

}
