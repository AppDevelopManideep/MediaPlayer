package com.example.mediaplayer.Streaming;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.CommandButton;
import androidx.media3.session.LibraryResult;
import androidx.media3.session.MediaLibraryService;
import androidx.media3.session.MediaSession;
import androidx.media3.session.SessionCommand;
import androidx.media3.session.SessionResult;

import com.example.mediaplayer.R;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;

public class DemoMediaLibrarySessionCallback implements MediaLibraryService.MediaLibrarySession.Callback {

    private Context context;
    private List<CommandButton> customLayoutCommandButtons;
    private SessionCommandGroup mediaNotificationSessionCommands;
    private static final String CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON = "android.media3.session.demo.SHUFFLE_ON";
    private static final String CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF = "android.media3.session.demo.SHUFFLE_OFF";

    @OptIn(markerClass = UnstableApi.class)
    public DemoMediaLibrarySessionCallback(@NonNull Context context) {
        this.context = context;

        // Initialize MediaItemTree
        MediaItemTree.initialize(context.getAssets());

        // Initialize customLayoutCommandButtons
        this.customLayoutCommandButtons = new ArrayList<>();
        customLayoutCommandButtons.add(
                new CommandButton.Builder()
                        .setDisplayName(context.getString(R.string.exo_controls_shuffle_on_description))
                        .setSessionCommand(new SessionCommand(CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON, Bundle.EMPTY))
                        .setIconResId(R.drawable.foldericon)
                        .build()
        );
        customLayoutCommandButtons.add(
                new CommandButton.Builder()
                        .setDisplayName(context.getString(R.string.exo_controls_shuffle_off_description))
                        .setSessionCommand(new SessionCommand(CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF, Bundle.EMPTY))
                        .setIconResId(R.drawable.log)
                        .build()
        );

        // Initialize mediaNotificationSessionCommands
        SessionCommandGroup.Builder builder = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon();
        for (CommandButton commandButton : customLayoutCommandButtons) {
            SessionCommand sessionCommand = commandButton.getSessionCommand();
            if (sessionCommand != null) {
                builder.add(sessionCommand);
            }
        }
        this.mediaNotificationSessionCommands = builder.build();
    }

    // Define constants
    private static final String CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON = "android.media3.session.demo.SHUFFLE_ON";
    private static final String CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF = "android.media3.session.demo.SHUFFLE_OFF";

    @NonNull
    @Override
    public MediaSession.ConnectionResult onConnect(
            @NonNull MediaSession session, @NonNull MediaSession.ControllerInfo controller) {
        if (session.isMediaNotificationController(controller) ||
                session.isAutomotiveController(controller) ||
                session.isAutoCompanionController(controller)) {
            // Select the button to display.
            CommandButton customLayout = customLayoutCommandButtons.get(session.getPlayer().getShuffleModeEnabled() ? 1 : 0);
            return new MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                    .setAvailableSessionCommands(mediaNotificationSessionCommands)
                    .setCustomLayout(ImmutableList.of(customLayout))
                    .build();
        }
        // Default commands without custom layout for common controllers.
        return new MediaSession.ConnectionResult.AcceptedResultBuilder(session).build();
    }

    @NonNull
    @Override
    public ListenableFuture<SessionResult> onCustomCommand(
            @NonNull MediaSession session,
            @NonNull MediaSession.ControllerInfo controller,
            @NonNull SessionCommand customCommand,
            @NonNull Bundle args) {
        if (CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON.equals(customCommand.getCustomAction())) {
            // Enable shuffling.
            session.getPlayer().setShuffleModeEnabled(true);
            // Change the custom layout to contain the `Disable shuffling` command.
            session.setCustomLayout(
                    session.getMediaNotificationControllerInfo(),
                    ImmutableList.of(customLayoutCommandButtons.get(1))
            );
            return Futures.immediateFuture(new SessionResult(SessionResult.RESULT_SUCCESS));
        } else if (CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF.equals(customCommand.getCustomAction())) {
            // Disable shuffling.
            session.getPlayer().setShuffleModeEnabled(false);
            // Change the custom layout to contain the `Enable shuffling` command.
            session.setCustomLayout(
                    session.getMediaNotificationControllerInfo(),
                    ImmutableList.of(customLayoutCommandButtons.get(0))
            );
            return Futures.immediateFuture(new SessionResult(SessionResult.RESULT_SUCCESS));
        }
        return Futures.immediateFuture(new SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED));
    }

    @NonNull
    @Override
    public ListenableFuture<LibraryResult<MediaItem>> onGetLibraryRoot(
            @NonNull MediaLibraryService.MediaLibrarySession session,
            @NonNull MediaSession.ControllerInfo browser,
            MediaLibraryService.LibraryParams params) {
        return Futures.immediateFuture(
                LibraryResult.ofItem(MediaItemTree.getRootItem(), params),
                MoreExecutors.directExecutor()
        );
    }
    @NonNull
    @Override
    public ListenableFuture<LibraryResult<MediaItem>> onGetItem(
            @NonNull MediaLibraryService.MediaLibrarySession session,
            @NonNull MediaSession.ControllerInfo browser,
            @NonNull String mediaId) {
        MediaItem mediaItem = MediaItemTree.getItem(mediaId);
        if (mediaItem != null) {
            return Futures.immediateFuture(
                    LibraryResult.ofItem(mediaItem, null),
                    MoreExecutors.directExecutor()
            );
        }
        else {
            return Futures.immediateFuture(
                    LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE),
                    MoreExecutors.directExecutor()
            );
        }
    }
    @NonNull
    @Override
    public ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> onGetChildren(
            @NonNull MediaLibraryService.MediaLibrarySession session,
            @NonNull MediaSession.ControllerInfo browser,
            @NonNull String parentId,
            int page,
            int pageSize,
            MediaLibraryService.LibraryParams params) {
        ImmutableList<MediaItem> children = MediaItemTree.getChildren(parentId);
        if (!children.isEmpty()) {
            return Futures.immediateFuture(
                    LibraryResult.ofItemList(children, params),
                    MoreExecutors.directExecutor()
            );
        } else {
            return Futures.immediateFuture(
                    LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE),
                    MoreExecutors.directExecutor()
            );
        }
    }
    @NonNull
    @Override
    public ListenableFuture<List<MediaItem>> onAddMediaItems(
            @NonNull MediaSession mediaSession,
            @NonNull MediaSession.ControllerInfo controller,
            @NonNull List<MediaItem> mediaItems) {
        return Futures.immediateFuture(resolveMediaItems(mediaItems));
    }
    @NonNull
    @Override
    public ListenableFuture<MediaSession.MediaItemsWithStartPosition> onSetMediaItems(
            @NonNull MediaSession mediaSession,
            @NonNull MediaSession.ControllerInfo browser,
            @NonNull List<MediaItem> mediaItems,
            int startIndex,
            long startPositionMs) {
        if (mediaItems.size() == 1) {
            // Try to expand a single item to a playlist.
            MediaItemsWithStartPosition expandedPlaylist = maybeExpandSingleItemToPlaylist(mediaItems.get(0), startIndex, startPositionMs);
            if (expandedPlaylist != null) {
                return Futures.immediateFuture(expandedPlaylist);
            }
        }
        return Futures.immediateFuture(
                new MediaItemsWithStartPosition(resolveMediaItems(mediaItems), startIndex, startPositionMs),
                MoreExecutors.directExecutor()
        );
    }
    @NonNull
    private List<MediaItem> resolveMediaItems(@NonNull List<MediaItem> mediaItems) {
        List<MediaItem> playlist = new ArrayList<>();
        for (MediaItem mediaItem : mediaItems) {
            if (!mediaItem.mediaId.isEmpty()) {
                MediaItem expandedItem = MediaItemTree.expandItem(mediaItem);
                if (expandedItem != null) {
                    playlist.add(expandedItem);
                }
            } else if (mediaItem.requestMetadata != null && mediaItem.requestMetadata.searchQuery != null) {
                playlist.addAll(MediaItemTree.search(mediaItem.requestMetadata.searchQuery));
            }
        }
        return playlist;
    }

    @OptIn(markerClass = UnstableApi.class)
    private MediaSession.MediaItemsWithStartPosition maybeExpandSingleItemToPlaylist(
            @NonNull MediaItem mediaItem,
            int startIndex,
            long startPositionMs) {
        List<MediaItem> playlist = new ArrayList<>();
        int indexInPlaylist = startIndex;
        MediaItem parentMediaItem = MediaItemTree.getItem(mediaItem.getMediaId());
        if (parentMediaItem != null) {
            if (parentMediaItem.getMediaMetadata().isBrowsable() == Boolean.TRUE) {
                // Get children browsable item.
                playlist = MediaItemTree.getChildren(parentMediaItem.getMediaId());
            } else if (parentMediaItem.getRequestMetadata().getSearchQuery() == null) {
                // Try to get the parent and its children.
                String parentId = MediaItemTree.getParentId(parentMediaItem.getMediaId());
                if (parentId != null) {
                    List<MediaItem> parentChildren = MediaItemTree.getChildren(parentId);
                    for (MediaItem item : parentChildren) {
                        if (item.getMediaId().equals(mediaItem.getMediaId())) {
                            playlist.add(MediaItemTree.expandItem(item));
                        } else {
                            playlist.add(item);
                        }
                    }
                    indexInPlaylist = MediaItemTree.getIndexInMediaItems(parentMediaItem.getMediaId(), playlist);
                }
            }
        }
        if (!playlist.isEmpty()) {
            return new MediaSession.MediaItemsWithStartPosition(ImmutableList.copyOf(playlist), indexInPlaylist, startPositionMs);
        }
        return null;
    }

    @NonNull
    @Override
    public ListenableFuture<LibraryResult<Void>> onSearch(
            @NonNull MediaLibraryService.MediaLibrarySession session,
            @NonNull MediaSession.ControllerInfo browser,
            @NonNull String query,
            MediaLibraryService.LibraryParams params) {
        int searchResultSize = MediaItemTree.search(query).size();
        session.notifySearchResultChanged(browser, query, searchResultSize, params);
        return Futures.immediateFuture(LibraryResult.ofVoid(), MoreExecutors.directExecutor());
    }
    @NonNull
    @Override
    public ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> onGetSearchResult(
            @NonNull MediaLibraryService.MediaLibrarySession session,
            @NonNull MediaSession.ControllerInfo browser,
            @NonNull String query,
            int page,
            int pageSize,
            MediaLibraryService.LibraryParams params) {
        ImmutableList<MediaItem> searchResult = (ImmutableList<MediaItem>) MediaItemTree.search(query);
        return Futures.immediateFuture(
                LibraryResult.ofItemList(searchResult, params),
                MoreExecutors.directExecutor()
        );
    }



}
