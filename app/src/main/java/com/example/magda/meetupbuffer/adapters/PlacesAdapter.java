package com.example.magda.meetupbuffer.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.TextView;

import com.example.magda.meetupbuffer.R;

public class PlacesAdapter extends ArrayAdapter {
    Context context;
    int layoutResourceId;
    ArrayList<String> places;

    public PlacesAdapter(Context context, int layoutResourceId, ArrayList<String> _places) {
        super(context,layoutResourceId,_places);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.places = _places;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PlacesHolder holder = null;
        if(row == null)
        {LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new PlacesHolder();
            holder.txtTitle = (TextView)row.findViewById(R.id.placesItem);

            row.setTag(holder);
        }
        else
        {
            holder = (PlacesHolder)row.getTag();
        }

        holder.txtTitle.setText(places.get(position));

        return row;
    }




    static class PlacesHolder
    {
        TextView txtTitle;
    }
}