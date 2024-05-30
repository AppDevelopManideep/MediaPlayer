package com.example.mediaplayer.Streaming;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaLibraryService;
import androidx.media3.session.MediaSession;

import com.example.mediaplayer.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.util.EventLogger;

import org.json.JSONException;

import java.io.IOException;


public class DemoPlaybackService extends MediaLibraryService {

    private MediaLibrarySession mediaLibrarySession;

    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "demo_session_notification_channel_id";

    @Nullable
    public PendingIntent getSingleTopActivity() {
        return null;
    }

    @Nullable
    public PendingIntent getBackStackedActivity() {
        return null;
    }

    @NonNull
    protected MediaLibrarySession.Callback createLibrarySessionCallback() throws JSONException, IOException {
        try {
            return new DemoMediaLibrarySessionCallback(this);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            initializeSessionAndPlayer();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setListener(new MediaSessionServiceListener());
    }

    @NonNull
    @Override
    public MediaLibrarySession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
        return mediaLibrarySession;
    }

    @Override
    public void onTaskRemoved(@Nullable Intent rootIntent) {
        ExoPlayer player = (ExoPlayer) mediaLibrarySession.getPlayer();
        if (!player.getPlayWhenReady() || player.getMediaItemCount() == 0) {
            stopSelf();
        }
    }


    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onDestroy() {
        PendingIntent backStackedActivity = getBackStackedActivity();
        if (backStackedActivity != null) {
            mediaLibrarySession.setSessionActivity(backStackedActivity);
        }
        mediaLibrarySession.release();
        mediaLibrarySession.getPlayer().release();
        clearListener();
        super.onDestroy();
    }

    private void initializeSessionAndPlayer() throws JSONException, IOException {
        ExoPlayer player = new ExoPlayer.Builder(this).setAudioAttributes(AudioAttributes.DEFAULT, true).build();
        player.addAnalyticsListener(new EventLogger());

        try {
            mediaLibrarySession = new MediaLibrarySession.Builder((MediaLibraryService) this, (Player) player, createLibrarySessionCallback())
                    .setSessionActivity(getSingleTopActivity())
                    .build();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




     @UnstableApi
     private class MediaSessionServiceListener implements Listener {

        @Override
        public void onForegroundServiceStartNotAllowedException() {
            if (Build.VERSION.SDK_INT >= 33 &&
                    checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(DemoPlaybackService.this);
            ensureNotificationChannel(notificationManagerCompat);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(DemoPlaybackService.this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.foldericon)
                    .setContentTitle(getString(R.string.notification_content_title))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.notification_content_text)))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
            PendingIntent backStackedActivity = getBackStackedActivity();
            if (backStackedActivity != null) {
                builder.setContentIntent(backStackedActivity);
            }
            notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void ensureNotificationChannel(NotificationManagerCompat notificationManagerCompat) {
        if (Build.VERSION.SDK_INT < 26 || notificationManagerCompat.getNotificationChannel(CHANNEL_ID) != null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManagerCompat.createNotificationChannel(channel);
    }
}









