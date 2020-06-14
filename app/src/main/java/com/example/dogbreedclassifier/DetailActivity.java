package com.example.dogbreedclassifier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    static class DogInfo {
        int id;
        String name;
        Integer age;
        Integer weight;
        String size;
        String fur;
        String image;
        String result;
    }
    DogInfo dog = new DogInfo();
    List<String> breeds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getData();
    }

    public void getData(){
        openDB();

//        Log.e("DETAIL", "the id is "+dog.id);
//        Log.e("DETAIL", "the name is "+dog.name);
//        Log.e("DETAIL", "the age is "+dog.age);
//        Log.e("DETAIL", "the weight is "+dog.weight);
//        Log.e("DETAIL", "the size is "+dog.size);
//        Log.e("DETAIL", "the fur is "+dog.fur);
//        Log.e("DETAIL", "the image is "+dog.image);
//        Log.e("DETAIL", "the result is "+dog.result);

        List<String> obj= Arrays.asList(dog.result.split(", "));
        int listSize = obj.size();
        for(int i=0; i<listSize;i++){
            String str = obj.get(i);
            List<String> tmp = Arrays.asList(str.split(" "));
            String breed_name=tmp.get(1);
            breeds.add(breed_name);
        }

        setData();
    }

    public void setData(){
        ImageView image = findViewById(R.id.detail_image);
        if (dog.image != null){
            Uri imageUri = Uri.parse(dog.image);
            Bitmap bmp = null;
            try {
                bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                image.setImageBitmap(bmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        TextView result_text = findViewById(R.id.detail_result);
        result_text.setText(dog.result);

//        getWikiData(breeds.get(0));
        makeBtn(breeds);
    }

    public void openDB(){
        dog.id = getIntent().getExtras().getInt("id");
        String sql = "SELECT * FROM dogTBL WHERE Did="+dog.id+"";

        DBHelper DBHelper;
        SQLiteDatabase sqlDB;
        DBHelper = new DBHelper(this);
        sqlDB = DBHelper.getReadableDatabase();
        Cursor c = sqlDB.rawQuery(sql, null);
        c.moveToNext();

        dog.name = c.getString(c.getColumnIndex("Dname"));
        dog.age = c.getInt(c.getColumnIndex("Dage"));
        dog.weight = c.getInt(c.getColumnIndex("Dweight"));
        dog.size = c.getString(c.getColumnIndex("Dsize"));
        dog.fur = c.getString(c.getColumnIndex("Dfur"));
        dog.image = c.getString(c.getColumnIndex("Dimage"));
        dog.result = c.getString(c.getColumnIndex("Dresult"));
    }

    public void makeBtn(final List<String> breeds){
        LinearLayout ll = findViewById(R.id.btn_layout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        for(int i=0; i<breeds.size(); i++){
            Button linkBtn = new Button(this);
            final String breed_name = breeds.get(i);

            linkBtn.setText(breed_name);
            ll.addView(linkBtn, lp);

            TextView infoTxt = new TextView(this);
            infoTxt.setText("");
            switch(breed_name){
                case "말티즈":
                    infoTxt.setText(getString(R.string.Maltese_info));
                    break;
                case "포메라니안":
                    infoTxt.setText(getString(R.string.Pomeranian_info));
                    break;
                case "푸들":
                    infoTxt.setText(getString(R.string.Poodle_info));
                    break;
                case "시츄":
                    infoTxt.setText(getString(R.string.Shihtzu_info));
                    break;
                case "카디건_웰시_코기":
                case "펨브록_웰시_코기":
                    infoTxt.setText(getString(R.string.Welsh_corgi_info));
                    break;
                case "요크셔_테리어":
                    infoTxt.setText(getString(R.string.Yorkshire_info));
                    break;
            }
            ll.addView(infoTxt, lp);

            linkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openWiki(breed_name);
                }
            });
        }
    }

    public void openWiki(String keyword){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ko.wikipedia.org/wiki/"+keyword));
        startActivity(intent);
    }

//    public void getWikiData(String keyword){
////        String WIKIPEDIA_URL = "https://en.wikipedia.org/w/api.php?action=query&titles=" +
////                keyword
////                +"&prop=revisions&rvprop=content&format=json&prop=extracts";
//
//        String WIKIPEDIA_URL = "https://en.wikipedia.org/w/api.php?action=query&titles=" +
//                                "Pomeranian"
//                                +"&prop=revisions&rvprop=content&format=json&prop=extracts";
//
//        FetchWikiDataAsync fetchWikiDataAsync= new FetchWikiDataAsync();
//        fetchWikiDataAsync.execute(WIKIPEDIA_URL);
//    }
//
//    private class FetchWikiDataAsync extends AsyncTask<String, Void, String>{
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            TextView txtWikiData = findViewById(R.id.wiki_data);
//            txtWikiData.setText("위키 백과를 불러오는 중");
//        }
//
//        @Override
//        protected String doInBackground(String[] params) {
//            String sURL = params[0];
//            try {
//                URL url = new URL(sURL);
//                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                InputStream inputStream = urlConnection.getInputStream();
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//
//                StringBuilder stringBuilder = new StringBuilder();
//                String line;
//
//                while((line=bufferedReader.readLine()) != null) {
//                    stringBuilder.append(line);
//                }
//
//                String wikiData = stringBuilder.toString();
//
//                // Pasre JSON Data
//                String formattedData = parseJSONData(wikiData);
//
//                return formattedData;
//
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String formattedData) {
//            super.onPostExecute(formattedData);
//            TextView txtWikiData = findViewById(R.id.wiki_data);
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                txtWikiData.setText(Html.fromHtml(formattedData, Html.FROM_HTML_MODE_LEGACY));
//            }
//            else{
//                txtWikiData.setText(Html.fromHtml(formattedData));
//            }
//        }
//    }
//
//    private String parseJSONData(String wikiData){
//        Log.e("WIKI", ""+wikiData);
//        try {
//
//            JSONObject rootJSON = new JSONObject(wikiData);
//            JSONObject query = rootJSON.getJSONObject("query");
//            JSONObject pages = query.getJSONObject("pages");
//            JSONObject number = pages.getJSONObject(pages.keys().next());
//            Log.e("WIKI", ""+number);
//            String formattedData = number.getString("extract");
//
//            return formattedData;
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }

}
