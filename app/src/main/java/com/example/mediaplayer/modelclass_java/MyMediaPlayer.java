package com.example.mediaplayer.modelclass_java;

import android.media.MediaPlayer;

//this class is used to create the instances for media player to play music

public class MyMediaPlayer {
    static MediaPlayer instance;
    public static MediaPlayer getInstance(){
        if(instance==null){
            //if there is no instance i.e., first we called this
            instance=new MediaPlayer();
        }
        return instance;
    }
    //current position
    public static int currentIndex=-1;
}
