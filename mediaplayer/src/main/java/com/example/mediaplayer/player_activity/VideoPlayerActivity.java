package com.example.mediaplayer.player_activity;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.ConcatenatingMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerView;

import com.example.mediaplayer.R;
import com.example.mediaplayer.modelclass_java.MediaFiles;
import com.example.mediaplayer.modelclass_java.MyMediaPlayer;

import java.io.File;
import java.util.ArrayList;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener{
    private ArrayList<MediaFiles> videolist;
    private PlayerView playerView;
    private ExoPlayer player;
    private int currentindex;
    MediaSessionCompat mediaSession;
    PlaybackStateCompat.Builder stateBuilder;
    MediaControllerCompat mediaController;
    ImageView nextButton,previousButton,pause,play,forward,rewind;
    TextView title;

    ConcatenatingMediaSource concatenatingMediaSource;

    @SuppressLint({"RestrictedApi",  "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        videolist = (ArrayList<MediaFiles>) getIntent().getSerializableExtra("Files");
        playerView = findViewById(R.id.exoplayer_view);



        // Enable default playback controls
        playerView.setUseController(true);
        nextButton=findViewById(R.id.exo_next);
        previousButton=findViewById(R.id.exo_prev);

        title=findViewById(R.id.video_title);

            // Create a MediaSessionCompat
            mediaSession = new MediaSessionCompat(this, LOG_TAG);

            // Enable callbacks from MediaButtons and TransportControls
            mediaSession.setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

            // Do not let MediaButtons restart the player when the app is not visible
            mediaSession.setMediaButtonReceiver(null);

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = new PlaybackStateCompat.Builder()
                    .setActions(
                            PlaybackStateCompat.ACTION_PLAY |
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE);
            mediaSession.setPlaybackState(stateBuilder.build());
        player = new ExoPlayer.Builder(this).build();;
        playerView.setPlayer(player);




            // MySessionCallback has methods that handle callbacks from a media controller
            mediaSession.setCallback(new MySessionCallback());

            // Create a MediaControllerCompat
            mediaController =
                    new MediaControllerCompat(this, mediaSession);

            mediaController.setMediaController(this, mediaController);

        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);

        // Prepare the first video in the list
        playvideo();
    }


    @OptIn(markerClass = UnstableApi.class)
    private void playvideo(){
        String path = videolist.get(MyMediaPlayer.currentIndex).getPath();
        title.setText(videolist.get(MyMediaPlayer.currentIndex).getDisplayname());
        Uri uri =Uri.parse(path);

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this,"app"));
        //this is used to play vedeo one after other without interruption
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for(int i=0;i<videolist.size();i++){
            new File(String.valueOf(videolist.get(i)));
            // for setting the media item in the exoplayer we use MediaSource
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(uri))));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);//to prevent the screen from dimming
        player.prepare(concatenatingMediaSource);
        player.seekTo(C.TIME_UNSET);

        playError();
    }

    private void playError() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this,"Videoplaying Error",Toast.LENGTH_SHORT);
            }
        });
        player.setPlayWhenReady(true);
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        if(player.isPlaying()){
            player.stop();
        }
    }
    @Override
    public void onPause(){
        super.onPause();
        player.setPlayWhenReady(false); //when app pauses vedeo also pauses
        player.getPlaybackState();
    }
    //app resumed again
    @Override
    public void onResume(){
        super.onResume();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }
    @Override
    public void onRestart(){
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.exo_next:
                mediaController.getTransportControls().skipToNext();
                break;
            case R.id.exo_prev:
                mediaController.getTransportControls().skipToPrevious();
                break;
            case R.id.exo_forward:
                mediaController.getTransportControls().fastForward();



        }

    }


    public class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            // Handle play event
            mediaController.getTransportControls().play();



        }

        @Override
        public void onPause() {
            // Handle pause event
            mediaController.getTransportControls().pause();
        }


        @Override
        public void onSkipToNext() {
            // Handle skip to next event
            try {
                player.stop();
                MyMediaPlayer.currentIndex = MyMediaPlayer.currentIndex + 1;
                playvideo();
            }catch (Exception e){
                Toast.makeText(VideoPlayerActivity.this,"No Next Video",Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        @Override
        public void onSkipToPrevious() {
            // Handle skip to previous event
            try {
                player.stop();
                MyMediaPlayer.currentIndex = MyMediaPlayer.currentIndex - 1;
                playvideo();
            }catch (Exception e){
                Toast.makeText(VideoPlayerActivity.this,"No Previous Video",Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        @Override
        public void onFastForward(){
            PlaybackStateCompat playbackState= mediaController.getPlaybackState();
            if(playbackState!=null){
                long currentPosition = playbackState.getPosition();
                mediaController.getTransportControls().seekTo(currentPosition+10000);
            }

        }

        @Override
        public void onStop() {
            // Handle stop event
        }

        @Override
        public void onSeekTo(long pos) {
            // Handle seek to specific position event
        }
    }




}