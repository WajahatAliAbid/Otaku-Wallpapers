package com.ali.otaku.otakuwallpapers.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ali.otaku.models.Wallpaper;
import com.ali.otaku.otakuwallpapers.R;
import com.koushikdutta.ion.Ion;

import java.util.List;

public class WallpaperAdapter extends ArrayAdapter<Wallpaper> {
    private final int mResource;
    public WallpaperAdapter(@NonNull Context context, @NonNull List<Wallpaper> objects) {
        super(context, R.layout.wallpaper_item, objects);
        mResource = R.layout.wallpaper_item;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(mResource,parent,false);
        ImageView wallpaperImage = rowView.findViewById(R.id.wallpaper_image);
        TextView wallpaperTitle = rowView.findViewById(R.id.wallpaper_title);
        Wallpaper wallpaper = getItem(position);
        wallpaperTitle.setText(wallpaper.CharacterName);
        Ion.with(wallpaperImage)
                .placeholder(R.drawable.image)
                .error(R.drawable.image_broken_variant)
                .load(wallpaper.Url);
        return rowView;
    }
}
