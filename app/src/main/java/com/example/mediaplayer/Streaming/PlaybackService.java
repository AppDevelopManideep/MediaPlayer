package com.example.mediaplayer.Streaming;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.TaskStackBuilder;

public class PlaybackService extends DemoPlaybackService {

    private static final int FLAG_UPDATE_CURRENT = PendingIntent.FLAG_UPDATE_CURRENT;

    @Override
    public PendingIntent getSingleTopActivity() {
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, getImmutableFlag() | FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    @Override
    public PendingIntent getBackStackedActivity() {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainStreaming.class));
        stackBuilder.addNextIntent(new Intent(this, PlayerActivity.class));
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, getImmutableFlag() | FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private int getImmutableFlag() {
        int immutableFlag = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            immutableFlag = PendingIntent.FLAG_IMMUTABLE;
        }
        return immutableFlag;
    }
}
