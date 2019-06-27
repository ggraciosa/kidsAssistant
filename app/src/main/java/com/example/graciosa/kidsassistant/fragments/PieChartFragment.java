package com.example.graciosa.kidsassistant.fragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.graciosa.kidsassistant.MySharedPrefManager;
import com.example.graciosa.kidsassistant.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;


public class PieChartFragment extends Fragment {

    private MySharedPrefManager mSp;

    public PieChartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mSp = new MySharedPrefManager(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View pieChartFragmentView = inflater.inflate(R.layout.fragment_pie_chart, container, false);
        setPieChart((PieChart) pieChartFragmentView.findViewById(R.id.pie_chart));
        return pieChartFragmentView;
    }

    private void setPieChart(PieChart pieChart) {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setExtraOffsets(5, 5, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.9f);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.animateY(1000, Easing.EasingOption.EaseInOutCubic);

        // Set text on top of the pie
        pieChart.setEntryLabelColor(Color.DKGRAY);
        pieChart.setEntryLabelTextSize(16f);

        // Set pie pieces
        long playedTime = mSp.getPlayedTimeInMinutes();
        long playTimeLimit = mSp.getPlayTimeLimitInMinutes();
        long playedTimePie = Math.min((int) playedTime, (int) playTimeLimit);
        long remainingTimePie = playTimeLimit - playedTimePie;
        ArrayList<PieEntry> yValues = new ArrayList<>();
        yValues.add(new PieEntry(playedTimePie, "Played"));
        yValues.add(new PieEntry(remainingTimePie, "Remaining"));

        PieDataSet dataSet = new PieDataSet(yValues, null);
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);

        // Set pie color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // API level >= 23
            if (playedTime <= playTimeLimit){
                // Play time is within limit
                dataSet.setColors(getResources().getColor(R.color.colorAccent, null),
                    getResources().getColor(R.color.colorLightGrey, null));
            } else {
                // Play time limit exceeded
                dataSet.setColors(getResources().getColor(R.color.colorOrange, null),
                        getResources().getColor(R.color.colorOrange, null));
            }

        } else {
            // API level < 23
            dataSet.setColors(ColorTemplate.PASTEL_COLORS);
        }

        PieData pieData = new PieData((dataSet));
        pieData.setValueTextSize(20f);
        // pieData.setValueTextColor(Color.DKGRAY);
        /*ArrayList<Integer> textColors = new ArrayList<>();
        textColors.add(Color.DKGRAY);
        textColors.add(Color.LTGRAY);
        pieData.setValueTextColors(textColors);*/
        pieData.setValueTextColor(Color.DKGRAY);

        pieChart.setData(pieData);
    }

}
