package com.example.graciosa.kidsassistant;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class TimeStepJobService extends JobService {

    final String TAG = TimeStepJobService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters params) {

        String log = "onStartJob";
        MyLog.d(TAG, log);

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        String log = "onStopJob";
        MyLog.d(TAG, log);

        return false;
    }
}
