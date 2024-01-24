package com.example.opencv;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;

public class GalleryViewModel extends ViewModel {
    private final AppDatabase database;
    public static final int MAX_PHOTOS = 12;

    public GalleryViewModel(AppDatabase database) {
        this.database = database;
    }

    public LiveData<List<ImageEntity>> getAllImages() {
        return database.imageDao().getAllImages();
    }

    public void insertImage(String imagePath) {
        Executors.newSingleThreadExecutor().execute(() -> {
            int count = database.imageDao().getCount();
            if (count >= MAX_PHOTOS) {
                // If count is 12 or more, delete the oldest one before inserting a new one
                ImageEntity oldestImage = database.imageDao().getOldestImage();
                if (oldestImage != null) {
                    database.imageDao().deleteImage(oldestImage);
                }
            }
            ImageEntity image = new ImageEntity();
            image.setImagePath(imagePath);
            database.imageDao().insertImage(image);
        });
    }

    public void deleteAllImages() {
        Executors.newSingleThreadExecutor().execute(() -> database.imageDao().deleteAllImages());
    }

    public boolean verifyImagePathExists(String imagePath) {
        File file = new File(imagePath);
        return file.exists();
    }

    public int getCount() {
        return database.imageDao().getCount();
    }
}
