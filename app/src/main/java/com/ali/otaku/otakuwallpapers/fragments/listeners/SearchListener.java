package com.ali.otaku.otakuwallpapers.fragments.listeners;

import android.support.annotation.NonNull;

import com.ali.otaku.models.Wallpaper;
import com.ali.otaku.models.WallpaperDirectory;

public interface SearchListener {
    void onSearchItemsPassed(@NonNull OnSearchPassListener searchPassListener);
    void onWallpaperClicked(@NonNull Wallpaper wallpaper);
    void onFolderClicked(@NonNull WallpaperDirectory folder);
}
