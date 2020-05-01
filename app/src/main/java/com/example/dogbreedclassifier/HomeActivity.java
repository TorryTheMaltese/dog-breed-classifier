package com.example.dogbreedclassifier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

public class HomeActivity extends AppCompatActivity {

    public static final String STRSAVEPATH = Environment.getExternalStorageDirectory()+"/testFolder/";
    public static final String SAVEFILENAME = "dogInfo.json";
    File file = new File(STRSAVEPATH+SAVEFILENAME);
    static class DogInfo implements Serializable {
        String imageUri;
        String name;
        Integer age;
        Integer weight;
        String size;
        String fur;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        readFile(file);
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
