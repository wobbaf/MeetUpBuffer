package com.example.magda.meetupbuffer.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

/**
 * Created by Piotr on 25.06.2016.
 */
public class SharedPreferencesUtil {

    public static boolean saveArray(ArrayList sKey,Context context,String name)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor mEdit1 = sp.edit();
    /* sKey is an array */
        mEdit1.putInt(name +"_size", sKey.size());

        for(int i=0;i<sKey.size();i++)
        {
            mEdit1.remove(name +"_" + i);
            mEdit1.putString(name.concat("_") + i, sKey.get(i).toString());
        }

        return mEdit1.commit();
    }


    public static void loadArray(ArrayList sKey,Context mContext,String name)
    {
        try {
            SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(mContext);
            sKey.clear();
            int size = mSharedPreference1.getInt(name + "_size", 0);

            for (int i = 0; i < size; i++) {
                sKey.add(mSharedPreference1.getString(name + "_" + i, null));
            }
        }
        catch (Exception exp) {
            return;
        }

    }

}
