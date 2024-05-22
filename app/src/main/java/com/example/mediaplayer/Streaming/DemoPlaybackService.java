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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media3.common.Player;
import androidx.media3.session.MediaLibraryService;
import androidx.media3.session.MediaSession;

import com.example.mediaplayer.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.util.EventLogger;


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
    protected MediaLibrarySession.Callback createLibrarySessionCallback() {
        return new DemoMediaLibrarySessionCallback(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        initializeSessionAndPlayer();
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

    private void initializeSessionAndPlayer() {
        ExoPlayer player = new ExoPlayer.Builder(this).setAudioAttributes(AudioAttributes.DEFAULT, true).build();
        player.addAnalyticsListener(new EventLogger());

        mediaLibrarySession = new MediaLibrarySession.Builder((MediaLibraryService) this, (Player) player, createLibrarySessionCallback())
                .setSessionActivity(getSingleTopActivity())
                .build();
    }




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







/*import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaLibraryService;
import androidx.media3.session.MediaSession;
import androidx.media3.session.SessionToken;

import com.example.mediaplayer.Manifest;
import com.example.mediaplayer.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.util.EventLogger;

import java.util.Objects;

 public class DemoPlaybackService extends MediaLibraryService {

    private static final int FLAG_UPDATE_CURRENT = PendingIntent.FLAG_UPDATE_CURRENT;
    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "demo_session_notification_channel_id";

    private MediaLibrarySession mediaLibrarySession;

    public PendingIntent getSingleTopActivity(){
        return null;
    }
    public PendingIntent getBackStackedActivity() {
        return null;
    }



    protected MediaLibrarySession onCreateMediaLibrarySession(MediaSession.ControllerInfo controllerInfo) {
        return createLibrarySession();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        ExoPlayer player = (ExoPlayer) mediaLibrarySession.getPlayer();
        if (!player.getPlayWhenReady() || player.getMediaItemCount() == 0) {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        if (mediaLibrarySession != null) {
            mediaLibrarySession.release();
            mediaLibrarySession.getPlayer().release();
        }
        super.onDestroy();
    }

    private MediaLibrarySession createLibrarySession() {
        ExoPlayer player = new ExoPlayer.Builder(getApplicationContext()).build();
        player.addAnalyticsListener(new EventLogger());

        mediaLibrarySession = new MediaLibrarySession.Builder(
                getApplicationContext(),
                new PlayerToken(Objects.requireNonNull(player.getPlayerControl())),
                createLibrarySessionCallback())
                .setSessionActivity(getSingleTopActivity())
                .build();
        return mediaLibrarySession;
    }

    private MediaLibrarySession.Callback createLibrarySessionCallback() {
        return new DemoMediaLibrarySessionCallback(getApplicationContext());
    }

    @Override
    protected SessionToken getSessionToken(MediaSession.ControllerInfo controllerInfo) {
        return new MediaLibrarySessionToken(Objects.requireNonNull(mediaLibrarySession));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void ensureNotificationChannel(NotificationManagerCompat notificationManagerCompat) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManagerCompat.createNotificationChannel(channel);
    }

    private void showNotification() {
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Notification permission is required but not granted
            return;
        }
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ensureNotificationChannel(notificationManagerCompat);
        }
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.media3_notification_small_icon)
                        .setContentTitle(getString(R.string.notification_content_title))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(getString(R.string.notification_content_text)))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(getBackStackedActivity());
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    @Nullable
    @Override
    public MediaLibrarySession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return null;
    }
}*/

