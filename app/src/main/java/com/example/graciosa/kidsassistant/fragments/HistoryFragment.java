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
    private static final int LIST_SIZE = 20;
    private static final float BAR_TEXT_SIZE = 10f;
    private static final int BARS_PER_CHART = 12;

    /**************
     *** FIELDS ***
     **************/

    protected ArrayList<BarData> mList;
    // RecyclerView list. Requests view as user scrolls in list, in an efficient way.
    protected RecyclerView mRecyclerView;
    protected BarChartsDataBuilderAsyncTask asyncTask;

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
    private class BarChartsDataBuilderAsyncTask extends AsyncTask<Void, Void, ArrayList<BarData>>{

        // Executed in background thread
        @Override
        protected ArrayList<BarData> doInBackground(Void... voids){

            // Get total number of records in database
            PlayedTimeDatabase db =
                    PlayedTimeDatabaseSingleton.getInstance(getContext()).getDatabase();
            PlayedTimeDao dao = db.playedTimeDao();
            int totalRecords = dao.countAll();

            // Calculate the number of charts
            int chartsCnt = (int) Math.ceil(totalRecords / BARS_PER_CHART);

            // Build each chart's data
            ArrayList<BarData> list = new ArrayList<>();
            // TODO: replace LIST_SIZE with chartsCnt
            for (int i = 0; i < LIST_SIZE; i++) {
                list.add(buildBarData(i));
            }

            // Return chart list to system
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
        try {
            mList = new BarChartsDataBuilderAsyncTask().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        return rootView;
    }

    private void initList(int cnt) {
        mList = new ArrayList<>();
        for (int i = 0; i < cnt; i++) {
            mList.add(buildBarData(i));
        }
    }

    /*
     * listItem: chart item in chart list i.e. 1, 2, 3, etc
     */
    private BarData buildBarData(int listItem){

        ArrayList<PlayedTimeEntity> entities = getEntities(listItem);
        ArrayList<BarEntry> entries = new ArrayList<>();

        // Set value of each bar
        for (int i = 0; i < BARS_PER_CHART; i++) {
            BarEntry entry = new BarEntry(i, entities.get(i).getPlayed());
            entries.add(entry);
        }

        // TODO: replace chart label with beginning and end date
        BarDataSet d = new BarDataSet(entries, "TODO: give me a label");

        // Set values to be displayed at the top of each bar as integers (no decimal digits)
        d.setValueFormatter(new IntegerFormatter());

        // Set text size of values at the top of each bar
        d.setValueTextSize(BAR_TEXT_SIZE);

        // Set color of each bar
        int colorAccent = getResources().getColor(R.color.colorAccent);
        int colorOrange = getResources().getColor(R.color.colorOrange);
        int colors[] = new int[BARS_PER_CHART];
        for (int i = 0; i < BARS_PER_CHART; i++){
            if (entities.get(i).getPlayed() <= entities.get(i).getLimit()){
                // Played time within limit
                colors[i] = colorAccent;
            } else {
                // Played time exceeded limit
                colors[i] = colorOrange;
            }
        }
        d.setColors(colors);
        d.setBarShadowColor(Color.rgb(203, 203, 203));

        ArrayList<IBarDataSet> sets = new ArrayList<>();
        sets.add(d);

        BarData bd = new BarData(sets);
        bd.setBarWidth(0.9f);
        return bd;

    }


    /* TODO: RETRIEVE ACTUAL ENTITIES FROM DATABASE
     * offset: number of heading BARS_PER_CHART entities to be skipped
     */
    private ArrayList<PlayedTimeEntity> getEntities(int offset){

        ArrayList<PlayedTimeEntity> entities = new ArrayList<>();

        for (int i = 0; i < BARS_PER_CHART; i++) {
            PlayedTimeEntity entity = new PlayedTimeEntity();
            entity.setDate("2019-06-23");
            entity.setPlayed((int) (Math.random() * 100));
            entity.setLimit(50);
            entities.add(entity);
        }
        return entities;
    }

    /**
     * Generates a random ChartData object with just one DataSet
     * @return Bar data
     */
    private BarData generateData(int cnt) {

        ArrayList<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < BARS_PER_CHART; i++) {
            BarEntry entry = new BarEntry(i, (float) (Math.random() * 70) + 30);
            entries.add(entry);
        }

        // TODO: replace chart label with beginning and end date
        BarDataSet d = new BarDataSet(entries, "New DataSet " + cnt);
        // Display values at the top of each bar as integers (no decimal digits)
        d.setValueFormatter(new IntegerFormatter());
        // Set size of values at the top of each bar
        d.setValueTextSize(BAR_TEXT_SIZE);
        // Set color of each bar
        int colorAccent = getResources().getColor(R.color.colorAccent);
        int colorOrange = getResources().getColor(R.color.colorOrange);
        int[] colors = {colorAccent, colorOrange, colorAccent, colorOrange,
                colorAccent, colorOrange, colorAccent, colorOrange,
                colorAccent, colorOrange, colorAccent, colorOrange,};
        d.setColors(colors);
        d.setBarShadowColor(Color.rgb(203, 203, 203));

        ArrayList<IBarDataSet> sets = new ArrayList<>();
        sets.add(d);

        BarData cd = new BarData(sets);
        cd.setBarWidth(0.9f);
        return cd;
    }

}
