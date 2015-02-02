package com.ugopiemontese.openband.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ugopiemontese.openband.R;

import java.util.List;

public class MiBandAdapter extends ArrayAdapter<MiBand> {

    public MiBandAdapter(Context context, int textViewResourceId, List<MiBand> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.single_miband_element, null);
        }

        MiBand c = getItem(position);

        if (c != null) {

            TextView name = (TextView) convertView.findViewById(R.id.name);
            name.setText(c.getName());

            TextView address = (TextView) convertView.findViewById(R.id.address);
            address.setText(c.getAddress());

        }

        return convertView;
    }

}