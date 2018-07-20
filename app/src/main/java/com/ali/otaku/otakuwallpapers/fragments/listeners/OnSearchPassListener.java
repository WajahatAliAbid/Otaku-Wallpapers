package com.ali.otaku.otakuwallpapers.fragments.listeners;

import android.support.annotation.NonNull;

import com.ali.otaku.models.WallpaperDirectory;
import com.ali.otaku.models.WallpaperItem;

import java.util.List;

public interface OnSearchPassListener {
    void onSearchItemsPassed(String query,List<WallpaperItem> searchItems);
}
