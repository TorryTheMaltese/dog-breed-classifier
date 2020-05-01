package com.example.dogbreedclassifier;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class WriteInfoActivity extends AppCompatActivity {

    static class DogInfo implements Serializable {
        String imageUri;
        String name;
        Integer age;
        Integer weight;
        String size;
        String fur;
    }

    String size;
    String fur;
    Uri profileImage;

    public static final String STRSAVEPATH = Environment.getExternalStorageDirectory()+"/testFolder/";
    public static final String SAVEFILENAME = "dogInfo.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_info);

        RadioGroup dogSize = findViewById(R.id.dog_size),
                dogFur = findViewById(R.id.dog_fur);

        dogSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = findViewById(checkedId);
                size = radioButton.getText().toString();
            }
        });

        dogFur.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = findViewById(checkedId);
                fur = radioButton.getText().toString();
            }
        });
        getImage();

    }

    public void getImage() {
        ImageView image = findViewById(R.id.profileImg);
        if(getIntent().getExtras().getByteArray("imageByte") != null) {
            byte[] byteArray = getIntent().getExtras().getByteArray("imageByte");
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            profileImage = getImageUri(this,bmp);
            image.setImageBitmap(bmp);
        } else {
            String imageString = getIntent().getExtras().getString("imageString");
            profileImage = Uri.parse(imageString);
            image.setImageURI(profileImage);
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getData(View view){
        EditText dogName = findViewById(R.id.dog_name),
                dogAge = findViewById(R.id.dog_age),
                dogWeight = findViewById(R.id.dog_weight);

        DogInfo dog = new DogInfo();
        dog.name = dogName.getText().toString();
        dog.age = Integer.parseInt(dogAge.getText().toString());
        dog.weight = Integer.parseInt(dogWeight.getText().toString());
        dog.size = size;
        dog.fur = fur;
        dog.imageUri = profileImage.toString();

        storeData(dog);
        startNewActivity();
    }

    public void storeData(DogInfo dog){
        Gson gson = new Gson();
        String content = gson.toJson(dog);

        File dir = makeDirectory(STRSAVEPATH);
        File file = makeFile(dir, STRSAVEPATH+SAVEFILENAME);
        writeFile(file, content.getBytes());
//        readFile(file);
    }

    public String loadJSONFromAsset(){
        String json = null;
        try {
            InputStream input = getAssets().open("DogInfo.json");
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    private File makeDirectory(String dir_path){
        File dir = new File(dir_path);
        if(!dir.exists()){
            dir.mkdir();
            Log.e("DIR_TEST","dir does not exist");
        }
        else{
            Log.i("DIR_TEST", "dir exist");
        }
        return dir;
    }

    private File makeFile(File dir, String file_path){
        File file = null;
        boolean isSuccess = false;
        Log.e("DIR_ID_DIRECTORY", dir.toString());

            file = new File(file_path);
                try{
                    isSuccess = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    Log.e("CREATE+FILE", "creating file = "+isSuccess);
                }
        return file;
    }

    private boolean writeFile(File file, byte[] file_content){
        boolean result;
        FileOutputStream fos;
        if(file != null && file.exists() && file_content!=null){
            try{
                fos = new FileOutputStream(file);
                try{
                    fos.write(file_content);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            result = true;
        }
        else{result = false;}
        return result;
    }

    public void startNewActivity(){
        Intent intent = new Intent(WriteInfoActivity.this, ResultActivity.class);
        startActivity(intent);
    }
}
