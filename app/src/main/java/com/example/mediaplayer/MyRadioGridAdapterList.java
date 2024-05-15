package com.example.mediaplayer;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MyRadioGridAdapterList extends BaseAdapter {

    String[] items;
    private Context context;
    private LayoutInflater inflater;
    private RadioItemListener radioItemListener = null;

    public MyRadioGridAdapterList(Context context, String[] itemList) {
        this.context = context;
        this.items = itemList;//this item sent from radio class
        /*if (radioItemListener == null) {
            radioItemListener = (MainActivity) ;
        }*/
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
        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {

            convertView = inflater.inflate(R.layout.radiogridviewsongs, null);
        }
        // here we have set data to grid view based on the position of list for textview and image view
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(context, "clicked on " + items[position], Toast.LENGTH_SHORT).show();
            }
        });
        TextView textView = convertView.findViewById(R.id.folderName);
        textView.setText(items[position]);
        ImageButton menuButton = convertView.findViewById(R.id.menu);
        menuButton.setTag("menu_button" + position);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openMenu(view);
            }
        });
        return convertView;
    }

}

interface RadioItemListener {
    void onitemclick();
}
