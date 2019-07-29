package com.example.graciosa.kidsassistant.db;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlayedTimeDao {

    @Query("SELECT COUNT(*) FROM PLAYEDTIMEENTITY WHERE date = :date")
    // Get the number of records with with column 'date' equal to parameter 'date'
    int countByDate(String date);

    @Query("SELECT COUNT(*) FROM PLAYEDTIMEENTITY")
    // Get the total number of records
    LiveData<Integer> countAll();

    @Query("SELECT * FROM PLAYEDTIMEENTITY WHERE played > :threshold")
    // Get the number of records for which the played time is greater than 'threshold'.
    // threshold: a number of minutes played
    int countPlayedTimeGreaterThan(int threshold);

    @Query("SELECT * FROM PLAYEDTIMEENTITY ORDER BY `date` ASC")
    // Get all records, oldest first
    LiveData<List<PlayedTimeEntity>> getAll();

    @Query("SELECT * FROM PLAYEDTIMEENTITY WHERE played > `limit`")
    // Get the records where played time exceeded the limited for the day
    List<PlayedTimeEntity> getTimePlayedExceededLimit();

    @Query("DELETE FROM PLAYEDTIMEENTITY")
    void clear();

    @Insert
    void insert(PlayedTimeEntity playedTime);

    @Update
    void update(PlayedTimeEntity playedTime);

    @Delete
    void delete(PlayedTimeEntity playedTime);
}
