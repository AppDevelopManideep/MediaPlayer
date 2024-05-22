package com.example.mediaplayer.Streaming;

import static android.media.metrics.TrackChangeEvent.TRACK_TYPE_TEXT;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;
import androidx.media3.ui.PlayerView;

import com.example.mediaplayer.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class PlayerActivity extends AppCompatActivity {
    private ListenableFuture<MediaController> controllerFuture;
    private PlayerView playerView;
    private ListView mediaItemListView;
    private MediaItemListAdapter mediaItemListAdapter;
    private final List<MediaItem> mediaItemList = new ArrayList<>();
    private String lastMediaItemId;

    @OptIn(markerClass = UnstableApi.class) @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        playerView = findViewById(R.id.player_view);

        mediaItemListView = findViewById(R.id.current_playing_list);
        mediaItemListAdapter = new MediaItemListAdapter(this, R.layout.folder_items, mediaItemList);
        mediaItemListView.setAdapter(mediaItemListAdapter);
        mediaItemListView.setOnItemClickListener((parent, view, position, id) -> {
            MediaController controller = null;
            try {
                controller = controllerFuture.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (controller != null) {
                if (controller.getCurrentMediaItemIndex() == position) {
                    controller.setPlayWhenReady(!controller.getPlayWhenReady());
                    if (controller.getPlayWhenReady()) {
                        playerView.hideController();
                    }
                } else {
                    controller.seekToDefaultPosition(position);
                    mediaItemListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeController();
    }

    @Override
    protected void onStop() {
        super.onStop();
        playerView.setPlayer(null);
        releaseController();
    }

    private void initializeController() {
        controllerFuture = new MediaController.Builder(this,
                new SessionToken(this, new ComponentName(this, PlaybackService.class)))
                .buildAsync();
        updateMediaMetadataUI();
        controllerFuture.addListener(this::setController, MoreExecutors.directExecutor());
    }

    private void releaseController() {
        MediaController.releaseFuture(controllerFuture);
    }

    private void setController() {
        MediaController controller = null;
        try {
            controller = controllerFuture.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (controller == null) return;

        playerView.setPlayer(controller);

        updateCurrentPlaylistUI();
        updateMediaMetadataUI();

        controller.addListener(new Player.Listener() {
            @OptIn(markerClass = UnstableApi.class) @Override
            public void onEvents(@NonNull Player player, @NonNull Player.Events events) {
                if (events.contains(Player.EVENT_TRACKS_CHANGED)) {
                    playerView.setShowSubtitleButton(player.getCurrentTracks().isTypeSupported(TRACK_TYPE_TEXT));
                }
                if (events.contains(Player.EVENT_TIMELINE_CHANGED)) {
                    updateCurrentPlaylistUI();
                }
                if (events.contains(Player.EVENT_MEDIA_METADATA_CHANGED)) {
                    updateMediaMetadataUI();
                }
                if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                    mediaItemListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void updateMediaMetadataUI() {
        MediaController controller = null;
        try {
            controller = this.controllerFuture.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (controller == null || controller.getMediaItemCount() == 0) {
            TextView title = findViewById(R.id.media_title);
            title.setText(getString(R.string.waiting_for_metadata));
            TextView artist = findViewById(R.id.media_artist);
            artist.setText("");
            return;
        }

        MediaMetadata mediaMetadata = controller.getMediaMetadata();
        CharSequence title = mediaMetadata.title != null ? mediaMetadata.title : "";

        TextView titleTextView = findViewById(R.id.media_title);
        titleTextView.setText(title);
        TextView artistTextView = findViewById(R.id.media_artist);
        artistTextView.setText(mediaMetadata.artist);
    }

    private void updateCurrentPlaylistUI() {
        MediaController controller = null;
        try {
            controller = this.controllerFuture.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (controller == null) return;
        mediaItemList.clear();
        for (int i = 0; i < controller.getMediaItemCount(); i++) {
            mediaItemList.add(controller.getMediaItemAt(i));
        }
        mediaItemListAdapter.notifyDataSetChanged();
    }

    private class MediaItemListAdapter extends ArrayAdapter<MediaItem> {
        MediaItemListAdapter(Context context, int viewID, List<MediaItem> mediaItemList) {
            super(context, viewID, mediaItemList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MediaItem mediaItem = getItem(position);
            if (mediaItem == null) return convertView;

            View returnConvertView = convertView != null
                    ? convertView
                    : LayoutInflater.from(getContext()).inflate(R.layout.playlist_items, parent, false);

            TextView mediaItemTextView = returnConvertView.findViewById(R.id.media_item);
            mediaItemTextView.setText(mediaItem.mediaMetadata.title);

            Button deleteButton = returnConvertView.findViewById(R.id.delete_button);
            try {
                if (Objects.equals(position, controllerFuture.get().getCurrentMediaItemIndex())) {
                    returnConvertView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.playlist_item_background));
                    mediaItemTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                    deleteButton.setVisibility(View.GONE);
                } else {
                    returnConvertView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.player_background));
                    mediaItemTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                    deleteButton.setVisibility(View.VISIBLE);
                    deleteButton.setOnClickListener(v -> {
                        MediaController controller = null;
                        try {
                            controller = controllerFuture.get();
                        } catch (ExecutionException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if (controller != null) {
                            controller.removeMediaItem(position);
                            updateCurrentPlaylistUI();
                        }
                    });
                }
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return returnConvertView;
        }
    }
}
