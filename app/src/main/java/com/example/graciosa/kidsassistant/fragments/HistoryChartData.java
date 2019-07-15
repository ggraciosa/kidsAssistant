package com.example.graciosa.kidsassistant.fragments;

import com.github.mikephil.charting.data.BarData;

import java.lang.reflect.Array;
import java.util.ArrayList;

/*
 * This class intends to encapsulate all data required to customize a history chart.
 * It is necessary because the BarData object built in HistoryFragment holds the data (e.g. bar
 * value and color) of each bar but not its metadata such as the value to be displayed in x axis,
 * which is desired to be the day of the date associated with each bar.
 * Axis customization can only be set through BarChart class, which is a View customized and
 * provided to RecyclerViewer by HistoryAdapter class.
 * So this class intends to allow HistoryFragment to build both bar data (BarData) and bar
 * metadata (dates, colors, etc.) and transfer it to HistoryAdapter for proper customization of
 * each BarChart view.
 */

public class HistoryChartData {

    public static final String EMPTY = "EMPTY";
    public static final float BAR_TEXT_SIZE = 11f;
    public static final int BARS_PER_CHART = 12;

    /**************
     *** FIELDS ***
     **************/

    // BarData is already an array holding data (e.g. values, color) of each bar of the chart
    private BarData mBarsData;
    // Dates of each bar of the chart
    private ArrayList<String> mBarsDate;

    /***************
     *** METHODS ***
     ***************/

    public HistoryChartData(BarData data, ArrayList<String> dates){
        mBarsData = data;
        mBarsDate = dates;
    }

    public BarData getBarsData() {
        return mBarsData;
    }

    public ArrayList<String> getBarsDate() {
        return mBarsDate;
    }
}
