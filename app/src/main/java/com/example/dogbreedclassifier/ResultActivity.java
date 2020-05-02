package com.example.dogbreedclassifier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    PieChart pieChart;
    float[] yData = {10,20,70};
    String[] xData = {"푸들", "시추", "말티즈"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        makePieChart();
    }

    public void goHomePage(View view){
        Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    public void makePieChart(){
        pieChart = findViewById(R.id.result_chart);
        pieChart.setDescription(null);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);

        addChartData();

        Legend legend = pieChart.getLegend();
        legend.setTextSize(12f);
        legend.setDirection(Legend.LegendDirection.RIGHT_TO_LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setXEntrySpace(7);
        legend.setYEntrySpace(5);
    }

    private void addChartData(){
        List<PieEntry> entries = new ArrayList<>();
        for(int i=0; i<yData.length;i++){
            entries.add(new PieEntry(yData[i], xData[i%xData.length]));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Dog Breed");
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(5);

        ArrayList<Integer> colors = new ArrayList<>();
        for (int c: ColorTemplate.VORDIPLOM_COLORS) colors.add(c);
        for (int c: ColorTemplate.JOYFUL_COLORS) colors.add(c);
        for (int c: ColorTemplate.COLORFUL_COLORS) colors.add(c);
        for (int c: ColorTemplate.LIBERTY_COLORS) colors.add(c);
        for (int c: ColorTemplate.PASTEL_COLORS) colors.add(c);
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
