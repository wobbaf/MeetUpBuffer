package com.example.magda.meetupbuffer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.magda.meetupbuffer.R;
import com.example.magda.meetupbuffer.async.DownloadImageTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FriendListAdapter extends ArrayAdapter {

    Context context;
    int layoutResourceId;
    ArrayList<JSONObject> friendsListData;

    public FriendListAdapter(Context context, int layoutResourceId, ArrayList<JSONObject> friendsListData) {
        super(context,layoutResourceId,friendsListData);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.friendsListData = friendsListData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        FriendHolder holder = null;
        if(row == null)
        {LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new FriendHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.icon);
            holder.txtTitle = (TextView)row.findViewById(R.id.firstLine);

            row.setTag(holder);
        }
        else
        {
            holder = (FriendHolder)row.getTag();
        }



        JSONObject friend = friendsListData.get(position);
        try {
            /*String profilePicUrl = friend.getString("url");
            URL fb_url = new URL(profilePicUrl);//small | noraml | large
            HttpsURLConnection conn1 = (HttpsURLConnection) fb_url.openConnection();
            HttpsURLConnection.setFollowRedirects(true);
            conn1.setInstanceFollowRedirects(true);
            Bitmap fb_img = BitmapFactory.decodeStream(conn1.getInputStream());*/
            holder.txtTitle.setText(friend.getString("name"));
            //holder.imgIcon.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
            //new DownloadImageTask(holder.imgIcon).execute("https://graph.facebook.com/" + friend.getString("id") + "/picture?type=small");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return row;
    }




    static class FriendHolder
    {
        ImageView imgIcon;
        TextView txtTitle;
    }
}