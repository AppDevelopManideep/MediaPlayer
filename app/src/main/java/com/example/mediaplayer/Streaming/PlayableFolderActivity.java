package com.example.mediaplayer.Streaming;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.session.LibraryResult;
import androidx.media3.session.MediaBrowser;
import androidx.media3.session.SessionToken;

import com.example.mediaplayer.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class PlayableFolderActivity extends AppCompatActivity {
    private ListenableFuture<MediaBrowser> browserFuture;
    private ListView mediaList;
    private PlayableMediaItemArrayAdapter mediaListAdapter;
    private final List<MediaItem> subItemMediaList = new ArrayList<>();

    private static final String MEDIA_ITEM_ID_KEY = "MEDIA_ITEM_ID_KEY";

    public static Intent createIntent(Context context, String mediaItemID) {
        Intent intent = new Intent(context, PlayableFolderActivity.class);
        intent.putExtra(MEDIA_ITEM_ID_KEY, mediaItemID);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playable_folder);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mediaList = findViewById(R.id.media_list_view);
        mediaListAdapter = new PlayableMediaItemArrayAdapter(this, R.layout.playable_items, subItemMediaList);
        mediaList.setAdapter(mediaListAdapter);

        mediaList.setOnItemClickListener((parent, view, position, id) -> {
            MediaBrowser browser = null;
            try {
                browser = this.browserFuture.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (browser != null) {
                browser.setMediaItems(subItemMediaList, position, C.TIME_UNSET);
                browser.setShuffleModeEnabled(false);
                browser.prepare();
                browser.play();
                try {
                    Objects.requireNonNull(browser.getSessionActivity()).send();
                } catch (PendingIntent.CanceledException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        findViewById(R.id.shuffle_button).setOnClickListener(v -> {
            MediaBrowser browser = null;
            try {
                browser = this.browserFuture.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (browser != null) {
                browser.setMediaItems(subItemMediaList);
                browser.setShuffleModeEnabled(true);
                browser.prepare();
                browser.play();
                try {
                    Objects.requireNonNull(browser.getSessionActivity()).send();
                } catch (PendingIntent.CanceledException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        findViewById(R.id.play_button).setOnClickListener(v -> {
            MediaBrowser browser = null;
            try {
                browser = this.browserFuture.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (browser != null) {
                browser.setMediaItems(subItemMediaList);
                browser.setShuffleModeEnabled(false);
                browser.prepare();
                browser.play();
                Intent intent = new Intent(this, PlayerActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.open_player_floating_button)
                .setOnClickListener(v -> {
                    MediaBrowser browser = null;
                    try {
                        browser = this.browserFuture.get();
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (browser != null) {
                        try {
                            Objects.requireNonNull(browser.getSessionActivity()).send();
                        } catch (PendingIntent.CanceledException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeBrowser();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseBrowser();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeBrowser() {
        browserFuture =
                new MediaBrowser.Builder(
                        this,
                        new SessionToken(this, new ComponentName(this, PlaybackService.class)))
                        .buildAsync();
        browserFuture.addListener(this::displayFolder, ContextCompat.getMainExecutor(this));
    }

    private void releaseBrowser() {
        MediaBrowser.releaseFuture(browserFuture);
    }

    private void displayFolder() {
        MediaBrowser browser = null;
        try {
            browser = this.browserFuture.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (browser == null) return;

        String id = getIntent().getStringExtra(MEDIA_ITEM_ID_KEY);//getting list from the MainStreaming activity that selected media item id we are getting
        ListenableFuture<LibraryResult<MediaItem>> mediaItemFuture = browser.getItem(id);
        ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> childrenFuture =
                browser.getChildren(id, 0, Integer.MAX_VALUE, null);

        mediaItemFuture.addListener(() -> {
            TextView title = findViewById(R.id.folder_description);
            LibraryResult<MediaItem> result = null;
            try {
                result = mediaItemFuture.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (result != null && result.value != null) {
                title.setText(result.value.mediaMetadata.title);
            }
        }, ContextCompat.getMainExecutor(this));

        childrenFuture.addListener(() -> {
            LibraryResult<ImmutableList<MediaItem>> result = null;
            try {
                result = childrenFuture.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (result != null && result.value != null) {
                List<MediaItem> children = result.value;
                subItemMediaList.clear();
                subItemMediaList.addAll(children);
                mediaListAdapter.notifyDataSetChanged();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private class PlayableMediaItemArrayAdapter extends ArrayAdapter<MediaItem> {
        PlayableMediaItemArrayAdapter(Context context, int viewID, List<MediaItem> mediaItemList) {
            super(context, viewID, mediaItemList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MediaItem mediaItem = getItem(position);
            if (mediaItem == null) return convertView;

            View returnConvertView =
                    convertView != null
                            ? convertView
                            : LayoutInflater.from(getContext()).inflate(R.layout.playable_items, parent, false);

            TextView textView = returnConvertView.findViewById(R.id.media_item);
            textView.setText(mediaItem.mediaMetadata.title);

            Button addButton = returnConvertView.findViewById(R.id.add_button);
            addButton.setOnClickListener(v -> {
                MediaBrowser browser = null;
                try {
                    browser = PlayableFolderActivity.this.browserFuture.get();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (browser != null) {
                    browser.addMediaItem(mediaItem);
                    if (browser.getPlaybackState() == Player.STATE_IDLE) {
                        browser.prepare();
                    }
                    Snackbar.make(findViewById(R.id.linear_layout),
                            getString(R.string.added_media_item_format, mediaItem.mediaMetadata.title),
                            BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            });

            return returnConvertView;
        }
    }
}
