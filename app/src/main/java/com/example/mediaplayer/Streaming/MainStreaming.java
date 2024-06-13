package com.example.mediaplayer.Streaming;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.session.LibraryResult;
import androidx.media3.session.MediaBrowser;
import androidx.media3.session.SessionToken;

import com.example.mediaplayer.R;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MainStreaming extends AppCompatActivity {
    private ListenableFuture<MediaBrowser> browserFuture ;
    private ListView mediaListView;
    MediaBrowser browser ;


    private MediaBrowser getBrowser() {

        if (browserFuture.isDone() && !browserFuture.isCancelled()) {
            try {
                browser = browserFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            browser = null;
        }
        return browser;
    }

    private FolderMediaItemArrayAdapter mediaListAdapter;
    private final ArrayDeque<MediaItem> treePathStack = new ArrayDeque<>();
    private final List<MediaItem> subItemMediaList = new ArrayList<>();



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_streaming);

        mediaListView = findViewById(R.id.media_list_view);

        initializeBrowser();
        mediaListAdapter = new FolderMediaItemArrayAdapter(this, R.layout.folder_items, subItemMediaList);
        mediaListView.setAdapter(mediaListAdapter);
        //passing selected album to the PlayableFolderActivity
        mediaListView.setOnItemClickListener((parent, view, position, id) -> {
            MediaItem selectedMediaItem = Objects.requireNonNull(mediaListAdapter.getItem(position));
            if (Boolean.TRUE.equals(selectedMediaItem.mediaMetadata.isPlayable)) {
                Intent intent = PlayableFolderActivity.createIntent(this, selectedMediaItem.mediaId);
                startActivity(intent);
            } else {
                pushPathStack(selectedMediaItem);
            }
        });

        findViewById(R.id.open_player_floating_button)
                .setOnClickListener(v -> {
                    if (browser != null) {
                        try {
                            browser.getSessionActivity().send();
                        } catch (PendingIntent.CanceledException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                popPathStack();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeBrowser();//connecting to mediabrowser
    }

    @Override
    protected void onStop() {
        releaseBrowser();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), R.string.notification_permission_denied, Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void initializeBrowser() {
        browserFuture =
                new MediaBrowser.Builder(
                        this,
                        new SessionToken(this, new ComponentName(this, PlaybackService.class)))
                        .buildAsync();
        browserFuture.addListener(this::pushRoot, ContextCompat.getMainExecutor(this)); // here if operation is completed pushRoot() is called
    }


    private void releaseBrowser() {
        MediaBrowser.releaseFuture(browserFuture);
    }

    private void displayChildrenList(MediaItem mediaItem) {
        MediaBrowser browser = this.browser;
        if (browser == null) return;

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(treePathStack.size() != 1);
        ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> childrenFuture =
                browser.getChildren(mediaItem.mediaId, 0, Integer.MAX_VALUE, null);

        subItemMediaList.clear();
        childrenFuture.addListener(
                () -> {
                    LibraryResult<ImmutableList<MediaItem>> result = null;
                    try {
                        result = childrenFuture.get();
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (result != null  && result.value!= null) {
                        List<MediaItem> children = result.value;
                        /*subItemMediaList.addAll(children);
                        mediaListAdapter.notifyDataSetChanged();*/
                        runOnUiThread(() -> {
                            subItemMediaList.addAll(children);
                            mediaListAdapter.notifyDataSetChanged();
                        });

                    }
                },
                ContextCompat.getMainExecutor(this));
    }

    private void pushPathStack(MediaItem mediaItem) {
        treePathStack.addLast(mediaItem);
        displayChildrenList(Objects.requireNonNull(treePathStack.peekLast())); //peeklast() retrieves the last element and not removes from the dequene
    }

    private void popPathStack() {
        treePathStack.removeLast();
        if (treePathStack.isEmpty()) {
            finish();
            return;
        }

        displayChildrenList(Objects.requireNonNull(treePathStack.peekLast()));
    }

    private void pushRoot() {
        if (!treePathStack.isEmpty()) return;

       // browser = this.browser;
        browser = getBrowser();
        if (browser == null) return;

        ListenableFuture<LibraryResult<MediaItem>> rootFuture = browser.getLibraryRoot(null);
        rootFuture.addListener(
                () -> {
                    LibraryResult<MediaItem> result = null;
                    try {
                        result = rootFuture.get();
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (result != null && result.value != null) {
                        MediaItem root = result.value;
                        pushPathStack(root);
                    }
                },
                ContextCompat.getMainExecutor(this));
    }

    public static class FolderMediaItemArrayAdapter extends ArrayAdapter<MediaItem> {
        private Context context;
        private List<MediaItem> mediaItemList;
        private int viewID;

        public FolderMediaItemArrayAdapter(Context context, int viewID, List<MediaItem> mediaItemList) {
            super(context, viewID,mediaItemList);
            this.context = context;
            this.viewID = viewID;
            this.mediaItemList = mediaItemList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MediaItem mediaItem = getItem(position);
            View returnConvertView = convertView;
            if (returnConvertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                returnConvertView = inflater.inflate(viewID, parent, false);
            }

            if (mediaItem != null) {
                TextView textView = returnConvertView.findViewById(R.id.media_item);
                textView.setText(mediaItem.mediaMetadata.title);
            }

            return returnConvertView;
        }
    }

}
