package com.example.vi00064.bluetoothbeacons;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AdapterBeacon extends ArrayAdapter<ArrayList<HashMap<String,String>>> {

    ArrayList<HashMap<String, String>> fileNeedList;

    public AdapterBeacon(Context context, int resource, ArrayList<HashMap<String, String>> objects) {
        super(context, resource, Collections.singletonList(objects));
        this.fileNeedList = new ArrayList<>();
        fileNeedList.addAll(objects);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_beacons, parent, false);
            holder = new ViewHolder();
            holder.beaconId = (TextView) convertView.findViewById(R.id.beaconId);
            holder.powervalue = (TextView) convertView.findViewById(R.id.powervalue);
            holder.beaconName = (TextView) convertView.findViewById(R.id.beaconName);
            holder.tv_distance = (TextView) convertView.findViewById(R.id.tv_distance) ;
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        HashMap<String,String> beacon = fileNeedList.get(position);
        holder.powervalue.setText(beacon.get("power"));
        holder.beaconName.setText(beacon.get("name"));
        holder.beaconId.setText(beacon.get("uuid"));
        holder.tv_distance.setText(beacon.get("distance"));

        return convertView;
    }

    @Override
    public int getCount() {
        return fileNeedList.size();
    }

    private class ViewHolder
    {
        TextView beaconId,powervalue,beaconName, tv_distance;

    }

}
