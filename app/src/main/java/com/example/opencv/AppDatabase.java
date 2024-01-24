package com.example.opencv;

import androidx.room.Database;
import androidx.room.RoomDatabase;
@Database(entities = {ImageEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ImageDao imageDao();
}