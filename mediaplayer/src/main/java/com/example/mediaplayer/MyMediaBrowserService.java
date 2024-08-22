package com.example.mediaplayer;


import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.media.MediaBrowserServiceCompat;

import com.example.mediaplayer.modelclass_java.MediaFiles;
import com.example.mediaplayer.modelclass_java.MyMediaPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

public class MyMediaBrowserService extends MediaBrowserServiceCompat {
    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    MediaPlayer mediaFiles = MyMediaPlayer.getInstance();

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    ArrayList<MediaFiles> musicList;
    private SimpleExoPlayer exoPlayer;
    int currentIndex;

    @Override
    public void onCreate() {
        super.onCreate();

        exoPlayer = new SimpleExoPlayer.Builder(this).build();
        exoPlayer.setPlayWhenReady(true);
        // Create a MediaSessionCompat
        mediaSession = new MediaSessionCompat(this, "MyMediaSession");

        // Enable callbacks from MediaButtons and TransportControls
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());

        // MySessionCallback() has methods that handle callbacks from a media controller


        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession.getSessionToken());

    }
    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid,
                                 Bundle rootHints) {


            return new BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null);

    }


    @Override
    public void onLoadChildren(final String parentMediaId,
                               final Result<List<MediaBrowserCompat.MediaItem>> result) {


    }


    //for retriving files from the app/intent

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Bundle mediaData = extras.getBundle("MediaData");
                musicList = (ArrayList<MediaFiles>) mediaData.getSerializable("MusicList");
                currentIndex = mediaData.getInt("CurrentIndex");
                preparePlayer(musicList.get(currentIndex).getPath());
                // Now you have access to the music list and the current index.
                // You can use them as needed in your service.

                // Initialize media session and other components
                // Set callback for MediaSessionCompat
                mediaSession.setCallback(new MediaSessionCompat.Callback() {
                    @Override
                    public void onPlay() {
                        // Handle play action
                        playMedia();
                    }

                    @Override
                    public void onPause() {
                        // Handle pause action
                        pauseMedia();
                    }

                    @Override
                    public void onSkipToNext() {
                        // Handle skip to next action
                        skipToNextMedia();
                    }

                    @Override
                    public void onSkipToPrevious() {
                        // Handle skip to previous action
                        skipToPreviousMedia();
                    }
                    @Override
                    public void onSeekTo(long pos) {
                        // Handle seek action

                        seekToPosition(pos);

                    }

                });
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void seekToPosition(long pos) {
        if (exoPlayer != null) {
            exoPlayer.seekTo(pos);
            Log.d("TAG123", "seekToPosition: "+pos);
        }

        // Update playback state, assuming you are using MediaSessionCompat
        if (mediaSession != null) {
            PlaybackStateCompat.Builder playbackStateBuilder = new PlaybackStateCompat.Builder();
            playbackStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, pos, 1.0f);
            mediaSession.setPlaybackState(playbackStateBuilder.build());
        }
    }

    private void skipToPreviousMedia() {

        //currentIndex = (currentIndex - 1 + musicList.size()) % musicList.size();
        if((currentIndex>0 ) ) {
            currentIndex = currentIndex - 1;
            preparePlayer(musicList.get(currentIndex).getPath());
        }

         else{
            currentIndex=musicList.size()-1;
            preparePlayer(musicList.get(currentIndex).getPath());

        }
    }

    private void skipToNextMedia() {
        if((currentIndex<(musicList.size()-1) )) {
            currentIndex = currentIndex + 1;
            //currentIndex = (currentIndex + 1) % musicList.size();
            preparePlayer(musicList.get(currentIndex).getPath());
        }
        else{
            currentIndex=0;
            preparePlayer(musicList.get(currentIndex).getPath());
        }
    }

    private void pauseMedia() {
        exoPlayer.setPlayWhenReady(false);
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build());
    }

    private void playMedia() {
        if (musicList != null && !musicList.isEmpty()) {
            MediaFiles currentMedia = musicList.get(currentIndex);

            // Start playback of currentMedia using your media player (e.g., SimpleExoPlayer)
            exoPlayer.setPlayWhenReady(true);

            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                    .build());
        }
    }

    // Method to prepare ExoPlayer with media file
   private void preparePlayer(String mediaUrl) {
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "MyApp"));
        //media source used to provide mediacontent to exoplayer to play
        ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory,
                new DefaultExtractorsFactory()).createMediaSource(MediaItem.fromUri(Uri.parse(mediaUrl)));
        exoPlayer.prepare(mediaSource);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        exoPlayer.release();
        mediaSession.release();

    }
}
