package com.example.mediaplayer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class radio extends Fragment {


    GridView gridView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_radio,container,false);
        //items placed in grid view
        String []itemList={"Radio1","Radio2","Radio3","Radio4","Radio5","Radio6","Radio7","Radio8"};//here create this to place textview in gridview
        String []image ={};//here create a image list or array to place in grid view
        gridView=view.findViewById(R.id.gridview);
        MyRadioGridAdapterList adapter = new MyRadioGridAdapterList(getContext(),itemList);
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