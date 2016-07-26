package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by enroiv on 7/12/16.
 *
 * A fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart(){
        super.onStart();
        getDailyForecast();
    }

    private void getDailyForecast(){

        FetchWeatherTask fwt = new FetchWeatherTask();
        String defaultUnits = getString(R.string.pref_units_default);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String zipCode = prefs.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        String prefUnits = prefs.getString(getString(R.string.pref_units_key),defaultUnits);

        Boolean isMetric = Boolean.valueOf(prefUnits.contentEquals(defaultUnits));

        fwt.execute(zipCode,defaultUnits,isMetric.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        final Context ctx = getActivity();

        mForecastAdapter = new ArrayAdapter<String>(ctx,
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);


        // Respond to user selecting an item in the list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String itStr = mForecastAdapter.getItem(i);

                // Create a Toast to display selected item
                Toast alvarezTostadoPacheco = Toast.makeText(ctx,itStr,Toast.LENGTH_SHORT);
                alvarezTostadoPacheco.show();

                // Create an explicit intent to open details screen
                Intent intent = new Intent(ctx,DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT,itStr);

                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.action_refresh) {
            getDailyForecast();
            return true;
        }
        else if(id == R.id.action_show_location){

            // Get current location
            Context ctx = getActivity();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            String zipCode = prefs.getString(getString(R.string.pref_location_key),
                    getString(R.string.pref_location_default));

            // Create Uri
            Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                    .appendQueryParameter("q",zipCode).build();


            // Create an implicit Intent to show address on map
            Intent mapIntent = new Intent();
            mapIntent.setAction(Intent.ACTION_VIEW);
            mapIntent.setData(geoLocation);

            if(mapIntent.resolveActivity(ctx.getPackageManager()) != null){
                startActivity(mapIntent);
            }
            else{
                // Show a toast with the current location
                Toast alvarezTostadoPacheco = Toast.makeText(ctx,zipCode,Toast.LENGTH_SHORT);
                alvarezTostadoPacheco.show();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class FetchWeatherTask extends AsyncTask<String,Integer,String[]>{

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            // Will contain the array of weather forecasts
            String [] forecasts = null;

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.openweathermap.org")
                    .appendPath("data/2.5/forecast/daily")
                    .appendQueryParameter("q",params[0])
                    .appendQueryParameter("units",params[1])
                    .appendQueryParameter("cnt","7")
                    .appendQueryParameter("appid",MainActivity.APPID);

            try {
                URL url = new URL(builder.build().toString());
                Log.v(LOG_TAG,"API Endpoint URL: "+url.toString());

                // These two need to be declared outside the try/catch
                // so that they can be closed in the finally block.
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                try {

                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }

                    forecastJsonStr = buffer.toString();
                    Log.v(LOG_TAG,"Forecast JSON String: "+forecastJsonStr);

                    forecasts = SunshineUtils.getWeatherDataFromJson(forecastJsonStr,7,new Boolean(params[2]));

                    for(String f : forecasts){
                        Log.v(LOG_TAG,f);
                    }

                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attemping
                    // to parse it.
                    return null;
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attemping
                    // to parse it.
                    return null;
                } finally{
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }
                    }
                }

            } catch (MalformedURLException e) {
                Log.e(LOG_TAG,"Wrong API endpoint",e);
            }

            return forecasts;
        }

        @Override
        protected void onPostExecute(String[] forecasts){

            mForecastAdapter.clear();

            for(String s:forecasts){
                mForecastAdapter.add(s);
            }
        }

    }
}