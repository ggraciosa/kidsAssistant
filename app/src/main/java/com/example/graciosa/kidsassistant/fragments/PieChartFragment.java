package com.example.graciosa.kidsassistant.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.graciosa.kidsassistant.MyLog;
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

    /*****************
     *** CONSTANTS ***
     *****************/

    public static final String TAG = PieChartFragment.class.getSimpleName();

    /**************
     *** FIELDS ***
     **************/

    private MySharedPrefManager mMySp;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;

    /***************
     *** METHODS ***
     ***************/

    public PieChartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mMySp = new MySharedPrefManager(getActivity());
        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                // Update pie chart with changes in played time if they occur while this chart is
                // being displayed
                if (!key.equals(MySharedPrefManager.SHARED_PREF_PLAYED_TIME_KEY)){
                    // Discard
                    MyLog.d(TAG,"onSharedPreferenceChanged: Discarding key=" + key + " value=" + prefs.getLong(key,-1));
                    return;
                } else {
                    // Get pie chart view and update pieces
                    MyLog.d(TAG,"onSharedPreferenceChanged: Processing key=" + key + " value=" + prefs.getLong(key,-1));
                    PieChart pieChart = (PieChart) getView().findViewById(R.id.pie_chart);
                    updatePieChart(pieChart);
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View pieChartFragmentView = inflater.inflate(R.layout.fragment_pie_chart, container, false);
        createPieChart((PieChart) pieChartFragmentView.findViewById(R.id.pie_chart));
        return pieChartFragmentView;
    }

    @Override
    public void onResume(){
        super.onResume();
        // Register to listen to updates in played time
        mMySp.getPlayedSharedPref().registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onPause(){
        super.onPause();
        // Unregister to listen to updates in played time
        mMySp.getPlayedSharedPref().unregisterOnSharedPreferenceChangeListener(mListener);
    }


    private void createPieChart(PieChart pieChart) {
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

        // Set pie pieces and update view
        setPieChartPieces(pieChart);
    }

    // pieChart: the pie chart view to have its pieces update
    private void updatePieChart(PieChart pieChart){
        setPieChartPieces(pieChart);
        pieChart.notifyDataSetChanged();
        pieChart.invalidate();
    }

    // pieChart: the pie chart view to have its pieces set
    private void setPieChartPieces(PieChart pieChart){

        int played = (int) mMySp.getPlayedTimeInMinutes();
        int limit = (int) mMySp.getPlayTimeLimitInMinutes();
        int playedTimePie = Math.min(played,    limit);
        int remainingTimePie = limit - playedTimePie;
        ArrayList<PieEntry> yValues = new ArrayList<>();
        yValues.add(new PieEntry(playedTimePie, "Played"));
        yValues.add(new PieEntry(remainingTimePie, "Remaining"));
        PieDataSet dataSet = new PieDataSet(yValues, null);
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);

        // Set pie color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // API level >= 23
            if (played <= limit) {
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
        // ArrayList<Integer> textColors = new ArrayList<>();
        // textColors.add(Color.DKGRAY);
        // textColors.add(Color.LTGRAY);
        // pieData.setValueTextColors(textColors);
        // pieData.setValueTextColor(Color.DKGRAY);

        pieChart.setData(pieData);
    }

}
