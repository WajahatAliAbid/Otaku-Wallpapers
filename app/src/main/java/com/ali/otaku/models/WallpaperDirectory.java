package com.ali.otaku.models;


public class WallpaperDirectory {
    public String Title;
    public String PreviewUrl;
    public WallpaperDirectory(){

    }
    public WallpaperDirectory(String title){
        Title = title;
    }
    public WallpaperDirectory(String title, String previewUrl) {
        Title = title;
        PreviewUrl = previewUrl;
    }

    @Override
    public boolean equals(Object obj) {
        return ((WallpaperDirectory) obj).Title.equals(Title);
    }
}
