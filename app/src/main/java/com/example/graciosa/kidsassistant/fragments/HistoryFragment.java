package com.example.graciosa.kidsassistant.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graciosa.kidsassistant.MyLog;
import com.example.graciosa.kidsassistant.R;
import com.example.graciosa.kidsassistant.db.PlayedTimeEntity;
import com.example.graciosa.kidsassistant.db.PlayedTimeViewModel;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;


public class HistoryFragment extends Fragment {

    /*****************
     *** CONSTANTS ***
     *****************/

    final String TAG = HistoryFragment.class.getSimpleName();

    /**************
     *** FIELDS ***
     **************/

    // ViewModels holds live data from db.
    private PlayedTimeViewModel mViewModel;
    // RecyclerView list. Requests view as user scrolls in list, in an efficient way.
    protected RecyclerView mRecyclerView;
    protected HistoryAdapter mRecyclerViewAdapter;
    // Data retrieved from db.
    protected ArrayList<PlayedTimeEntity> mEntities;

    /*********************
     *** INNER CLASSES ***
     *********************/

    public class IntegerFormatter implements IValueFormatter {

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

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The adapter provides views to RecyclerView's layout manager as user scrolls the list.
        mRecyclerViewAdapter = new HistoryAdapter(getContext());

        // Get the ViewModel that host the db live data which survives fragment lifecycle.
        mViewModel = ViewModelProviders.of(this).get(PlayedTimeViewModel.class);

        // Below call of getAllPlayedTimeRecords() returns the data in "onChanged", asynchronously,
        // when LiveData is ready. This call shall be done here in onCreate. onChanged callback is
        // not invoked if below code is defined at onCreateView.
        mViewModel.getAllPlayedTimeRecords().observe(this, new Observer<List<PlayedTimeEntity>>() {
            @Override
            // Callback to be asynchronously called by system upon 1st call to
            // getAllPlayedTimeRecords() once the LiveData return value is loaded. Then will
            // be called agiain every time the data to be returned by getAllPlayedTimeRecords()
            // is changed due to changes in room db.
            public void onChanged(@Nullable List<PlayedTimeEntity> playedTimeEntities) {

                // playedTimeEntities contains all records from db
                mEntities = (ArrayList<PlayedTimeEntity>) playedTimeEntities;
                int totalRecords = mEntities.size();
                MyLog.d(TAG, "onChanged: totalRecords=" + totalRecords);

                // Calculate the number of charts
                int chartsCnt = (int) Math.ceil(totalRecords / (double) HistoryChartData.BARS_PER_CHART);
                MyLog.d(TAG, "onChanged: chartsCnt=" + chartsCnt);

                // Build each chart's data and order to display chart with most recent data at the top.
                ArrayList<HistoryChartData> list = new ArrayList<>(chartsCnt);
                for (int i = 0; i < chartsCnt; i++) {
                    // Add at the 1st position to shift right other elements
                    list.add(0, buildChartData(i));
                }

                mRecyclerViewAdapter.setOrUpdateData(list);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        MyLog.d(TAG,"onCreateView begin");

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_history_list, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        // If layout manager has been set scroll to current list position, otherwise set it
        int scrollPosition = 0;
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        } else {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
        mRecyclerView.scrollToPosition(scrollPosition);

        // Set the adapter for RecyclerView list.
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        MyLog.d(TAG,"onCreateView end");

        return rootView;
    }

    /*
     * chartPosition: chart item in chart list i.e. 0, 1, 2, 3, etc
     */
    private HistoryChartData buildChartData(int chartPosition){

        int offset = chartPosition * HistoryChartData.BARS_PER_CHART;
        ArrayList<PlayedTimeEntity> entities = getEntitiesSlice(offset);
        // Get entities slice size
        int count = entities.size();
        MyLog.d(TAG,"buildChartData entities.size =" + count);

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        // Set color of each bar
        int colorAccent = getResources().getColor(R.color.colorAccent);
        int colorOrange = getResources().getColor(R.color.colorOrange);
        int colorTransparent = getResources().getColor(R.color.colorTransparent);
        int barsColor[] = new int[HistoryChartData.BARS_PER_CHART];
        ArrayList<Integer> barsTextColor = new ArrayList<>();

        // Get valid entries
        for (int i = 0; i < count; i++) {
            // Create bar and set text value
            MyLog.d(TAG, "buildChartData: played time ="+ entities.get(i).getPlayed());
            BarEntry entry = new BarEntry(i, entities.get(i).getPlayed());
            entries.add(entry);
            // Set bar color
            if (entities.get(i).getPlayed() <= entities.get(i).getLimit()){
                // Played time within limit
                barsColor[i] = colorAccent;
                barsTextColor.add(colorAccent);
            } else {
                // Played time exceeded limit
                barsColor[i] = colorOrange;
                barsTextColor.add(colorOrange);
            }
            // Get bar date
            dates.add(entities.get(i).getDate());
        }

        // Add dummy entries (bars) to force each bar in an incomplete charts to have the same
        // width of the bars in a complete charts, if any.
        for (int j = count; j < HistoryChartData.BARS_PER_CHART; j++){
            // Chart is incomplete, create dummy bars
            BarEntry entry = new BarEntry(j, 0);
            entries.add(entry);
            // Set transparency to 100% to hide bar text value
            barsColor[j] = colorTransparent;
            barsTextColor.add(colorTransparent);
            dates.add(HistoryChartData.EMPTY);
        }

        // Set subtitle to start and end dates of this chart
        String legend = dates.get(0) + " ... " + dates.get(count-1);
        BarDataSet d = new BarDataSet(entries, legend);

        // Set values to be displayed at the top of each bar as integers (no decimal digits)
        d.setValueFormatter(new IntegerFormatter());

        // Set text size of values at the top of each bar
        d.setValueTextSize(HistoryChartData.BAR_TEXT_SIZE);

        // Set colors
        d.setColors(barsColor);
        d.setValueTextColors(barsTextColor);
        d.setBarShadowColor(Color.rgb(203, 203, 203));

        ArrayList<IBarDataSet> sets = new ArrayList<>();
        sets.add(d);

        BarData bd = new BarData(sets);
        bd.setBarWidth(0.9f);

        HistoryChartData hd = new HistoryChartData(bd, dates);

        return hd;
    }

    /*
     * offset: number of heading entities to be skipped
     */
    private ArrayList<PlayedTimeEntity> getEntitiesSlice(int offset){

        ArrayList<PlayedTimeEntity> entities = new ArrayList<>();

        // Calculate the end of entities slice
        int end = offset + Math.min(mEntities.size() - offset, HistoryChartData.BARS_PER_CHART);

        for (int i = offset; i < end; i++) {
            entities.add(mEntities.get(i));
        }
        return entities;
    }
}
