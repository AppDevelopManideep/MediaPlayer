package com.example.mediaplayer.Streaming;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.LibraryResult;
import androidx.media3.session.MediaConstants;
import androidx.media3.session.MediaSession.ControllerInfo;

import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;

import java.io.IOException;

public class AutomotiveService extends DemoPlaybackService {

    @NonNull
    @Override
    protected MediaLibrarySession.Callback createLibrarySessionCallback() throws JSONException, IOException {
        return new DemoMediaLibrarySessionCallback(AutomotiveService.this) {


            @OptIn(markerClass = UnstableApi.class) @NonNull
            @Override
            public ListenableFuture<LibraryResult<MediaItem>> onGetLibraryRoot(
                    @NonNull MediaLibrarySession session,
                    @NonNull ControllerInfo browser,
                    LibraryParams params) {
                LibraryParams responseParams = params;
                if (session.isAutomotiveController(browser)) {
                    // See https://developer.android.com/training/cars/media#apply_content_style
                    LibraryParams rootHintParams = (params != null) ? params : new LibraryParams.Builder().build();
                    rootHintParams.extras.putInt(
                            MediaConstants.EXTRAS_KEY_CONTENT_STYLE_BROWSABLE,
                            MediaConstants.EXTRAS_VALUE_CONTENT_STYLE_GRID_ITEM);
                    rootHintParams.extras.putInt(
                            MediaConstants.EXTRAS_KEY_CONTENT_STYLE_PLAYABLE,
                            MediaConstants.EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM);
                    // Tweaked params are propagated to Automotive browsers as root hints.
                    responseParams = rootHintParams;
                }
                // Use super to return the common library root with the tweaked params sent to the browser.
                return super.onGetLibraryRoot(session, browser, responseParams);
            }
        };
    }
}
