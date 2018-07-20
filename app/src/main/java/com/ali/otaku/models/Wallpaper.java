package com.ali.otaku.models;

public class Wallpaper {
    public String CharacterName;
    public boolean IsNotSafeForWork;
    public String Url;
    public String Category;

    public Wallpaper(){

    }

    public Wallpaper(String characterName, boolean isNotSafeForWork, String url, String category) {
        CharacterName = characterName;
        IsNotSafeForWork = isNotSafeForWork;
        Url = url;
        Category = category;
    }
}
