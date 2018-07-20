package com.ali.otaku.otakuwallpapers.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.ali.otaku.models.Wallpaper;
import com.ali.otaku.models.WallpaperDirectory;
import com.ali.otaku.models.WallpaperItem;
import com.ali.otaku.otakuwallpapers.PrefsKeys;
import com.ali.otaku.otakuwallpapers.R;
import com.ali.otaku.otakuwallpapers.adapters.FolderAdapter;
import com.ali.otaku.otakuwallpapers.adapters.WallpaperAdapter;
import com.ali.otaku.otakuwallpapers.fragments.listeners.FolderListener;
import com.ali.otaku.otakuwallpapers.fragments.listeners.SearchListener;
import com.ali.otaku.otakuwallpapers.fragments.listeners.WallpaperListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    //This fragment works similarly to WallpaperFragment listener
    //For reference check that fragment
    //Almost all of the code is same as that one

    private GridView folderGrid,wallpaperGrid;

    private WallpaperAdapter wallpaperAdapter;
    private FolderAdapter folderAdapter;

    private SearchListener searchListener;

    private SharedPreferences sharedPreferences;

    private TextView folderText,wallpaperText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        wallpaperGrid = view.findViewById(R.id.search_grid_wallpapers);
        folderGrid = view.findViewById(R.id.search_grid_folders);
        wallpaperText = view.findViewById(R.id.wallpaper_text);
        folderText = view.findViewById(R.id.folder_text);
        sharedPreferences =getActivity().getSharedPreferences(PrefsKeys.SETTINGS, Context.MODE_PRIVATE);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int numColumns = Integer.parseInt(sharedPreferences.getString(PrefsKeys.GridColumns,
                getResources().getString(R.string.default_columns)));
        wallpaperGrid.setNumColumns(numColumns);
        folderGrid.setNumColumns(numColumns);

        wallpaperAdapter = new WallpaperAdapter(getContext(),new ArrayList<>());
        wallpaperGrid.setAdapter(wallpaperAdapter);
        folderAdapter = new FolderAdapter(getContext(),new ArrayList<>());
        folderGrid.setAdapter(folderAdapter);
        if(searchListener!=null){

            searchListener.onSearchItemsPassed((query, searchItems) -> {
                Log.d("Query",searchItems.size()+"");
                folderAdapter.clear();
                wallpaperAdapter.clear();
                ArrayList<WallpaperDirectory> directories = new ArrayList<>();
                ArrayList<Wallpaper> wallpapers = new ArrayList<>();
                for (WallpaperItem item:searchItems){
                    if(containsSubString(item.Title,query)){
                        WallpaperDirectory wallpaperDirectory = new WallpaperDirectory(item.Title,
                                item.Url);
                        if(!directories.contains(wallpaperDirectory)){
                            directories.add(wallpaperDirectory);
                        }
                        Log.d("Query","Added folder");
                    }
                    if(containsSubString(item.CharacterName,query)){
                        wallpapers.add(new Wallpaper(item.CharacterName,item.IsNotSafeForWork,
                                item.Url,item.Category));
                        Log.d("Query","Added wallpaper");
                    }
                }
                if(directories.isEmpty()){
                    folderText.setVisibility(View.GONE);
                }else {
                    folderText.setVisibility(View.VISIBLE);
                }
                if(wallpapers.isEmpty()){
                    wallpaperText.setVisibility(View.GONE);
                }else{
                    wallpaperText.setVisibility(View.VISIBLE);
                }
                if(directories.isEmpty()&& wallpapers.isEmpty()){
                    Toast.makeText(getContext(),"No items in search result",Toast.LENGTH_SHORT).show();
                }
                folderAdapter.addAll(directories);
                wallpaperAdapter.addAll(wallpapers);
            });

            folderGrid.setOnItemClickListener((parent, view, position, id) -> {
                //Gets item from grid view that is clicked and passes it to listener
                WallpaperDirectory wallpaperDirectory = folderAdapter.getItem(position);
                //we pass clicked folder to whoever implements the listener
                //in our case it is main activity
                //event is sent to main activity and it handles the click action
                searchListener.onFolderClicked(wallpaperDirectory);
            });
            wallpaperGrid.setOnItemClickListener((parent, view, position, id) -> {
                Wallpaper wallpaper = wallpaperAdapter.getItem(position);
                searchListener.onWallpaperClicked(wallpaper);
            });
        }
    }
    public void setSearchPassListener(@NonNull SearchListener searchListener){
        this.searchListener = searchListener;
    }

    private boolean containsSubString(String string1, String string2){
        return string1.toLowerCase().contains(string2.toLowerCase());
    }

}
