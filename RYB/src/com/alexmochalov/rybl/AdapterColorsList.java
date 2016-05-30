package com.alexmochalov.rybl;

import java.util.ArrayList;

import com.mochalov.alex.rybl.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdapterColorsList  extends BaseAdapter {
	Context context;
	LayoutInflater lInflater;
	ArrayList<ColorName> objects;
	  
	AdapterColorsList(Context context, ArrayList<ColorName> obj) {
		this.context = context;
	    objects = obj;
	    lInflater = (LayoutInflater)context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 }
	
	
	@Override
	public int getCount() {
		return objects.size();
	}

	@Override
	public Object getItem(int position) {
		return objects.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
	    if (view == null) {
	      view = lInflater.inflate(R.layout.item_color, parent, false);
	    }

	    ColorName p = (ColorName)getItem(position);

	    ((TextView) view.findViewById(R.id.textViewItemColor)).setText(p.name);
	    ((ImageView) view.findViewById(R.id.imageViewItemColor)).setBackgroundColor(Var.ryb2rgb(p.pixelFloat));
	    return view;	
	  }

}
