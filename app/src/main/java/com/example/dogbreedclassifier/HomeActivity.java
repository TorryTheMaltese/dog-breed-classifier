package com.example.dogbreedclassifier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {

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

    RecyclerView mRecyclerView = null;
    Adapter mAdapter = null;
    ArrayList<com.example.dogbreedclassifier.RecyclerView> mList = new ArrayList<>();

    private BackPressCloseHandler backPressCloseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        backPressCloseHandler = new BackPressCloseHandler(this);

        mRecyclerView= findViewById(R.id.recycler);
        mAdapter= new Adapter(mList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        externalFontFamily();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        openDB();

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull final RecyclerView rv, @NonNull MotionEvent e) {
                if(e.getAction() == MotionEvent.ACTION_DOWN){
                    final View child = rv.findChildViewUnder(e.getX(), e.getY());

                    ImageButton ib = rv.getChildViewHolder(child).itemView.findViewById(R.id.imagebtn);
                    ib.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int currentPosition = rv.findViewHolderForAdapterPosition(rv.getChildLayoutPosition(child)).getAdapterPosition();
                            Log.e("DEL", "");
                            deleteItem(currentPosition);
                            mAdapter.deleteItem(currentPosition);
                            mAdapter.notifyItemRemoved(currentPosition);
                        }
                    });
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });



        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);

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

    @Override
    public void onBackPressed(){
        backPressCloseHandler.onBackPressed();
    }

    public void externalFontFamily(){

    }

    public class BackPressCloseHandler {
        private long backKeyPressedTime= 0;
        private Toast toast;
        private Activity activity;

        public BackPressCloseHandler(Activity context){ this.activity = context; }

        public void onBackPressed(){
            if(System.currentTimeMillis() > backKeyPressedTime + 2000){
                backKeyPressedTime = System.currentTimeMillis();
                toast = Toast.makeText(activity, "뒤로가기 버튼을 두 번 누르면 종료됩니다.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            else{
                activity.finish();
                toast.cancel();
            }
        }
    }

    public void addItem(Uri image, String dogName, String dogAge, String dogWeight){
        com.example.dogbreedclassifier.RecyclerView item= new com.example.dogbreedclassifier.RecyclerView();
        item.setDog_image(image);
        item.setDog_name(dogName);
        item.setDog_age(dogAge);
        item.setDog_weight(dogWeight);

        mList.add(item);
    }

    public void deleteItem(Integer position){
        Integer id = position+1;
        String name = mList.get(position).getDog_name();
        DBHelper DBHelper;
        SQLiteDatabase sqlDB;
        DBHelper = new DBHelper(this);
        sqlDB = DBHelper.getWritableDatabase();

        sqlDB.execSQL("DELETE FROM dogTBL WHERE Dname = '"+name+"';");
        sqlDB.close();
    }

    public void openDB(){
        DBHelper DBHelper;
        SQLiteDatabase sqlDB;
        DBHelper = new DBHelper(this);
        DogInfo dogInfo = new DogInfo();

        sqlDB = DBHelper.getReadableDatabase();
        Cursor c = sqlDB.rawQuery("SELECT * FROM dogTBL", null);

        if(c.moveToFirst()){
            while(!c.isAfterLast()){
                dogInfo.name = c.getString(c.getColumnIndex("Dname"));
                dogInfo.age = c.getInt(c.getColumnIndex("Dage"));
                dogInfo.weight = c.getInt(c.getColumnIndex("Dweight"));
                dogInfo.size = c.getString(c.getColumnIndex("Dsize"));
                dogInfo.fur = c.getString(c.getColumnIndex("Dfur"));
                dogInfo.imageUri = c.getString(c.getColumnIndex("Dimage"));
                Uri imageUri = Uri.parse(dogInfo.imageUri);

                addItem(imageUri, dogInfo.name, String.valueOf(dogInfo.age), String.valueOf(dogInfo.weight));
                mAdapter.notifyDataSetChanged();

                c.moveToNext();
            }
        }

    }

    public void getWeather(Location location){

        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + api_key;
        ReceiveWeatherTask receiveUseTask = new ReceiveWeatherTask();
        receiveUseTask.execute(url);
    }

    private class ReceiveWeatherTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(strings[0]).openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    InputStreamReader reader = new InputStreamReader(is);
                    BufferedReader in = new BufferedReader(reader);

                    String readed;
                    while ((readed = in.readLine()) != null) {
                        JSONObject jObject = new JSONObject(readed);
                        return jObject;
                    }
                } else {
                    return null;
                }
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
            if (result != null) {
                String nowTemp = "";
                double nowCTemp;
                String main = "";
                String description = "";

                try {
                    nowTemp = result.getJSONObject("main").getString("temp");
                    main = result.getJSONArray("weather").getJSONObject(0).getString("main");
                    description = result.getJSONArray("weather").getJSONObject(0).getString("description");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                nowCTemp = Double.parseDouble(nowTemp) - 273;
                String ctmp = String.format("%.1f", nowCTemp);
                String msg = description + "\n" + "현재온도 : " + ctmp + "℃";
                TextView text = findViewById(R.id.temp);
                text.setText(msg);
            }
        }
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == sensor) {
            sensorValue = event.values[0];
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void startNewActivity(View view) {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
    }


}
