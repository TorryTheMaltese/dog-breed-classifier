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
//--------추가----------
    myDBHelper myHelper;
    EditText dog_name,dog_age,dog_weight;
    SQLiteDatabase sqlDB;
//-----------------
    String size;
    String fur;
    Uri profileImage;
    byte[] imageByteArray = {};

    public static final String STRSAVEPATH = Environment.getExternalStorageDirectory()+"/testFolder/";
    public static final String SAVEFILENAME = "dogInfo.json";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_info);
        //--------------------- 추가---------------------------------------------
        dog_name=(EditText)findViewById(R.id.dog_name);
        dog_age=(EditText) findViewById(R.id.dog_age);
        dog_weight=(EditText)findViewById(R.id.dog_weight);
        ImageButton btn_insert=(ImageButton) findViewById(R.id.btn_insert);
        myHelper = new myDBHelper(this);

        btn_insert.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View v) {
              sqlDB = myHelper.getWritableDatabase();
              sqlDB.execSQL("INSERT INTO dogTBL VALUES ('" +
                      dog_name.getText().toString() + "',"
                      + dog_age.getText().toString() +
                      ", "+ dog_weight.getText().toString()+",'"+ size +"','"+fur+"');");
                sqlDB.close();
               Toast.makeText(getApplicationContext(),"추가완료",0).show();

            }
        });
        //------------------------------------------------------------------
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
   //------------------------추가--------------------------------
     public class myDBHelper extends SQLiteOpenHelper{
     public myDBHelper(Context context){
         super(context,"DOGINFO",null,1);
     }
     @Override
       public void onCreate(SQLiteDatabase db){
         db.execSQL("CREATE TABLE dogTBL (Dname CHAR(20) , Dage INTEGER, Dweight INTEGER, Dsize char(20), Dfur char(20) );");
     }
     @Override
       public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
         db.execSQL("DROP TABLE IF EXISTS dogTBL");
         onCreate(db);
     }
    }
    //----------------------------------------------------------
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

        storeData(dog);
        startNewActivity(profileImage);
    }

    public void storeData(DogInfo dog){
        Gson gson = new Gson();
        String content = gson.toJson(dog);

        File dir = makeDirectory(STRSAVEPATH);
        File file = makeFile(dir, STRSAVEPATH+SAVEFILENAME);
        writeFile(file, content.getBytes());
//        readFile(file);
    }

//    public String loadJSONFromAsset(){
//        String json = null;
//        try {
//            InputStream input = getAssets().open("DogInfo.json");
//            int size = input.available();
//            byte[] buffer = new byte[size];
//            input.read(buffer);
//            input.close();
//            json = new String(buffer, "UTF-8");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return json;
//    }

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

    public void startNewActivity(Uri imageUri){
        Intent intent = new Intent(WriteInfoActivity.this, ResultActivity.class);
        intent.putExtra("imageUri", imageUri.toString());
        startActivity(intent);
    }


}
