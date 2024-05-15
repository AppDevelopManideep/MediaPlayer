package com.example.mediaplayer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;


public class usb extends Fragment {


    GridView gridView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_usb,container,false);
        //items placed in grid view
        String []itemList={"USB1","USB2","USB3","USB4","USB5","USB6","USB7","USB8"};
        gridView=view.findViewById(R.id.gridview);
        UsbPageAdapter adapter = new UsbPageAdapter(getContext(),itemList);
        gridView.setAdapter(adapter);
        // shows which item is clicked
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //write code here after clicking item in grid view what it has to do
                Toast.makeText(getContext(),"You clicked on "+itemList[position],Toast.LENGTH_LONG).show();
            }
        });
        return view;
    }
    }