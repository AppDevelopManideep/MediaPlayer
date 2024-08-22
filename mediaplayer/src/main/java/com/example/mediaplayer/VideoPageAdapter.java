package com.example.mediaplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class VideoPageAdapter extends BaseAdapter {
    String[] items;
    private Context context;
    LayoutInflater inflater;

    public VideoPageAdapter(Context context, String[] itemList) {
        this.context=context;
        this.items=itemList;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(inflater==null){
            inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if(convertView==null){
            //LayoutInflater inflater=LayoutInflater.from(context);
            convertView=inflater.inflate(R.layout.videogridviewlayout,null);
        }
        // here we have set data to grid view
        TextView textView = convertView.findViewById(R.id.itemvedeo_name);
        textView.setText(items[position]);
        return convertView;
    }
}
