package com.example.graciosa.kidsassistant.fragments;

import android.content.Context;
import android.graphics.Color;
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
import com.github.mikephil.charting.data.BarData;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    /*****************
     *** CONSTANTS ***
     *****************/

    private static final String TAG = HistoryAdapter.class.getSimpleName();

    /**************
     *** FIELDS ***
     **************/

    private Context mContext;
    private ArrayList<BarData> mList;

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

    public HistoryAdapter(ArrayList<BarData> list, Context context) {

        mList = list;
        mContext = context;
    }

    /***************
     *** METHODS ***
     ***************/

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
        BarData data = mList.get(position);

        // Apply styling
        //data.setValueTypeface(tfLight);
        //data.setValueTextColor(Color.CYAN);
        data.setValueTextColor(mContext.getResources().getColor(R.color.colorAccent, null));

        // Get content container
        BarChart holder = viewHolder.getBarChartView();

        // Apply styling
        holder.getDescription().setEnabled(false);
        holder.setDrawGridBackground(false);

        XAxis xAxis = holder.getXAxis();

        int axisLineColor = xAxis.getAxisLineColor();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setTypeface(tfLight);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(axisLineColor);

        YAxis leftAxis = holder.getAxisLeft();
        //leftAxis.setTypeface(tfLight);
        leftAxis.setLabelCount(6, false);
        leftAxis.setSpaceTop(15f);
        leftAxis.setTextColor(axisLineColor);

        YAxis rightAxis = holder.getAxisRight();
        //rightAxis.setTypeface(tfLight);
        rightAxis.setLabelCount(6, false);
        rightAxis.setSpaceTop(15f);
        rightAxis.setTextColor(axisLineColor);

        // Set data
        holder.setData(data);
        holder.setFitBars(true);

        // Refresh the chart
        // holder.chart.invalidate();
        holder.animateY(700);
    }

    // Return the dataset size (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mList.size();
    }
}