package com.example.graciosa.kidsassistant.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(version = 1, entities = {PlayedTimeEntity.class})
public abstract class PlayedTimeDatabase extends RoomDatabase {
    public abstract PlayedTimeDao playedTimeDao();
}
