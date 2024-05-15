package com.example.mediaplayer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


public class video extends Fragment {


    GridView gridView;
    private boolean isImageViewExpanded = false;
    Button resizeButton;
    ImageView imageView;
    LinearLayout firstLayout,secondLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video,container,false);

        //items placed in grid view
        String []itemList={"VIDEO1","VIDEO2","VIDEO3","VIDEO4","VIDEO5","VIDEO6","VIDEO7","VIDEO8"};
        gridView=view.findViewById(R.id.vedeogridview);
        firstLayout=view.findViewById((R.id.firstlayout_gridview));
        secondLayout=view.findViewById(R.id.secondlayout);
        resizeButton=view.findViewById(R.id.button);
        final LinearLayout containerLayout=view.findViewById(R.id.container);
        imageView=view.findViewById(R.id.vedeoimage);
        resizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                containerLayout.removeAllViews();

                if(isImageViewExpanded){
                    //after clicking minimizebutton
                    containerLayout.addView(firstLayout);
                    containerLayout.addView(secondLayout);
                  firstLayout.setLayoutParams(new LinearLayout.LayoutParams(
                             ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,2
                    ));
                    secondLayout.setLayoutParams(new LinearLayout.LayoutParams(
                              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1
                    ));
                    resizeButton.setBackgroundResource(R.drawable.baseline_fullscreen_24);

                }
                else{
                    //minimizebutton
                    //Expand the imageview
                    containerLayout.addView(secondLayout);
                    secondLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT
                    ));
                    resizeButton.setBackgroundResource(R.drawable.baseline_fullscreen_exit_24);
                }
                isImageViewExpanded=!isImageViewExpanded;

            }
        });
        VideoPageAdapter adapter = new VideoPageAdapter(getContext(),itemList);
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