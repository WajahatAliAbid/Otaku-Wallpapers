package com.ali.otaku.otakuwallpapers.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;

import com.ali.otaku.models.Wallpaper;
import com.ali.otaku.models.WallpaperDirectory;
import com.ali.otaku.otakuwallpapers.PrefsKeys;
import com.ali.otaku.otakuwallpapers.R;
import com.ali.otaku.otakuwallpapers.adapters.WallpaperAdapter;
import com.ali.otaku.otakuwallpapers.fragments.listeners.AddWallpaperClickListener;
import com.ali.otaku.otakuwallpapers.fragments.listeners.WallpaperListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class WallpaperFragment extends Fragment {

    public static String FOLDER_TITLE = "Title";
    public static String FOLDER_PREVIEW = "PREVIEW";

    private GridView wallpaperGrid;
    private WallpaperAdapter adapter;

    private WallpaperListener wallpaperListener;

    private TextView folderTitle;
    private CheckBox isFavorite;

    private SharedPreferences sharedPreferences;


    private FloatingActionButton addWallpaperButton;
    private AddWallpaperClickListener addWallpaperClickListener;

    private WallpaperDirectory wallpaperDirectory;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wallpaper, container, false);
        folderTitle = view.findViewById(R.id.folder_title);
        isFavorite = view.findViewById(R.id.is_favorite);
        wallpaperGrid = view.findViewById(R.id.grid_view);
        addWallpaperButton = view.findViewById(R.id.add_wallpaper);

        //We take arguments passed to the fragment and set wallpaperDirectory Object for this fragment
        //and set title to whatever wallpaper Directory title is
        wallpaperDirectory = new WallpaperDirectory();
        wallpaperDirectory.Title = getArguments().getString(FOLDER_TITLE);
        wallpaperDirectory.PreviewUrl = getArguments().getString(FOLDER_PREVIEW);
        folderTitle.setText(wallpaperDirectory.Title);

        //we get sharedPreference Object for both settings and favorites
        sharedPreferences =getActivity().getSharedPreferences(PrefsKeys.SETTINGS,Context.MODE_PRIVATE);
        SharedPreferences favPreferences = getActivity().getSharedPreferences(PrefsKeys.FAVORITES,Context.MODE_PRIVATE);

        //we check if current folder is favorited then we set isFavorite to checked

        isFavorite.setChecked(favPreferences.contains(wallpaperDirectory.Title));
        isFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isFavorite.setEnabled(false);
            //we update the value whether or not isFavorite is checked
            if(isChecked){
                favPreferences.edit().putString(wallpaperDirectory.Title,wallpaperDirectory.PreviewUrl).apply();
            }else{
                favPreferences.edit().remove(wallpaperDirectory.Title).apply();
            }
            isFavorite.setEnabled(true);
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Here we set number of columns for grid View and set its adapter
        int numColumns = Integer.parseInt(sharedPreferences.getString(PrefsKeys.GridColumns,
                getResources().getString(R.string.default_columns)));
        wallpaperGrid.setNumColumns(numColumns);

        adapter = new WallpaperAdapter(getContext(),new ArrayList<>());
        wallpaperGrid.setAdapter(adapter);

        //we pass wallpaperDirectory to whoever implements the listener
        //and take all the wallpapers and add it to adapter of wallpaper
        if(wallpaperListener!=null){
            wallpaperListener.onWallpapersPassed(wallpaperDirectory, wallpapers -> {
                adapter.clear();
                adapter.addAll(wallpapers);
            });
            //we set item click listener for grid view and pass wallpaper to
            //whoever implements the listenr
            wallpaperGrid.setOnItemClickListener((parent, view, position, id) -> {
                Wallpaper wallpaper = adapter.getItem(position);
                wallpaperListener.onWallpaperClicked(wallpaper);
            });
        }
        //add floating action button listener passes wallpaper Directory to whoever
        //implements the listner
        if(addWallpaperClickListener!=null){
            addWallpaperButton.setOnClickListener(v -> addWallpaperClickListener.onClickAdd(wallpaperDirectory));
        }
    }
    //sets listeners here
    public void setWallpaperListener(@NonNull WallpaperListener wallpaperListener){
        this.wallpaperListener = wallpaperListener;
    }
    public void setAddButtonClickListener(@NonNull AddWallpaperClickListener addButtonClickListener){
        this.addWallpaperClickListener = addButtonClickListener;
    }
}
