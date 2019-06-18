package com.example.graciosa.kidsassistant.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PlayedTimeEntity {

    @PrimaryKey @NonNull
    private String date;
    private int played;
    private int limit;

    public String getDate() {return this.date;}
    public void setDate(final String date) {this.date = date;}

    public int getPlayed() {return this.played;}
    public void setPlayed(final int played) {this.played = played;}

    public int getLimit() {return this.limit;}
    public void setLimit(final int limit) {this.limit = limit;}
}
