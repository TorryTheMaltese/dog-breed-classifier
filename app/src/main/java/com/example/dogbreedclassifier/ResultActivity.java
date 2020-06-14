package com.example.dogbreedclassifier;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ResultActivity extends AppCompatActivity {

    // mobilenet: 224, inception_v3: 299
    private static final int INPUT_SIZE = 299;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128;
    // mobilenet: input, inception_v3: Mul, Keras: input_1
    private static final String INPUT_NAME = "Mul";
    // output, inception(tf): final_result, Keras: output/Softmax
    private static final String OUTPUT_NAME = "final_result";

    private Classifier classifier;
    private static final String MODEL_FILE = "file:///android_asset/stripped.pb";

    protected ArrayList<String> currentRecognitions;
    TextView resultsView;

    PieChart pieChart;
    float[] yData = {10, 20, 70};
    String[] xData = {"푸들", "시추", "말티즈"};
    List final_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorBackground));
        }

        getImage();

    }


    public void getImage(){
        ImageView image = findViewById(R.id.result_image);
         if (getIntent().getExtras().getString("imageUri") != null){
             Uri imageUri = Uri.parse(getIntent().getExtras().getString("imageUri"));
             Bitmap bmp = null;
             try {
                 bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                 int orientation=0;
                 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                     orientation = getOrientation(getApplicationContext(), imageUri);
                 }
                 Bitmap croppedImage = resizeCropAndRotate(bmp, orientation);
                 image.setImageBitmap(bmp);
                 ResultActivity.InferenceTask inferenceTask;
                 inferenceTask = new InferenceTask();
                 inferenceTask.execute(croppedImage);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
    }

    public void addDatabase(){
        DBHelper myHelper;
        myHelper = new DBHelper(this);
        SQLiteDatabase sqlDB;

        String dName, dSize, dFur, dImage, listString;
        int dAge, dWeight;

        dName = getIntent().getExtras().getString("dog_name");
        dAge = getIntent().getExtras().getInt("dog_age");
        dWeight = getIntent().getExtras().getInt("dog_weight");
        dSize = getIntent().getExtras().getString("dog_size");
        dFur = getIntent().getExtras().getString("dog_fur");
        dImage = getIntent().getExtras().getString("imageUri");
        listString="";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            listString= TextUtils.join(", ", final_result);
            System.out.println(listString);
            Log.e("RES", "listString : "+listString);
        }

        sqlDB = myHelper.getWritableDatabase();
        sqlDB.execSQL("INSERT INTO dogTBL(Dname,Dage,Dweight,Dsize,Dfur,Dimage, Dresult) VALUES ('" +
                dName + "',"
                + dAge +
                ", "+ dWeight+",'"+ dSize +"','"+dFur+"','"+dImage+"','"+listString+"');");
        sqlDB.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        try (final Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null)
        ) {
            if (cursor.getCount() != 1) {
                cursor.close();
                return -1;
            }

            if (cursor != null && cursor.moveToFirst()) {
                final int r = cursor.getInt(0);
                cursor.close();
                return r;
            }

        } catch (Exception e) {
            return -1;
        }
        return -1;
    }

    private Bitmap resizeCropAndRotate(Bitmap originalImage, int orientation) {
        Bitmap result = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888);

        final float originalWidth = originalImage.getWidth();
        final float originalHeight = originalImage.getHeight();

        final Canvas canvas = new Canvas(result);

        final float scale = INPUT_SIZE / originalWidth;

        final float xTranslation = 0.0f;
        final float yTranslation = (INPUT_SIZE - originalHeight * scale) / 2.0f;

        final Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);

        final Paint paint = new Paint();
        paint.setFilterBitmap(true);

        canvas.drawBitmap(originalImage, transformation, paint);

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            final Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            result = Bitmap.createBitmap(result, 0, 0, INPUT_SIZE,
                    INPUT_SIZE, matrix, true);
        }

        return result;
    }

    protected synchronized void initClassifier() {
        if (classifier == null)
            try {
                classifier =
                        TensorFlowImageClassifier.create(
                                getAssets(),
                                MODEL_FILE,
                                getResources().getStringArray(R.array.breeds_array),
                                INPUT_SIZE,
                                IMAGE_MEAN,
                                IMAGE_STD,
                                INPUT_NAME,
                                OUTPUT_NAME);
            } catch (OutOfMemoryError e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("TEST", "TEST");
                    }
                });
            }
    }

    void updateResults(final List<Classifier.Recognition> results) {
        Log.e("TEST updateResults", results.toString());
        makePieChart(results);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                ResultActivity.this.updateResultsView(results);
//            }
//        });
    }

    void updateResultsView(List<Classifier.Recognition> results){
        final StringBuilder sb = new StringBuilder();
        currentRecognitions = new ArrayList<String>();

        if (results != null) {
            Log.e("TEST updateResultView", results.toString());
//            resultsView.setEnabled(true);

            if (results.size() > 0) {
                for (final Classifier.Recognition recog : results) {
                    final String text = String.format(Locale.getDefault(), "%s: %d %%\n",
                            recog.getTitle(), Math.round(recog.getConfidence() * 100));
                    sb.append(text);
                    currentRecognitions.add(recog.getTitle());
                }
            } else {
                sb.append("no detection");
            }
        } else {
            resultsView.setEnabled(false);
        }

        final String finalText = sb.toString();
        resultsView.setText(finalText);
    }

    protected class InferenceTask extends AsyncTask<Bitmap, Void, List<Classifier.Recognition>>{

        @Override
        protected List<Classifier.Recognition> doInBackground(Bitmap... bitmaps) {
            initClassifier();

            if(!isCancelled() && classifier != null){
                Log.e("TEST InferenceTask", classifier.toString());
                return classifier.recognizeImage(bitmaps[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Classifier.Recognition> recognitions) {
            if(!isCancelled()) updateResults(recognitions);
            Button btnSave = findViewById(R.id.btn_save);
            btnSave.setVisibility(View.VISIBLE);
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addDatabase();
                    goHomePage();
                }
            });
        }
    }

    public void goHomePage() {
        Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    public void makePieChart(List<Classifier.Recognition> results){
        pieChart = findViewById(R.id.result_chart);
        pieChart.setDescription(null);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);

        final_result = results;
        addChartData(results);

        Legend legend = pieChart.getLegend();
        legend.setTextSize(12f);
        legend.setDirection(Legend.LegendDirection.RIGHT_TO_LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setXEntrySpace(7);
        legend.setYEntrySpace(5);
    }

    private void addChartData(List<Classifier.Recognition> results){
        final ArrayList<PieEntry> entries = new ArrayList<>();
        float sum = 0;
        if(results != null){
            for (int i = 0; i<results.size(); i++){
                sum += results.get(i).getConfidence();
                PieEntry entry = new PieEntry(results.get(i).getConfidence() * 100, results.get(i).getTitle());
                entries.add(entry);
            }
        }
//        List<PieEntry> entries = new ArrayList<>();
//        for(int i=0; i<yData.length;i++){
//            entries.add(new PieEntry(yData[i], xData[i%xData.length]));
//        }

        PieDataSet dataSet = new PieDataSet(entries, "Dog Breed");
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(5);

        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS) colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS) colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS) colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(15f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(20f);
        pieChart.highlightValue(null);
        pieChart.invalidate();
    }


}