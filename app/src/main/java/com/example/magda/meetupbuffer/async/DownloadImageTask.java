package com.example.magda.meetupbuffer.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.example.magda.meetupbuffer.activities.MainActivity;

import java.io.InputStream;

/**
 * Created by magda on 04.05.16.
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {


    private Bitmap bmImage;
    private Long key;

    public DownloadImageTask(Bitmap bmImage, Long key) {
        this.bmImage = bmImage;
        this.key = key;
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
        MainActivity.friendsDictionaryImg.put(key,bmImage);

        //close
    }
}