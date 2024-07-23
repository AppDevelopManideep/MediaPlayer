package com.example.mediaplayer.Streaming;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BitmapContentProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        Context context = getContext();
        if (context != null) {
            String assetPath = getAssetPath(uri);
            if (assetPath != null) {
                try {
                    File file = copyAssetFileToCacheDirectory(context, assetPath);
                    return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return super.openFile(uri, mode);
    }

    private String getAssetPath(Uri contentUri) {
        String path = contentUri.getPath();
        if (path != null) {
            return path.substring(1); // Remove the leading '/'
        }
        return null;
    }

    private File copyAssetFileToCacheDirectory(Context context, String assetPath) throws IOException {
        File outFile = new File(context.getCacheDir(), assetPath.replace("/", "_"));
        if (!outFile.exists()) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = context.getAssets().open(assetPath);
                out = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        return outFile;
    }

    // No-op implementations of abstract ContentProvider methods.

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}
