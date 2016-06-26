package com.example.magda.meetupbuffer.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by magda on 04.05.16.
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {


    private Bitmap bmImage;

    public DownloadImageTask(Bitmap bmImage) {
        this.bmImage = bmImage;
    }

    protected void onPreExecute() {
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", "image download error");
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        //set image of your imageview
        bmImage = result;
        //close
    }
}