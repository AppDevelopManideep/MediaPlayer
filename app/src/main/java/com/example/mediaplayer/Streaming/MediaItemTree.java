package com.example.mediaplayer.Streaming;

import android.content.res.AssetManager;
import android.net.Uri;

import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaItem.SubtitleConfiguration;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.util.UnstableApi;

import com.google.common.collect.ImmutableList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

 @UnstableApi public class MediaItemTree {
    private static final Map<String, MediaItemNode> treeNodes = new HashMap<>();
    private static final Map<String, MediaItemNode> titleMap = new HashMap<>();
    private static boolean isInitialized = false;
    private static final String ROOT_ID = "[rootID]";
    private static final String ALBUM_ID = "[albumID]";
    private static final String GENRE_ID = "[genreID]";
    private static final String ARTIST_ID = "[artistID]";
    private static final String ALBUM_PREFIX = "[album]";
    private static final String GENRE_PREFIX = "[genre]";
    private static final String ARTIST_PREFIX = "[artist]";
    private static final String ITEM_PREFIX = "[item]";

    private static class MediaItemNode {
        final MediaItem item;
        final String searchTitle;
        final String searchText;
        final List<MediaItem> children = new ArrayList<>();

        MediaItemNode(MediaItem item) {
            this.item = item;
            searchTitle = normalizeSearchText(item.mediaMetadata.title);
            searchText = new StringBuilder()
                    .append(searchTitle)
                    .append(" ")
                    .append(normalizeSearchText(item.mediaMetadata.subtitle))
                    .append(" ")
                    .append(normalizeSearchText(item.mediaMetadata.artist))
                    .append(" ")
                    .append(normalizeSearchText(item.mediaMetadata.albumArtist))
                    .append(" ")
                    .append(normalizeSearchText(item.mediaMetadata.albumTitle))
                    .toString();
        }

        void addChild(String childID) {
            this.children.add(treeNodes.get(childID).item);
        }

        List<MediaItem> getChildren() {
            return ImmutableList.copyOf(children);
        }
    }

    private static MediaItem buildMediaItem(String title, String mediaId, boolean isPlayable, boolean isBrowsable,
                                            int mediaType, List<SubtitleConfiguration> subtitleConfigurations,
                                            String album, String artist, String genre, Uri sourceUri, Uri imageUri) {
        MediaMetadata metadata =
                new MediaMetadata.Builder()
                        .setAlbumTitle(album)
                        .setTitle(title)
                        .setArtist(artist)
                        .setGenre(genre)
                        .setIsBrowsable(isBrowsable)
                        .setIsPlayable(isPlayable)
                        .setArtworkUri(imageUri)
                        .setMediaType(mediaType)
                        .build();

        return new MediaItem.Builder()
                .setMediaId(mediaId)
                .setSubtitleConfigurations(subtitleConfigurations)
                .setMediaMetadata(metadata)
                .setUri(sourceUri)
                .build();
    }
     private static MediaItem buildMedia(String title, String mediaId, boolean isPlayable, boolean isBrowsable,
                                             int mediaType) {
         MediaMetadata metadata =
                 new MediaMetadata.Builder()

                         .setTitle(title)

                         .setIsBrowsable(isBrowsable)
                         .setIsPlayable(isPlayable)

                         .setMediaType(mediaType)
                         .build();

         return new MediaItem.Builder()
                 .setMediaId(mediaId)

                 .setMediaMetadata(metadata)

                 .build();
     }

    private static String loadJSONFromAsset(AssetManager assets) throws IOException {
        StringBuilder buf = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(assets.open("catalog.json")));
        String str;
        while ((str = in.readLine()) != null) {
            buf.append(str);
        }
        in.close();
        return buf.toString();
    }

    public static void initialize(AssetManager assets) throws IOException, JSONException {
        if (isInitialized) return;
        isInitialized = true;
        // create root and folders for album/artist/genre.
        treeNodes.put(ROOT_ID,
                new MediaItemNode(buildMedia("Root Folder", ROOT_ID, false, true, MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)));
        treeNodes.put(ALBUM_ID,
                new MediaItemNode(buildMedia("Album Folder", ALBUM_ID, false, true, MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS)));
        treeNodes.put(ARTIST_ID,
                new MediaItemNode(buildMedia("Artist Folder", ARTIST_ID, false, true, MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS)));
        treeNodes.put(GENRE_ID,
                new MediaItemNode(buildMedia("Genre Folder", GENRE_ID, false, true, MediaMetadata.MEDIA_TYPE_FOLDER_GENRES)));


        treeNodes.get(ROOT_ID).addChild(ALBUM_ID);
        treeNodes.get(ROOT_ID).addChild(ARTIST_ID);
        treeNodes.get(ROOT_ID).addChild(GENRE_ID);

        // Here, parse the json file in asset for media list.
        // We use a file in asset for demo purpose
        JSONObject jsonObject = new JSONObject(loadJSONFromAsset(assets));
        JSONArray mediaList = jsonObject.getJSONArray("media");

        // create subfolder with same artist, album, etc.
        for (int i = 0; i < mediaList.length(); i++) {
            addNodeToTree(mediaList.getJSONObject(i));
        }
    }

    private static void addNodeToTree(JSONObject mediaObject) throws JSONException {
        String id = mediaObject.getString("id");
        String album = mediaObject.getString("album");
        String title = mediaObject.getString("title");
        String artist = mediaObject.getString("artist");
        String genre = mediaObject.getString("genre");
        List<SubtitleConfiguration> subtitleConfigurations = new ArrayList<>();
        if (mediaObject.has("subtitles")) {
            JSONArray subtitlesJson = mediaObject.getJSONArray("subtitles");
            for (int i = 0; i < subtitlesJson.length(); i++) {
                JSONObject subtitleObject = subtitlesJson.getJSONObject(i);
                subtitleConfigurations.add(
                        new SubtitleConfiguration.Builder(Uri.parse(subtitleObject.getString("subtitle_uri")))
                                .setMimeType(subtitleObject.getString("subtitle_mime_type"))
                                .setLanguage(subtitleObject.getString("subtitle_lang"))
                                .build()
                );
            }
        }
        Uri sourceUri = Uri.parse(mediaObject.getString("source"));
        Uri imageUri = Uri.parse(mediaObject.getString("image"));
        // key of such items in tree
        String idInTree = ITEM_PREFIX + id;
        String albumFolderIdInTree = ALBUM_PREFIX + album;
        String artistFolderIdInTree = ARTIST_PREFIX + artist;
        String genreFolderIdInTree = GENRE_PREFIX + genre;

        treeNodes.put(idInTree,
                new MediaItemNode(buildMediaItem(title, idInTree, true, false, MediaMetadata.MEDIA_TYPE_MUSIC,
                        subtitleConfigurations, album, artist, genre, sourceUri, imageUri)));

        titleMap.put(title.toLowerCase(), treeNodes.get(idInTree));

        if (!treeNodes.containsKey(albumFolderIdInTree)) {
            treeNodes.put(albumFolderIdInTree,
                    new MediaItemNode(buildMediaItem(album, albumFolderIdInTree, true, true,
                            MediaMetadata.MEDIA_TYPE_ALBUM, subtitleConfigurations, null, null, null, null, imageUri)));
            treeNodes.get(ALBUM_ID).addChild(albumFolderIdInTree);
        }
        treeNodes.get(albumFolderIdInTree).addChild(idInTree);

        // add into artist folder
        if (!treeNodes.containsKey(artistFolderIdInTree)) {
            treeNodes.put(artistFolderIdInTree,
                    new MediaItemNode(buildMediaItem(artist, artistFolderIdInTree, true, true,
                            MediaMetadata.MEDIA_TYPE_ARTIST, subtitleConfigurations, null, null, null, null, null)));
            treeNodes.get(ARTIST_ID).addChild(artistFolderIdInTree);
        }
        treeNodes.get(artistFolderIdInTree).addChild(idInTree);

        // add into genre folder
        if (!treeNodes.containsKey(genreFolderIdInTree)) {
            treeNodes.put(genreFolderIdInTree,
                    new MediaItemNode(buildMediaItem(genre, genreFolderIdInTree, true, true,
                            MediaMetadata.MEDIA_TYPE_GENRE, subtitleConfigurations, null, null, null, null, null)));
            treeNodes.get(GENRE_ID).addChild(genreFolderIdInTree);
        }
        treeNodes.get(genreFolderIdInTree).addChild(idInTree);
    }

    public static MediaItem getItem(String id) {
        return treeNodes.containsKey(id) ? treeNodes.get(id).item : null;
    }

    public static MediaItem expandItem(MediaItem item) {
        MediaItemNode treeItem = treeNodes.get(item.mediaId);
        if (treeItem == null) return null;
        MediaMetadata.Builder metadataBuilder = treeItem.item.mediaMetadata.buildUpon()
                .populate(item.mediaMetadata);
        MediaMetadata metadata = metadataBuilder.build();
        return item.buildUpon()
                .setMediaMetadata(metadata)
                .setSubtitleConfigurations(treeItem.item.localConfiguration != null ?
                        treeItem.item.localConfiguration.subtitleConfigurations : new ArrayList<>())
                .setUri(treeItem.item.localConfiguration != null ? treeItem.item.localConfiguration.uri : null)
                .build();
    }

    public static String getParentId(String mediaId) {
        return getParentId(mediaId, ROOT_ID);
    }

    private static String getParentId(String mediaId, String parentId) {
        MediaItemNode parentNode = treeNodes.get(parentId);
        if (parentNode == null) return null;
        for (MediaItem child : parentNode.children) {
            if (child.mediaId.equals(mediaId)) {
                return parentId;
            } else if (child.mediaMetadata.isBrowsable == true) {
                String nextParentId = getParentId(mediaId, child.mediaId);
                if (nextParentId != null) {
                    return nextParentId;
                }
            }
        }
        return null;
    }

    public static int getIndexInMediaItems(String mediaId, List<MediaItem> mediaItems) {
        for (int i = 0; i < mediaItems.size(); i++) {
            if (mediaItems.get(i).mediaId.equals(mediaId)) {
                return i;
            }
        }
        return 0;
    }

    public static List<MediaItem> search(String query) {
        List<MediaItem> matches = new ArrayList<>();
        List<MediaItem> titleMatches = new ArrayList<>();
        List<String> words = new ArrayList<>();
        for (String word : query.split(" ")) {
            String trimmedWord = word.trim().toLowerCase();
            if (trimmedWord.length() > 1) {
                words.add(trimmedWord);
            }
        }
        for (String title : titleMap.keySet()) {
            MediaItemNode mediaItemNode = titleMap.get(title);
            for (String word : words) {
                if (mediaItemNode.searchText.contains(word)) {
                    if (mediaItemNode.searchTitle.contains(query.toLowerCase())) {
                        titleMatches.add(mediaItemNode.item);
                    } else {
                        matches.add(mediaItemNode.item);
                    }
                    break;
                }
            }
        }
        titleMatches.addAll(matches);
        return titleMatches;
    }

    public static MediaItem getRootItem() {
        return treeNodes.get(ROOT_ID).item;
    }

    public static List<MediaItem> getChildren(String id) {
        return treeNodes.containsKey(id) ? treeNodes.get(id).getChildren() : new ArrayList<>();
    }

    private static String normalizeSearchText(CharSequence text) {
        if (text == null || text.length() == 0 || text.toString().trim().length() == 1) {
            return "";
        }
        return text.toString().trim().toLowerCase();
    }
}
