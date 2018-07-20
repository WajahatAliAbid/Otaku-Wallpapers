package com.ali.otaku.otakuwallpapers.fragments.listeners;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ali.otaku.models.Wallpaper;
import com.ali.otaku.models.WallpaperDirectory;

import java.util.List;

public interface WallpaperListener {
    void onWallpapersPassed(WallpaperDirectory wallpaperDirectory,@NonNull OnWallpaperPassListener wallpaperPassListener);
    void onWallpaperClicked(@NonNull Wallpaper wallpaper);
}
