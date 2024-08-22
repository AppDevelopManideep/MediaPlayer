package com.example.mediaplayer.folders_adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mediaplayer.R;
import com.example.mediaplayer.folder_files_from_storage.VideoFiles;
import com.example.mediaplayer.modelclass_java.MediaFiles;

import java.util.ArrayList;

public class MyVideoFolderAdapter extends BaseAdapter {

    private ArrayList<MediaFiles> mediaFiles;
    private ArrayList<String> folderPath;

    String[] items;
    private Context context;
    LayoutInflater inflater;

    public MyVideoFolderAdapter(Context context, ArrayList<String> folderPath, ArrayList<MediaFiles> mediaFiles) {
        this.context=context;
        this.folderPath=folderPath;
        this.mediaFiles=mediaFiles;//this item sent from radio class
    }

    @Override
    public int getCount() {
        return folderPath.size();
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

            convertView=inflater.inflate(R.layout.folder_musicgridview,null);
        }

        TextView folderName,folder_path,noOfFiles;
        folderName = convertView.findViewById(R.id.folderName);
        folder_path=convertView.findViewById(R.id.folderPath);
        noOfFiles=convertView.findViewById(R.id.noOfFiles);

        //setting into the textviews

        int indexPath=folderPath.get(position).lastIndexOf("/");
        String nameOfFolder=folderPath.get(position).substring(indexPath+1);
        folderName.setText(nameOfFolder);
        folder_path.setText(folderPath.get(position));
        noOfFiles.setText("5 files");

        // here we have set data to grid view based on the position of list for textview and image view
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"clicked on ",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, VideoFiles.class);
                intent.putExtra("folderName",nameOfFolder);
                context.startActivity(intent);
            }
        });

        ImageView folder_menu=convertView.findViewById(R.id.foldermusic_menu);
        folder_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMenu(view);
            }
        });
        return convertView;
    }

    private void openMenu(View view){
        PopupMenu popupMenu =new PopupMenu(context,view);
        popupMenu.inflate(R.menu.menu_list);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.refresh_folders:
                        Toast.makeText(context,"You clicked menu_item 1 ",Toast.LENGTH_LONG).show();
                        return true;

                    case R.id.share_app:
                        Toast.makeText(context,"You clicked menu_item 2 ",Toast.LENGTH_LONG).show();
                        return true;

                    default:
                        return false;

                }
            }
        });
        popupMenu.show();
    }
}

