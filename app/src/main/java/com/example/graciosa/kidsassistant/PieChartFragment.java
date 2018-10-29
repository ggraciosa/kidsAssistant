package com.example.graciosa.kidsassistant;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;


public class PieChartFragment extends Fragment {

    public PieChartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        pieChart.setExtraOffsets(5,5,5,5);
        pieChart.setDragDecelerationFrictionCoef(0.9f);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.animateY(1000, Easing.EasingOption.EaseInOutCubic);

        // set text on top of the pie
        pieChart.setEntryLabelColor(Color.DKGRAY);
        pieChart.setEntryLabelTextSize(16f);

        // set pie pieces
        MySharedPrefManager sp = new MySharedPrefManager(getActivity());
        long playedTime = sp.getPlayedTimeInMinutes();
        long maxPlayingTime = sp.getMaxPlayingTimeInMinutes();
        playedTime = Math.min((int) playedTime, (int) maxPlayingTime);
        long remainingTime = maxPlayingTime - playedTime;
        ArrayList<PieEntry> yValues = new ArrayList<>();
        yValues.add(new PieEntry((float) playedTime,"Played"));
        yValues.add(new PieEntry((float) remainingTime,"Remaining"));

        PieDataSet dataSet = new PieDataSet(yValues, null);
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // API level >= 23
            dataSet.setColors(getResources().getColor(R.color.colorAccent, null),
                    getResources().getColor(R.color.colorAccentAlpha40, null));
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
        //pieData.setValueTextColor(getResources().getColor(R.color.orange, null));

        pieChart.setData(pieData);
    }

}
