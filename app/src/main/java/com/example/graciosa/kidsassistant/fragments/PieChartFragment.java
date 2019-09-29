package com.example.graciosa.kidsassistant.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

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

    /*********************
     *** INNER CLASSES ***
     *********************/

    private class IntegerFormatter implements IValueFormatter {

        @Override
        public String getFormattedValue(float value,
                                        Entry entry,
                                        int dataSetIndex,
                                        ViewPortHandler viewPortHandler) {
            Integer v = (int) value;
            return v.toString();
        }
    }
    /***************
     *** METHODS ***
     ***************/

    public PieChartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        MyLog.d(TAG, "onCreate");

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
        MyLog.d(TAG, "onResume");
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
        boolean overtime = played > limit;
        ArrayList<PieEntry> yValues = new ArrayList<>();
        int[] colors = new int[2];

        if (overtime){
            yValues.add(new PieEntry(limit, "Allowed"));
            yValues.add(new PieEntry(played - limit, "Overtime"));
            colors[0] = getResources().getColor(R.color.colorAccent, null);
            colors[1] = getResources().getColor(R.color.colorOrange, null);

        } else {
            yValues.add(new PieEntry(played, "Played"));
            yValues.add(new PieEntry(limit - played, "Remaining"));
            colors[0] = getResources().getColor(R.color.colorAccent, null);
            colors[1] = getResources().getColor(R.color.colorLightGrey, null);
        }

        PieDataSet dataSet = new PieDataSet(yValues, null);
        dataSet.setColors(colors);
        dataSet.setValueFormatter(new IntegerFormatter());
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);

        PieData pieData = new PieData((dataSet));
        pieData.setValueTextSize(20f);

        pieChart.setUsePercentValues(false);
        pieChart.setData(pieData);
    }
}
