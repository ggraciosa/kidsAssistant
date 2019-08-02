package com.example.graciosa.kidsassistant.db;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.graciosa.kidsassistant.MyLog;
import com.example.graciosa.kidsassistant.db.PlayedTimeDao;
import com.example.graciosa.kidsassistant.db.PlayedTimeDatabase;
import com.example.graciosa.kidsassistant.db.PlayedTimeDatabaseSingleton;
import com.example.graciosa.kidsassistant.db.PlayedTimeEntity;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class PlayedTimeViewModel extends AndroidViewModel {

    /*****************
     *** CONSTANTS ***
     *****************/

    final String TAG = PlayedTimeViewModel.class.getSimpleName();

    /**************
     *** FIELDS ***
     **************/

    private PlayedTimeDatabase mDb;
    private PlayedTimeDao mDao;
    // All records of PlayedTimeEntities database table.
    // System transparently updates this data every time db is changed so that the result of the
    // query associated with this data is changed.
    private LiveData<List<PlayedTimeEntity>> mRecords;

    /***************
     *** METHODS ***
     ***************/

    /*
     * Called by system through ViewModelProviders class.
     * System takes care of accessing room db in a worker thread.
     */
    public PlayedTimeViewModel(Application app){

        super(app);

        MyLog.d(TAG,"Loading room db");

        // Get room database and perform all queries to save all data here in ViewModel object.
        mDb = PlayedTimeDatabaseSingleton.getInstance(app).getDatabase();
        mDao = mDb.playedTimeDao();
        mRecords = mDao.getAll();

    }

    public LiveData<List<PlayedTimeEntity>> getAllPlayedTimeRecords(){
        return mRecords;
    }
}
