package com.example.dogbreedclassifier;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_info);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        FrameLayout button = findViewById(R.id.result_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    getData();
                }
            }
        });

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
    public void getData(){
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

        startNewActivity(profileImage, dog.name, dog.age, dog.weight, dog.size, dog.fur);
    }

    public void startNewActivity(Uri imageUri, String dName, int dAge, int dWeight, String dSize, String dFur){
        Intent intent = new Intent(WriteInfoActivity.this, ResultActivity.class);
        intent.putExtra("imageUri", imageUri.toString());
        intent.putExtra("dog_name", dName);
        intent.putExtra("dog_age", dAge);
        intent.putExtra("dog_weight", dWeight);
        intent.putExtra("dog_size", dSize);
        intent.putExtra("dog_fur", dFur);

        startActivity(intent);
    }


}
