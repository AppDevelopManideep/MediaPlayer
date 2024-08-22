package com.example.mediaplayer.player_activity;

import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mediaplayer.MyMediaBrowserService;
import com.example.mediaplayer.R;
import com.example.mediaplayer.modelclass_java.MediaFiles;
import com.example.mediaplayer.modelclass_java.MyMediaPlayer;

import java.util.ArrayList;

public class MusicPlayerActivity extends AppCompatActivity {

    boolean i = true;

    TextView title, current_time, total_time;
    SeekBar seekbar;

    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;

    private ImageView pause, next, previous, music_icon;
    private ArrayList<MediaFiles> musiclist;
    private Intent serviceIntent;


    private Class<MyMediaBrowserService> cls;

    //to make any updates to the ui continously
    Handler handler = new Handler();
    Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaController != null) {
                int currentPosition = (int) (mediaController.getPlaybackState().getPosition() / 1000); // Get current position in seconds
                seekbar.setProgress(currentPosition);

                int minutes = currentPosition / 60;
                int seconds = currentPosition % 60;
                String time = String.format("%d:%02d", minutes, seconds);
                current_time.setText(time);

                handler.postDelayed(this, 1000); // Update every second
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        title = findViewById(R.id.music_title);
        cls = MyMediaBrowserService.class;
        current_time = findViewById(R.id.current_time);
        total_time = findViewById(R.id.total_time);
        seekbar = findViewById(R.id.seekbar);

        pause = findViewById(R.id.pause);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        music_icon = findViewById(R.id.music_icon);
        musiclist = (ArrayList<MediaFiles>) getIntent().getSerializableExtra("Files");//getting list of songs from folders
        Toast.makeText(this,musiclist.get(MyMediaPlayer.currentIndex).getDuration(),Toast.LENGTH_LONG).show();
        serviceIntent = new Intent(this, cls);
        Bundle mediaData = new Bundle();
        mediaData.putSerializable("MusicList", musiclist);
        mediaData.putInt("CurrentIndex", MyMediaPlayer.currentIndex);
        serviceIntent.putExtra("MediaData", mediaData);
        startService(serviceIntent);
        //new creating instance of MediaBrowser service
        mediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, cls),
                connectionCallbacks,
                null);
        seekbar
                .setOnSeekBarChangeListener(
                        new SeekBar
                                .OnSeekBarChangeListener() {

                            // When the progress value has changed
                            @Override
                            public void onProgressChanged(
                                    SeekBar seekBar,
                                    int progress,
                                    boolean fromUser)
                            {

                                if (fromUser && mediaController != null) {
                                    mediaController.getTransportControls().seekTo(progress * 1000);

                                    // Seek to the corresponding position in milliseconds
                                }

                                // increment 1 in progress and
                                // increase the textsize
                                // with the value of progress
                                int minutes = progress / 60;
                                int seconds = progress % 60;
                                String time = String.format("0%d:%02d", minutes,seconds);

                                current_time.setText(time);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar)
                            {

                                // This method will automatically
                                // called when the user touches the SeekBar
                                handler.removeCallbacks(updateSeekBarRunnable); // Stop updating while user is dragging
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar)
                            {

                                // This method will automatically
                                // called when the user
                                // stops touching the SeekBar
                                handler.post(updateSeekBarRunnable); // Resume updating when user stops dragging
                            }

                        });

    }

    //the below method is used to get the token  of the service.create an instance of ConnectionCallback. Modify its onConnected() method to retrieve the media session token from the MediaBrowserService and use the token to create a MediaControllerCompat.
    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {

                    mediaController = new MediaControllerCompat(MusicPlayerActivity.this,
                            mediaBrowser.getSessionToken());

                    //Register a callback to receive updates on playback state changes
                    // Set the MediaController for the MediaControllerCompat widget
                    MediaControllerCompat.setMediaController(MusicPlayerActivity.this, mediaController);

                    // Finish building the UI
                    buildTransportControls();
                    //mediaController.registerCallback(controllerCallback);
                    handler.post(updateSeekBarRunnable); // Start updating the SeekBar when player connected


                }

                @Override
                public void onConnectionSuspended() {
                    // The Service has crashed. Disable transport controls until it automatically reconnects
                }

                @Override
                public void onConnectionFailed() {
                    // The Service has refused our connection
                }
            };

    //the below is used to convert the time of player into particular format
    public String timeConversion(long value) {
        String musicTime;
        int duration = (int) value;
        int hrs = (duration / 3600000);
        int mns = (duration / 60000) % 60000;
        int scs = duration % 60000 / 1000;
        if (hrs > 0) {
            musicTime = String.format("%02d:%02:%02", hrs, mns, scs);
        } else {
            musicTime = String.format("%02d:%02d", mns, scs);
        }
        return musicTime;
    }

    //newly added
    @Override
    public void onStart() {
        super.onStart();
        mediaBrowser.connect();

    }

    @Override
    public void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

    }

    @Override
    public void onStop() {
        super.onStop();
        // (see "stay in sync with the MediaSession")
        if (MediaControllerCompat.getMediaController(MusicPlayerActivity.this) != null) {
           // MediaControllerCompat.getMediaController(MusicPlayerActivity.this).unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();
        handler.removeCallbacks(updateSeekBarRunnable); // Stop updating the SeekBar
    }

    protected void onDestroy() {
        super.onDestroy();
        // Disconnect from the MediaBrowserService
        if (mediaBrowser != null) {
            mediaBrowser.disconnect();
        }
        stopService(serviceIntent);
        handler.removeCallbacks(updateSeekBarRunnable); // Stop updating the SeekBar

    }

    // it is used to interact with the ui
    void buildTransportControls() {
        // Grab the view for the play/pause button


       title.setText(musiclist.get(MyMediaPlayer.currentIndex).getDisplayname());
       total_time.setText(timeConversion((Long.parseLong(musiclist.get(MyMediaPlayer.currentIndex).getDuration()))));

        seekbar.setMax((Integer.parseInt(String.valueOf((musiclist.get(MyMediaPlayer.currentIndex).getDuration())))) / 1000);

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = !i;
                if (mediaController != null && i) {

                    // Toggle between play and pause
                    MediaControllerCompat.getMediaController(MusicPlayerActivity.this)
                            .getTransportControls().play();


                    pause.setImageResource(R.drawable.baseline_pause_circle);
                    title.setText(musiclist.get(MyMediaPlayer.currentIndex).getDisplayname());
                    total_time.setText(timeConversion((Long.parseLong(musiclist.get(MyMediaPlayer.currentIndex).getDuration()))));

                   // seekbar.setMax((Integer.parseInt(String.valueOf((musiclist.get(MyMediaPlayer.currentIndex).getDuration())))) / 1000);

                    //title.setText(MediaControllerCompat.getMediaController(MusicPlayerActivity.this).getTransportControls().);
                } else {

                    MediaControllerCompat.getMediaController(MusicPlayerActivity.this)
                            .getTransportControls().pause();
                    pause.setImageResource(R.drawable.baseline_play_arrow_24);
                }
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null && (MyMediaPlayer.currentIndex > 0)) {
                    // Send skip to previous command to MyMediaBrowserService
                    MyMediaPlayer.currentIndex = MyMediaPlayer.currentIndex - 1;
                    title.setText(musiclist.get(MyMediaPlayer.currentIndex).getDisplayname());
                    total_time.setText(timeConversion((Long.parseLong(musiclist.get(MyMediaPlayer.currentIndex).getDuration()))));
                    seekbar.setMax((Integer.parseInt(String.valueOf((musiclist.get(MyMediaPlayer.currentIndex).getDuration())))) / 1000);

                    MediaControllerCompat.getMediaController(MusicPlayerActivity.this)
                            .getTransportControls().skipToPrevious();

                } else {
                    MyMediaPlayer.currentIndex = musiclist.size() - 1;
                    title.setText(musiclist.get(MyMediaPlayer.currentIndex).getDisplayname());
                    total_time.setText(timeConversion((Long.parseLong(musiclist.get(MyMediaPlayer.currentIndex).getDuration()))));
                    seekbar.setMax((Integer.parseInt(String.valueOf((musiclist.get(MyMediaPlayer.currentIndex).getDuration())))) / 1000);

                    MediaControllerCompat.getMediaController(MusicPlayerActivity.this)
                            .getTransportControls().skipToPrevious();
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null && (MyMediaPlayer.currentIndex < (musiclist.size() - 1))) {
                    // Send skip to previous command to MyMediaBrowserService
                    MyMediaPlayer.currentIndex = MyMediaPlayer.currentIndex + 1;
                    title.setText(musiclist.get(MyMediaPlayer.currentIndex).getDisplayname());
                    total_time.setText(timeConversion((Long.parseLong(musiclist.get(MyMediaPlayer.currentIndex).getDuration()))));
                    seekbar.setMax((Integer.parseInt(String.valueOf((musiclist.get(MyMediaPlayer.currentIndex).getDuration())))) / 1000);

                    MediaControllerCompat.getMediaController(MusicPlayerActivity.this)
                            .getTransportControls().skipToNext();

                } else {
                    MyMediaPlayer.currentIndex = 0;
                    title.setText(musiclist.get(MyMediaPlayer.currentIndex).getDisplayname());
                    total_time.setText(timeConversion((Long.parseLong(musiclist.get(MyMediaPlayer.currentIndex).getDuration()))));
                    seekbar.setMax((Integer.parseInt(String.valueOf((musiclist.get(MyMediaPlayer.currentIndex).getDuration())))) / 1000);

                    MediaControllerCompat.getMediaController(MusicPlayerActivity.this)
                            .getTransportControls().skipToPrevious();
                }
            }
        });
    }
}