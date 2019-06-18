package com.example.graciosa.kidsassistant.db;

import android.content.Context;

import androidx.room.Room;

public class PlayedTimeDatabaseSingleton {

    private static PlayedTimeDatabaseSingleton mInstance;
    private PlayedTimeDatabase mDb;

    private PlayedTimeDatabaseSingleton(Context context){
        mDb = Room.databaseBuilder(context, PlayedTimeDatabase.class, "kids-assistant-db").build();
    }

    public static PlayedTimeDatabaseSingleton getInstance(Context context){
        if (mInstance == null) {
            mInstance = new PlayedTimeDatabaseSingleton(context);
        }
        return mInstance;
    }

    public PlayedTimeDatabase getDatabase (){
        return mDb;
    }

}
