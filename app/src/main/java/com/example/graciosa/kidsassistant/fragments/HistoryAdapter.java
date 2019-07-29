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
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_history_list_item, viewGroup, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        MyLog.d(TAG, "Bar chart " + position + " set.");

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

    // Receive data and if data is being replaced notify view to get refreshed.
    public void setOrUpdateData(ArrayList<HistoryChartData> list){
        boolean requestViewUpdate = mList == null;
        mList = list;
        if (requestViewUpdate){
            // Data was replaced, update UI.
            notifyDataSetChanged();
        }
    }

    // Return the dataset size (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mList.size();
    }
}
