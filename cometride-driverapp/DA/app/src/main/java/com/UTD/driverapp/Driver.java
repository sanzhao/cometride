package com.UTD.driverapp;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class Driver extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    int num = 0;
    int total;
    ImageButton plus1, minus1;
    GPSTracker gps;
    TextView textView7,textView8,textView10;
    Button btnPost;
    Cab cab;
    double lat;
    double lng;
    Switch switch1;
    String sessionId;
    int capacity;
    HttpContext context = new BasicHttpContext();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        Intent intent = getIntent();
        sessionId = intent.getExtras().getString("sessionid");
        capacity = intent.getExtras().getInt("capacity");
        Log.d("session",sessionId);
        Log.d("capacity","current"+capacity);
        CookieStore cookieStore = new BasicCookieStore();
        context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        String name = intent.getExtras().getString("cookie-name");
        String value=intent.getExtras().getString("cookie-value");
        String domain=intent.getExtras().getString("cookie-domain");
        BasicClientCookie cookie = new BasicClientCookie( name, value );
        cookie.setDomain(domain);
        cookieStore.addCookie( cookie );
        Log.d("name",name);
        Log.d("value",value);
        Log.v( "cookie-name", cookieStore.getCookies().get( 0 ).getName());
        Log.v("cookie-value", cookieStore.getCookies().get( 0 ).getValue());
        plus1 = (ImageButton) findViewById(R.id.plus);
        minus1 = (ImageButton) findViewById(R.id.minus);
        textView10 = (TextView) findViewById(R.id.textView10);
        textView10.setText(Integer.toString(capacity));
        textView7= (TextView) findViewById(R.id.textView7);
        textView8= (TextView) findViewById(R.id.textView8);



        switch1 = (Switch) findViewById(R.id.switch1);
        if (switch1 != null) {
            switch1.setOnCheckedChangeListener(Driver.this);
        }


        plus1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (v == plus1) {
                    if(num<capacity)
                    {
                        num++;
                        total++;
                        textView7.setText(Integer.toString(total));
                        textView8.setText(Integer.toString(num));
                    }
                }
            }
        });
        minus1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (v == minus1) {
                    if(num>0)
                    {
                        num--;

                        textView8.setText(Integer.toString(num));
                    }
                }
            }
        });
        plus1.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View v) {

                total=total+(capacity-num);
                num=capacity;
                textView7.setText(Integer.toString(total));
                textView8.setText(Integer.toString(num));
                return true;
            }
        });
        minus1.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View v) {

                num=0;
                textView8.setText(Integer.toString(num));
                return true;
            }
        });

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        textView8 = (TextView) findViewById(R.id.textView8);
        textView7 = (TextView) findViewById(R.id.textView7);
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(num<9)
                            {
                                num++;
                                total++;
                                textView7.setText(Integer.toString(total));
                                textView8.setText(Integer.toString(num));
                            }
                        }
                    });
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(num>0)
                            {
                                num--;

                                textView8.setText(Integer.toString(num));
                            }
                        }
                    });
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_driver, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public  String POST(String url, Cab cab){

        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("cabSessionId", cab.getcabSessionId());
            jsonObject.accumulate("lat", cab.getlat());
            jsonObject.accumulate("lng", cab.getlng());
            jsonObject.accumulate("passengerCount", cab.getpassengerCount());
            jsonObject.accumulate("passengersAdded", cab.getpassengerTotal());

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // ** Alternative way to convert Person object to JSON string using Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost, context);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    public void onClick(View view) {



                // call AsynTask to perform network operation on separate thread

                final Handler handler = new Handler();

                handler.post(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        gps = new GPSTracker(Driver.this);

                        // check if GPS enabled
                        if (gps.canGetLocation()) {

                            lat = gps.getLatitude();
                            lng = gps.getLongitude();

                            // \n is for new line
                            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + lat + "\nLong: " + lng, Toast.LENGTH_LONG).show();
                        } else {
                            // can't get location
                            // GPS or Network is not enabled
                            // Ask user to enable GPS/network in settings
                            gps.showSettingsAlert();
                        }
                        new HttpAsyncTask().execute("http://cometride.elasticbeanstalk.com/api/driver/cab");
                        handler.postDelayed(this, 3000);

                    }
                }));

        }



    public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
        Toast.makeText(this, "The Switch is " + (isChecked ? "on" : "off"),
                Toast.LENGTH_SHORT).show();
        final Handler handler = new Handler();
        if(isChecked) {


            handler.post(new Thread(new Runnable() {
                @Override
                public void run() {
                    gps = new GPSTracker(Driver.this);

                    // check if GPS enabled
                    if (gps.canGetLocation()) {

                        lat = gps.getLatitude();
                        lng = gps.getLongitude();

                        // \n is for new line
                        Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + lat + "\nLong: " + lng, Toast.LENGTH_LONG).show();
                    } else {
                        // can't get location
                        // GPS or Network is not enabled
                        // Ask user to enable GPS/network in settings
                        gps.showSettingsAlert();
                    }
                    new HttpAsyncTask().execute("http://cometride.elasticbeanstalk.com/api/driver/cab");
                    handler.postDelayed(this, 3000);

                }
            }));

        } else {
            handler.removeCallbacksAndMessages(handler);
            Intent sevenIntent = new Intent(getApplicationContext(), DriverLogin.class);
            startActivityForResult(sevenIntent, 0);
        }
    }
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            cab = new Cab();
            Log.d("i want it here",sessionId);
            cab.setcabSessionId(sessionId);
            cab.setlat(lat);
            cab.setlng(lng);
            cab.setpassengerCount(Integer.parseInt(textView8.getText().toString()));
            cab.setpassengerTotal(Integer.parseInt(textView7.getText().toString()));


            return POST(urls[0],cab);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
            Log.d("result",result);
        }
    }


    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
