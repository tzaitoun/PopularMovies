package com.zaitoun.talat.popularmovies.data;


import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/* Author: Brijesh Thakur @stackoverflow */
public class PopularMoviesImageInternalStorage {

    /* This is where we store all of our movie images */
    public static final String IMAGE_DIRECTORY = "image_directory";

    public static final String IMAGE_FORMAT = ".jpg";

    /* Saves an image to internal storage */
    public static String saveToInternalStorage(Context context, Bitmap bitmapImage, String movieId) {

        ContextWrapper cw = new ContextWrapper(context);

        /* This will retrieve the directory or create it if its the first time */
        File directory = cw.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE);

        /* This will create the image file: <movieId>.jpg this name will always be unique since the id is unique */
        File imageFile = new File(directory, movieId + IMAGE_FORMAT);

        /* Write the image data to the file */
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);

            /* Use the compress method on the BitMap object to write image to the OutputStream */
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* returns the full path of where the movie image is stored */
        return imageFile.getAbsolutePath();
    }

    /* Loads an image from internal storage, you have to specify the image path */
    public static Bitmap loadFromInternalStorage(String imagePath) {

        Bitmap bitmap = null;

        try {
            File imageFile = new File(imagePath);
            bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /* Delete an image from internal storage */
    public static boolean deleteFromInternalStorage(String imagePath) {

        File deleteFile = new File(imagePath);
        return deleteFile.delete();
    }
}
