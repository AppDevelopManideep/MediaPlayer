package com.example.mediaplayer.folder_files_from_storage;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mediaplayer.R;
import com.example.mediaplayer.folder_files_adapter.MusicFilesAdapter;
import com.example.mediaplayer.modelclass_java.MediaFiles;

import java.util.ArrayList;

public class MusicFiles extends AppCompatActivity {

    GridView gridView;
    String folder_name;
    private ArrayList<MediaFiles> MusicFilesArrayList = new ArrayList<>();
    MusicFilesAdapter musicFilesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicfiles);

        gridView=findViewById(R.id.music_gridview);
        folder_name=getIntent().getStringExtra("folderName");
       showMusicFiles(folder_name);
    }
    private void showMusicFiles(String folder_name){
        MusicFilesArrayList=fetchMedia(folder_name);
        musicFilesAdapter=new MusicFilesAdapter(this,MusicFilesArrayList);
        gridView.setAdapter(musicFilesAdapter);
    }
    private ArrayList<MediaFiles> fetchMedia(String foldername){
        ArrayList<MediaFiles> musicfiles = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection=MediaStore.Audio.Media.DATA+ " like?";
        String[] selectionArg= new String[]{"%"+foldername+"%"};

        Cursor cursor = getContentResolver().query(uri,null,selection,selectionArg,null);
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

                MusicFilesArrayList.add(mediaFiles);
            }
            while(cursor.moveToNext());
            cursor.close();

        }
        else{
            Toast.makeText(this,"You clicked menu_item 1 ",Toast.LENGTH_LONG).show();

        }
        return MusicFilesArrayList;
    }
}