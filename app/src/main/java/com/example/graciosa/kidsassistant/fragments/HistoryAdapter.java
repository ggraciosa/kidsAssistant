package com.example.graciosa.kidsassistant.fragments;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graciosa.kidsassistant.MyLog;
import com.example.graciosa.kidsassistant.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    /*****************
     *** CONSTANTS ***
     *****************/

    private static final String TAG = HistoryAdapter.class.getSimpleName();

    private static final float AXIS_LABELS_TEXT_SIZE = 11f;

    private static final int ITEM_VIEW_TYPE_TEXT = 0;
    private static final int ITEM_VIEW_TYPE_DATA = 1;

    /**************
     *** FIELDS ***
     **************/

    private Context mContext;
    // List of charts, each element is a chart containing its bars and associated dates
    private ArrayList<HistoryChartData> mList;

    /*********************
     *** INNER CLASSES ***
     *********************/

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final BarChart barChartView;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyLog.d(TAG, "Bar chart " + getAdapterPosition() + " clicked.");
                }
            });
            barChartView = (BarChart) v.findViewById(R.id.list_item_bar_chart);
        }

        public BarChart getBarChartView() {
            return barChartView;
        }
    }

    /***************
     *** METHODS ***
     ***************/

    public HistoryAdapter(Context context) {

        mContext = context;

        // Need a non null object to provide to the system in getItemCount, while wait for an
        // update with valid data from HistoryFragment, which on its turn will receive
        // asynchronously from system via LiveData.
        mList = new ArrayList<>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        MyLog.d(TAG, "onCreateViewHolder");

        int res = 0;
        switch (viewType){
            case ITEM_VIEW_TYPE_TEXT:
                res = R.layout.fragment_history_list_empty;
                break;
            case ITEM_VIEW_TYPE_DATA:
                res = R.layout.fragment_history_list_item;
                break;
            default:
                MyLog.e(TAG, "onCreateViewHolder: this point should never be reached.");
        }

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(res, viewGroup, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        int viewType = viewHolder.getItemViewType();

        MyLog.d(TAG, "onBindViewHolder: view type= " + viewType + "; position=" + position);

        if (viewType == ITEM_VIEW_TYPE_TEXT){
            // Nothing to do since view content is in R.layout.fragment_history_list_empty.
            return;
        }

        // Get chart content to be displayed
        HistoryChartData data = mList.get(position);

        // Get content container
        BarChart holder = viewHolder.getBarChartView();

        // Apply styling
        // Remove text with chart in right bottom corner
        holder.getDescription().setEnabled(false);
        holder.setDrawGridBackground(false);

        XAxis xAxis = holder.getXAxis();

        int axisLineColor = xAxis.getAxisLineColor();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setTypeface(tfLight);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(axisLineColor);
        xAxis.setTextSize(AXIS_LABELS_TEXT_SIZE);
        // Set the day of the month as the x axis label for each bar from "YYYY-MM-DD"
        xAxis.setLabelCount(HistoryChartData.BARS_PER_CHART);
        ArrayList<String> dates = mList.get(position).getBarsDate();
        ArrayList<String> xLabels = new ArrayList<>();
        for (int i=0; i<12; i++){
            if (dates.get(i).equals(HistoryChartData.EMPTY)){
                // Dummy date
                xLabels.add("");
            } else {
                // Valid date, extract the day
                xLabels.add(dates.get(i).substring(8));
            }
        }
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));

        YAxis leftAxis = holder.getAxisLeft();
        //leftAxis.setTypeface(tfLight);
        leftAxis.setLabelCount(6, false);
        leftAxis.setSpaceTop(15f);
        leftAxis.setTextColor(axisLineColor);
        leftAxis.setTextSize(AXIS_LABELS_TEXT_SIZE);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = holder.getAxisRight();
        //rightAxis.setTypeface(tfLight);
        rightAxis.setLabelCount(6, false);
        rightAxis.setSpaceTop(15f);
        rightAxis.setTextColor(axisLineColor);
        rightAxis.setTextSize(AXIS_LABELS_TEXT_SIZE);
        rightAxis.setAxisMinimum(0f);

        // Legend
        holder.getLegend().setTextColor(axisLineColor);
        holder.getLegend().setTextSize(AXIS_LABELS_TEXT_SIZE);
        // Remove legend icons
        // List<LegendEntry> legendEntryList = new LinkedList<>();
        // LegendEntry legendEntry = new LegendEntry("LEGEND", null, NaN, NaN, null, axisLineColor);
        // legendEntryList.add(legendEntry);
        // holder.getLegend().setCustom(legendEntryList);
        // holder.getLegend().setCustom(new LinkedList<LegendEntry>());
        // holder.getLegend().setEnabled(false);

        // Set data
        holder.setData(data.getBarsData());
        holder.setFitBars(true);

        // Refresh the chart
        // holder.chart.invalidate();
        holder.animateY(700);
    }

    // Receive new data and notify view to refresh its data with the new data.
    public void setOrUpdateData(ArrayList<HistoryChartData> list){
        mList = list;
        notifyDataSetChanged();
    }

    // Invoked by the system just before onCreateViewHolder to get the type of view.
    @Override
    public int getItemViewType(int position){

         int viewType;

        if (mList == null || mList.size() == 0){
            // No data, e.g. user has not yed switch on time computation in app settings.
            // Inform user that data will be displayed when available.
            viewType = ITEM_VIEW_TYPE_TEXT;
        } else {
            // Data already available, display the data charts.
            viewType = ITEM_VIEW_TYPE_DATA;
        }
        MyLog.d(TAG, "getItemViewType: viewType=" + viewType);
        return viewType;
    }

    // Invoked by the system (layout manager) to get number of items to be displayed.
    @Override
    public int getItemCount() {
        if (mList.size() == 0){
            // No data, e.g. user has not yed switch on time computation in app settings.
            // Display the text item informing user that data will be displayed when available.
            return 1;
        } else {
            return mList.size();
        }
    }
}
