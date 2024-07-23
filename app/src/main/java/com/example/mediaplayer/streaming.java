package com.example.mediaplayer;

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
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.session.LibraryResult;
import androidx.media3.session.MediaBrowser;
import androidx.media3.session.SessionToken;

import com.example.mediaplayer.Streaming.PlayableFolderActivity;
import com.example.mediaplayer.Streaming.PlaybackService;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;



public class streaming extends Fragment {


    private ListenableFuture<MediaBrowser> browserFuture ;
    private ListView mediaListView;
    MediaBrowser browser ;
    //ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
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

    @Override
    public void onViewCreated( View view,  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle back press event

                popPathStack();

            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_streaming,container,false);
        mediaListView = view.findViewById(R.id.media_list_view);

        initializeBrowser();
        mediaListAdapter = new FolderMediaItemArrayAdapter(getContext(), R.layout.folder_items, subItemMediaList);
        mediaListView.setAdapter(mediaListAdapter);

        mediaListView.setAdapter(mediaListAdapter);
        //passing selected album to the PlayableFolderActivity
        mediaListView.setOnItemClickListener((parent, View, position, id) -> {
            MediaItem selectedMediaItem = Objects.requireNonNull(mediaListAdapter.getItem(position));
            if (Boolean.TRUE.equals(selectedMediaItem.mediaMetadata.isPlayable)) {
                Intent intent = PlayableFolderActivity.createIntent(getContext(), selectedMediaItem.mediaId);
                startActivity(intent);
            } else {
                pushPathStack(selectedMediaItem);
            }
        });

        view.findViewById(R.id.open_player_floating_button)
                .setOnClickListener(v -> {
                    if (browser != null) {
                        try {
                            browser.getSessionActivity().send();
                        } catch (PendingIntent.CanceledException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });



      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Check if VIBRATE (for notifications) permission is not granted
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.VIBRATE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request VIBRATE permission
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.VIBRATE},
                        1);
            }

            // Check if WAKE_LOCK permission is not granted
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WAKE_LOCK)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request WAKE_LOCK permission
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WAKE_LOCK},
                        2);
            }
        }*/

        return view;


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            requireActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onStart() {
        super.onStart();
        initializeBrowser();//connecting to mediabrowser
    }

    @Override
    public void onStop() {
        releaseBrowser();
        super.onStop();
    }

   /* @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1: {
                // Check if VIBRATE permission is granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, you can now proceed with your functionality
                    // For example, start using notifications that require vibration
                } else {
                    // Permission denied, handle this according to your app's logic
                    // You may disable certain features or show an explanation dialog
                }
                break;
            }
            case 2: {
                // Check if WAKE_LOCK permission is granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, you can now proceed with your functionality
                    // For example, acquire wake locks as needed
                } else {
                    // Permission denied, handle this according to your app's logic
                    // You may disable certain features or show an explanation dialog
                }
                break;
            }
            // Handle other permissions if needed

            default:
                // Handle other requestCode cases if any
                break;
        }

    }*/
    private void initializeBrowser() {
        browserFuture =
                new MediaBrowser.Builder(
                        getContext(),
                        new SessionToken(getContext(), new ComponentName(getContext(), PlaybackService.class)))
                        .buildAsync();
        browserFuture.addListener(this::pushRoot, ContextCompat.getMainExecutor(getContext())); // here if operation is completed pushRoot() is called
    }


    private void releaseBrowser() {
        MediaBrowser.releaseFuture(browserFuture);
    }

    private void displayChildrenList(MediaItem mediaItem) {
        MediaBrowser browser = this.browser;
        if (browser == null) return;

        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(treePathStack.size() != 1);
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
                        requireActivity().runOnUiThread(() -> {
                            subItemMediaList.addAll(children);
                            mediaListAdapter.notifyDataSetChanged();
                        });

                    }
                },
                ContextCompat.getMainExecutor(getContext()));
    }

    private void pushPathStack(MediaItem mediaItem) {
        treePathStack.addLast(mediaItem);
        displayChildrenList(Objects.requireNonNull(treePathStack.peekLast())); //peeklast() retrieves the last element and not removes from the dequene
    }

    private void popPathStack() {
        treePathStack.removeLast();
        if (treePathStack.isEmpty()) {
            requireActivity().finish();
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
                ContextCompat.getMainExecutor(getContext()));
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



