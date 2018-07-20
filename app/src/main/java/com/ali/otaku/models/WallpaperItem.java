package com.ali.otaku.models;

public class WallpaperItem {
    public String CharacterName;
    public boolean IsNotSafeForWork;
    public String Url;
    public String Category;
    public String Title;

    public WallpaperItem(){

    }

    public WallpaperItem(String characterName, boolean isNotSafeForWork, String url,
                         String category, String title) {
        CharacterName = characterName;
        IsNotSafeForWork = isNotSafeForWork;
        Url = url;
        Category = category;
        Title = title;
    }
}
