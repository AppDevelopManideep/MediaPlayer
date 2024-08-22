package com.example.mediaplayer.folders;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mediaplayer.R;
import com.example.mediaplayer.folders_adapters.MyMusicFolderAdapter;
import com.example.mediaplayer.modelclass_java.MediaFiles;

import java.util.ArrayList;



public class folder_music extends Fragment {

    GridView gridView;
    SwipeRefreshLayout swipeRefreshLayout;
    private static  final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE=123;
    ArrayList<MediaFiles> mediaFiles=new ArrayList<>();
    ArrayList<String> allFolderList=new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_folder_music,container,false);
        gridView=view.findViewById(R.id.gridview1);
        swipeRefreshLayout=view.findViewById(R.id.swipe_refresh_folders);
        //below is when we swipe down then it will be called
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showFolders();
                swipeRefreshLayout.setRefreshing(false);//to hide the refresh layout
            }
        });
        showFolders();
        return view;
    }



    private void showFolders(){
        mediaFiles=fetchMedia(); //this will return the folders of mp3
        MyMusicFolderAdapter adapter = new MyMusicFolderAdapter(getContext(),allFolderList,mediaFiles);
        gridView.setAdapter(adapter);

    }
    public ArrayList<MediaFiles> fetchMedia(){

        ArrayList<MediaFiles> mediaFilesArrayList=new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor=requireActivity().getContentResolver().query(uri,null,null,null,null);
        if(cursor!=null && cursor.moveToNext()){
            do{
                String id= cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String title= cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String displayName= cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                String size= cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                String duration= cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String path= cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String dateAdded= cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
                MediaFiles mediaFiles=new MediaFiles(id,title,displayName,size,duration,path,dateAdded);
                int index=path.lastIndexOf("/");
                String subString =path.substring(0,index);
                if(!allFolderList.contains(subString)){
                    allFolderList.add(subString);
                }
                mediaFilesArrayList.add(mediaFiles);
            }
            while(cursor.moveToNext());
            cursor.close();

        }
        else{
            Toast.makeText(getContext(),"cursor = null ",Toast.LENGTH_LONG).show();

        }
        return mediaFilesArrayList;
    }


}