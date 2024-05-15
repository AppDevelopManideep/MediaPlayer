package com.example.mediaplayer.folder_files_adapter;



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

import com.bumptech.glide.Glide;
import com.example.mediaplayer.player_activity.MusicPlayerActivity;
import com.example.mediaplayer.modelclass_java.MyMediaPlayer;
import com.example.mediaplayer.R;
import com.example.mediaplayer.modelclass_java.MediaFiles;
import com.example.mediaplayer.player_activity.VideoPlayerActivity;

import java.io.File;
import java.util.ArrayList;

public class VideoFilesAdapter extends BaseAdapter {

    private ArrayList<MediaFiles> fileslist;

    private Context context;
    LayoutInflater inflater;

    public VideoFilesAdapter(Context context, ArrayList<MediaFiles> videofiles) {
        this.context=context;
        this.fileslist =videofiles;//this item sent from radio class
    }

    @Override
    public int getCount() {
        return fileslist.size();
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

            convertView=inflater.inflate(R.layout.music_item,null);
        }

        ImageView thumbnail,menu_more;
        TextView musicName,musicSize,musicDuration;
        thumbnail = convertView.findViewById(R.id.thumbnail);
        menu_more = convertView.findViewById(R.id.music_menu);
        musicName = convertView.findViewById(R.id.music_name);
        musicSize = convertView.findViewById(R.id.music_size);
        musicDuration = convertView.findViewById(R.id.music_duration);



        //setting into the textviews


        musicName.setText(fileslist.get(position).getDisplayname());
        String size= fileslist.get(position).getSize();
        musicSize.setText(android.text.format.Formatter.formatFileSize(context,
                Long.parseLong(size)));
        Glide.with(context).load(new File(fileslist.get(position).getPath())).into(thumbnail);
        double milliseconds = Double.parseDouble(fileslist.get(position).getDuration());
        musicDuration.setText(timeConversion((long) milliseconds));//converting time duration into hrs or minutes or seconds



        // here we have set data to grid view based on the position of list for textview and image view
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"clicked on ",Toast.LENGTH_SHORT).show();
                MyMediaPlayer.getInstance().reset(); //first we reset when media player is playing also
                MyMediaPlayer.currentIndex=position;
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("Files", fileslist);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(intent);
            }
        });

        menu_more=convertView.findViewById(R.id.music_menu);
        menu_more.setOnClickListener(new View.OnClickListener() {
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

    public String timeConversion(long value){
        String musicTime;
        int duration=(int) value;
        int hrs=(duration/3600000);
        int mns=(duration/60000)%60000;
        int scs = duration%60000/1000;
        if(hrs>0){
            musicTime=String.format("%02d:%02:%02",hrs,mns,scs);
        }
        else{
            musicTime=String.format("%02d:%02d",mns,scs);
        }
        return musicTime;
    }
}

