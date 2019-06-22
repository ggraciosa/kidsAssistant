package com.example.graciosa.kidsassistant.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graciosa.kidsassistant.R;


public class HistoryFragment extends Fragment {

    /*****************
     *** CONSTANTS ***
     *****************/

    private static final int DATASET_COUNT = 60;

    /**************
     *** FIELDS ***
     **************/

    protected String[] mDataset;
    // RecyclerView list. Requests view as user scrolls in list, in an efficient way.
    protected RecyclerView mRecyclerView;

    /***************
     *** METHODS ***
     ***************/

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDataset();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

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
        mRecyclerView.setAdapter(new HistoryAdapter(mDataset));

        return rootView;
    }

    private void initDataset() {
        mDataset = new String[DATASET_COUNT];
        for (int i = 0; i < DATASET_COUNT; i++) {
            mDataset[i] = "This is element #" + i;
        }
    }
}
