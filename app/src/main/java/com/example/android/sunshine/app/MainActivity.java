package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {
    public final static String APPID="1fb7c1473c5d0c572e0cc1c459b2dc4c";
    public final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(LOG_TAG,"onCreate");

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if(id == R.id.action_settings){
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.v(LOG_TAG,"onStart");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.v(LOG_TAG,"onPause");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(LOG_TAG,"onResume");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.v(LOG_TAG,"onStop");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v(LOG_TAG,"onDestroy");
    }
}
