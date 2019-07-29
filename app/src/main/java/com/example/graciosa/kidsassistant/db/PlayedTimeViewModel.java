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
    // Total number of records in PlayedTimeEntities database table.
    // System transparently updates this data every time db is changed so that the result of the
    // query associated with this data is changed.
    private LiveData<Integer> mCount;
    // All records of PlayedTimeEntities database table.
    // System transparently updates this data every time db is changed so that the result of the
    // query associated with this data is changed.
    private LiveData<List<PlayedTimeEntity>> mRecords;

    /*********************
     *** INNER CLASSES ***
     *********************/

    private class loadDataFromRoomDb extends AsyncTask<Void, Void, Void> {

        // Executed in background thread
        @Override
        protected Void doInBackground(Void... voids) {

            MyLog.d(TAG, "loadDataFromRoomDb: doInBackground");

            // Queries
            mCount = mDao.countAll();
            // TODO: below returns null with LiveData and expected values if Integer. WHY???
            MyLog.d(TAG,"async task: mCount = " + mCount.getValue());
            mRecords = mDao.getAll();

            return null;
        }
    }

    /***************
     *** METHODS ***
     ***************/

    /*
     * It seems this constructor is called by system through ViewModelProviders class, and that
     * system takes care of accessing room db through in a worker thread.
     */
    public PlayedTimeViewModel(Application app){

        super(app);

        MyLog.d(TAG,"Loading room db data");

        // Get room database and perform all queries to save all data here in ViewModel object.
        mDb = PlayedTimeDatabaseSingleton.getInstance(app).getDatabase();
        mDao = mDb.playedTimeDao();

        // Need to access room db out of main thread.
        try {
            new loadDataFromRoomDb().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public LiveData<Integer> getPlayedTimeRecordsCount(){
        return mCount;
    }

    public LiveData<List<PlayedTimeEntity>> getAllPlayedTimeRecords(){
        return mRecords;
    }
}
