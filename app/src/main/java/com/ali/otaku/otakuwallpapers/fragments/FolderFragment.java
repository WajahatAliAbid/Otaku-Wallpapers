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
import android.widget.AdapterView;
import android.widget.GridView;

import com.ali.otaku.models.WallpaperDirectory;
import com.ali.otaku.otakuwallpapers.PrefsKeys;
import com.ali.otaku.otakuwallpapers.R;
import com.ali.otaku.otakuwallpapers.adapters.FolderAdapter;
import com.ali.otaku.otakuwallpapers.fragments.listeners.AddWallpaperClickListener;
import com.ali.otaku.otakuwallpapers.fragments.listeners.FolderListener;
import com.ali.otaku.otakuwallpapers.fragments.listeners.OnFolderPassListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FolderFragment extends Fragment {

    private GridView folderGrid;
    private FolderAdapter adapter;

    private FolderListener folderListener;

    private FloatingActionButton addWallpaperButton;
    private AddWallpaperClickListener addWallpaperClickListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Setts folderGrid and other UI related options from layout file
        View view = inflater.inflate(R.layout.fragment_folder, container, false);
        folderGrid = view.findViewById(R.id.folder_grid);
        addWallpaperButton = view.findViewById(R.id.add_wallpaper);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //We either get default stores in strings.txt or from shared preferences.
        //When we modify values, those are stored in shared preferences, and we get those
        //if we do not change default options then it gets from default values in xml files
        final SharedPreferences prefs = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        int numColumns = Integer.parseInt(prefs.getString(PrefsKeys.GridColumns,
                getResources().getString(R.string.default_columns)));
        //sets number of columns, adapter for folder grid. We are using FolderAdapter here
        folderGrid.setNumColumns(numColumns);

        adapter = new FolderAdapter(getContext(),new ArrayList<WallpaperDirectory>());
        folderGrid.setAdapter(adapter);

        //Checks if listener is passed to it and is not null then executes the code here
        //which is adds folders to the grid View
        if(folderListener!=null){
            folderListener.onFoldersPassed(directories -> {
                adapter.clear();
                adapter.addAll(directories);
            });
            folderGrid.setOnItemClickListener((parent, view, position, id) -> {
                //Gets item from grid view that is clicked and passes it to listener
                WallpaperDirectory wallpaperDirectory = adapter.getItem(position);
                //we pass clicked folder to whoever implements the listener
                //in our case it is main activity
                //event is sent to main activity and it handles the click action
                folderListener.onFolderClicked(wallpaperDirectory);
            });
        }
        //this part handles the onClick listener for floating Action button (+)
        //and passes to whoever implements the listener, in our case it is mainActivity
        if(addWallpaperClickListener!=null){
            addWallpaperButton.setOnClickListener(v -> addWallpaperClickListener.onClickAdd(null));
        }



    }

    //These two methods are used to pass listener to the fragment
    public void setFolderListener(@NonNull FolderListener folderListener){
        this.folderListener = folderListener;
    }
    public void setAddButtonClickListener(@NonNull AddWallpaperClickListener addButtonClickListener){
        this.addWallpaperClickListener = addButtonClickListener;
    }
}
