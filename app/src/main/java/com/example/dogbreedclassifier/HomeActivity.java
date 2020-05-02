package com.example.dogbreedclassifier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {

    public static final String STRSAVEPATH = Environment.getExternalStorageDirectory() + "/testFolder/";
    public static final String SAVEFILENAME = "dogInfo.json";
    File file = new File(STRSAVEPATH + SAVEFILENAME);
    String api_key = "e42e319b66ef5c4af146d334e6f117dc";

    static class DogInfo implements Serializable {
        String imageUri;
        String name;
        Integer age;
        Integer weight;
        String size;
        String fur;
    }

    SensorManager sensorManager;
    Sensor sensor;
    float sensorValue;

    SwipeLayout swipe_card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        readFile(file);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        swipe_card = findViewById(R.id.sample1);
        swipe_card.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipe_card.addDrag(SwipeLayout.DragEdge.Right, findViewById(R.id.bottom_wrapper));
        swipe_card.addSwipeListener(new SwipeLayout.SwipeListener() {

            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {

            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {

            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });

        ActivityCompat.requestPermissions(this, new String[] {ACCESS_FINE_LOCATION},1);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                getWeather(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void getWeather(Location location){
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        String url = "http://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid="+api_key;
        ReceiveWeatherTask receiveUseTask = new ReceiveWeatherTask();
        receiveUseTask.execute(url);
    }

    private class ReceiveWeatherTask extends AsyncTask<String, Void, JSONObject>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            try{
                HttpURLConnection conn = (HttpURLConnection) new URL(strings[0]).openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.connect();

                if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){
                    InputStream is = conn.getInputStream();
                    InputStreamReader reader = new InputStreamReader(is);
                    BufferedReader in = new BufferedReader(reader);

                    String readed;
                    while((readed = in.readLine()) != null){
                        JSONObject jObject = new JSONObject(readed);
                        return jObject;
                    }
                }
                else {return null;}
                return null;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            Log.e("API", result.toString());
            if(result!=null){
                String nowTemp = "";
                double nowCTemp;
                String main = "";
                String description = "";

                try{
                    nowTemp = result.getJSONObject("main").getString("temp");
                    main = result.getJSONArray("weather").getJSONObject(0).getString("main");
                    description = result.getJSONArray("weather").getJSONObject(0).getString("description");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                nowCTemp = Double.parseDouble(nowTemp)-273;
                String msg = description+"\n"+"현재온도 : "+nowCTemp;
                TextView text = findViewById(R.id.temp);
                text.setText(msg);
            }
        }
    }

    public void onSensorChanged(SensorEvent event) {
        if(event.sensor==sensor) {
            sensorValue = event.values[0];
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setData(DogInfo dogInfo){
        ImageView savedImage = findViewById(R.id.saved_dog_image);
        savedImage.setImageURI(Uri.parse(dogInfo.imageUri));

        TextView savedName = findViewById(R.id.saved_dog_name);
        savedName.setText(dogInfo.name);
    }

    private void readFile(File file){
        int readCount=0;
        if(file!=null && file.exists()){
            try{
                FileInputStream fis = new FileInputStream(file);
                readCount = (int) file.length();
                byte[] buffer = new byte[readCount];
                fis.read(buffer);
                String content = new String(buffer, "UTF-8");
                JSONObject object = new JSONObject(content);

                DogInfo dogInfo = new DogInfo();
                dogInfo.imageUri = object.getString("imageUri");
                dogInfo.name = object.getString("name");
                dogInfo.age = object.getInt("age");
                dogInfo.weight = object.getInt("weight");
                setData(dogInfo);
                fis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void startNewActivity(View view){
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
