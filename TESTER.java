import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class TESTER extends AppCompatActivity implements OnMapReadyCallback {

    //Call getDirections() once your map is created.
    //Route will be automatically plotted on the map along with two markers representing origin and destination.

    //Consider you already have decided two point on the map between which you want to seek directions.
    //Let these two points be :
    private LatLng origin = new LatLng(19.103265, 72.874282);
    private LatLng destination = new LatLng(19.052173, 72.825570);

    //Enter your Google Maps API key in this variable. Using an android string resource might create errors.
    private String api_key = "YOUR_API_KEY_GOES_HERE";

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tester);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*getDirections() arguments :
                1. GoogleMap
                2. Origin LatLng
                3. Destination LatLng
                4. Google Maps API Key String
                5. Polyline width Float
                6. Polyline color Color
         */
        getDirections(mMap, origin, destination, api_key, Float.valueOf("4.0"), R.color.colorPrimary);
    }

    private void getDirections(GoogleMap mMap, LatLng origin, LatLng destination, String api_key, Float inWidth, int inColor)
    {
        //Move camera to origin
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 15));


        //Forming an URL string which will return JSON as a result.
        String originString = "origin=" + origin.latitude + "," + origin.longitude;
        String destinationString = "destination=" + destination.latitude + "," + destination.longitude;

        //IF THIS GENERATES ERROR, HARD CODE API KEY INTO URL.
        String url = "https://maps.googleapis.com/maps/api/directions/json?"+ originString + "&" + destinationString + "&key=" + api_key;


        //Run the URL formed in above step and wait for result.
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);
    }

    private String downloadUrl(String url) throws IOException
    {
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        try
        {
            URL actualURL = new URL(url);
            urlConnection = (HttpURLConnection)actualURL.openConnection();
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while((line = br.readLine()) != null)
            {
                sb.append(line);
            }

            data = sb.toString();

            br.close();
        }
        catch (Exception e)
        {
            Log.d("EXCEPTION DOWNLADING", e.toString());
        }
        finally {
            inputStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... strings) {
            String data = "";

            try
            {
                data = downloadUrl(strings[0]);
            }
            catch (Exception e)
            {
                Log.d("ASYNC TASK", e.toString());
            }

            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Toast.makeText(showPreviousOrder.this, s, Toast.LENGTH_LONG).show();

            int totalDistance = 0;
            int totalTravelTime = 0;

            try {
                JSONObject parentMain = new JSONObject(s);
                JSONArray legs = parentMain.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");

                for(int i = 0; i < legs.length(); i++)
                {
                    JSONArray steps = legs.getJSONObject(i).getJSONArray("steps");
                    JSONObject distance = legs.getJSONObject(i).getJSONObject("distance");
                    JSONObject duration = legs.getJSONObject(i).getJSONObject("duration");

                    totalDistance += Integer.parseInt(distance.getString("value"));
                    totalTravelTime += Integer.parseInt(duration.getString("value"));

                    for(int j = 0; j < steps.length(); j++)
                    {
                        JSONObject polyline = steps.getJSONObject(j).getJSONObject("polyline");
                        List<LatLng> markers = PolyUtil.decode(polyline.getString("points"));

                        mMap.addPolyline(new PolylineOptions().addAll(markers).width(Float.valueOf("4.0")).color(Color.GREEN));
                    }
                }

            } catch (JSONException e) {
                Toast.makeText(TESTER.this, "WELL WE MESSED UP!", Toast.LENGTH_LONG).show();
            }
            toastData(totalDistance, totalTravelTime);
        }

        //Simply displays a toast message containing total distance and total time required.
        public void toastData(int totalDistance, int totalTravelTime)
        {
            int km = 0, m = 0;
            String displayDistance = "";

            if(totalDistance < 1000)
            {
                displayDistance = "0." + String.valueOf(totalDistance) + " km";
            }
            else
            {
                while(totalDistance >= 1000)
                {
                    km++;
                    totalDistance -= 1000;
                }
                m = totalDistance;
                displayDistance = String.valueOf(km) + "." + String.valueOf(m) + " km";
            }

            int min = 0, sec = 0;
            String displayTravelTime = "";
            if(totalDistance < 60)
                displayTravelTime = "1 minute";
            else
            {
                while(totalTravelTime >= 60)
                {
                    min++;
                    totalTravelTime -= 60;
                }
                sec = totalTravelTime;
                displayTravelTime = String.valueOf(min) + ":" + String.valueOf(sec) + " minutes";
            }

            Toast.makeText(TESTER.this, "DISTANCE : " + displayDistance + "\nTIME REQUIRED : " + displayTravelTime, Toast.LENGTH_LONG).show();
        }
    }
}
