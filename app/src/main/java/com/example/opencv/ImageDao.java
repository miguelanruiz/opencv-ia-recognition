package com.example.opencv;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.lifecycle.LiveData;

import java.util.List;
@Dao
public interface ImageDao {
    @Query("SELECT * FROM images")
    LiveData<List<ImageEntity>> getAllImages();

    @Insert
    void insertImage(ImageEntity image);

    @Delete
    void deleteImage(ImageEntity image);

    @Query("DELETE FROM images")
    void deleteAllImages();

    @Query("SELECT COUNT(*) FROM images")
    int getCount();

    @Query("SELECT * FROM images ORDER BY id LIMIT 1")
    ImageEntity getOldestImage();
}
