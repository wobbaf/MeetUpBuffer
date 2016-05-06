package com.example.magda.meetupbuffer.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import com.example.magda.meetupbuffer.R;

public class PlacesAdapter extends ArrayAdapter {
    HashMap<Integer, Boolean> checked = new HashMap<Integer, Boolean>();
    ArrayList places;
    Context context;
    public PlacesAdapter(Context context, int resource, int textViewResourceId, ArrayList _places) {
        super(context, resource, textViewResourceId);
        this.context = context;
        this.places = _places;
        for (int i = 0; i < _places.size(); i++)
            checked.put(i, false);
    }

    public void toggle(int position){
        if(checked.get(position))checked.put(position, false);
        else checked.put(position, true);
        notifyDataSetChanged();
    }

    public ArrayList getCheckedItemPosition(){
        ArrayList check = new ArrayList();
        for(int i=0;i<checked.size();i++){
            if(checked.get(i))check.add(i);
        }
        return check;
    }

    public ArrayList getCheckedItems(){
        ArrayList check = new ArrayList();
        for(int i=0;i<checked.size();i++){
            if(checked.get(i))check.add(places.get(i));
        }
        return check;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if(row == null){
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = vi.inflate(R.layout.places_list_item, null);
        }

        CheckedTextView checkedTextView = (CheckedTextView)row.findViewById(R.id.placesItem);
        checkedTextView.setText(places.get(position).toString());
        return row;
    }
}