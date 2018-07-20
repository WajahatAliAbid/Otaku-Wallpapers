package com.ali.otaku.otakuwallpapers.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ali.otaku.models.WallpaperDirectory;
import com.ali.otaku.otakuwallpapers.R;
import com.koushikdutta.ion.Ion;

import java.util.List;

public class FolderAdapter extends ArrayAdapter<WallpaperDirectory> {
    private final int mResource;
    public FolderAdapter(@NonNull Context context, @NonNull List<WallpaperDirectory> objects) {
        super(context, R.layout.folder_item, objects);
        mResource = R.layout.folder_item;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(mResource,parent,false);
        TextView titleView = rowView.findViewById(R.id.folder_title);
        ImageView imageView = rowView.findViewById(R.id.folder_icon);
        WallpaperDirectory item = getItem(position);
        titleView.setText(item.Title);
        Ion.with(imageView)
                .placeholder(R.drawable.folder_multiple_image)
                .error(R.drawable.folder_multiple_image)
                .load(item.PreviewUrl);
        return  rowView;
    }
}
