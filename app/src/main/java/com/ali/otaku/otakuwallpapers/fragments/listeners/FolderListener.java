package com.ali.otaku.otakuwallpapers.fragments.listeners;

import android.support.annotation.NonNull;

import com.ali.otaku.models.WallpaperDirectory;

import java.util.List;

public interface FolderListener {
    void onFolderClicked(@NonNull WallpaperDirectory folder);
    void onFoldersPassed(@NonNull OnFolderPassListener listener);
}
