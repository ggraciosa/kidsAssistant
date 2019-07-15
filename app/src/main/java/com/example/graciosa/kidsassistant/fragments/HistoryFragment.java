package com.example.graciosa.kidsassistant.fragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graciosa.kidsassistant.MyLog;
import com.example.graciosa.kidsassistant.R;
import com.example.graciosa.kidsassistant.db.PlayedTimeDao;
import com.example.graciosa.kidsassistant.db.PlayedTimeDatabase;
import com.example.graciosa.kidsassistant.db.PlayedTimeDatabaseSingleton;
import com.example.graciosa.kidsassistant.db.PlayedTimeEntity;
import com.example.graciosa.kidsassistant.receivers.TimeStepReceiver;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class HistoryFragment extends Fragment {

    /*****************
     *** CONSTANTS ***
     *****************/

    final String TAG = TimeStepReceiver.class.getSimpleName();

    /**************
     *** FIELDS ***
     **************/

    protected ArrayList<HistoryChartData> mList;
    // RecyclerView list. Requests view as user scrolls in list, in an efficient way.
    protected RecyclerView mRecyclerView;
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

    /*
     * Get data from database and build charts data
     */
    private class BarChartsDataBuilderAsyncTask extends AsyncTask<Void, Void, ArrayList<HistoryChartData>>{

        // Executed in background thread
        @Override
        protected ArrayList<HistoryChartData> doInBackground(Void... voids){

            MyLog.d(TAG,"doInBackground begin");

            // Get total number of records in database
            PlayedTimeDatabase db =
                    PlayedTimeDatabaseSingleton.getInstance(getContext()).getDatabase();
            PlayedTimeDao dao = db.playedTimeDao();
            int totalRecords = dao.countAll();
            MyLog.d(TAG,"doInBackground: totalRecords=" + totalRecords);

            // Get all records from database
            mEntities = (ArrayList<PlayedTimeEntity>) dao.getAll();

            // Calculate the number of charts
            int chartsCnt = (int) Math.ceil(totalRecords / (double) HistoryChartData.BARS_PER_CHART);
            MyLog.d(TAG,"doInBackground: chartsCnt=" + chartsCnt);

            // Build each chart's data and order to display chart with most recent data at the top.
            ArrayList<HistoryChartData> list = new ArrayList<>(chartsCnt);
            for (int i = 0; i < chartsCnt; i++) {
                // Add at the 1st position to shift right other elements
                list.add(0, buildChartData(i));
            }

            // Return chart list to system
            MyLog.d(TAG,"doInBackground end");

            return list;
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
        //initList(LIST_SIZE);
        // Execute async task to load data from database, wait until it is done
        // TODO: we may not block here and check async task status in onCreateView but how to wait there if not done?
        MyLog.d(TAG,"onCreate begin");
        try {
            mList = new BarChartsDataBuilderAsyncTask().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MyLog.d(TAG,"onCreate end");
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
        // The adapter provides views to RecyclerView's layout manager as user scrolls the list.
        mRecyclerView.setAdapter(new HistoryAdapter(mList, getContext()));

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
