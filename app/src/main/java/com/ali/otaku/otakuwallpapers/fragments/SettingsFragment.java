package com.ali.otaku.otakuwallpapers.fragments;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ali.otaku.otakuwallpapers.MainActivity;
import com.ali.otaku.otakuwallpapers.PrefsKeys;
import com.ali.otaku.otakuwallpapers.R;
import com.codekidlabs.storagechooser.StorageChooser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.function.Consumer;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {


    private Spinner gridColumNumberSpinner;
    private TextView currentStorageDirectory;
    private Button changeDirectoryButton;
    private Switch isNsfwSwitch;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        gridColumNumberSpinner = view.findViewById(R.id.grid_column_spinner);
        currentStorageDirectory = view.findViewById(R.id.current_storage_directory);
        changeDirectoryButton = view.findViewById(R.id.change_storage_directory);
        isNsfwSwitch = view.findViewById(R.id.nsfw);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //this part shows current options, either from shared preferences or from
        //xml files, for reference check bool.xml, strings.xml, integers.xml in res/values
        final SharedPreferences prefs = getActivity().getSharedPreferences(PrefsKeys.SETTINGS, Context.MODE_PRIVATE);
        gridColumNumberSpinner.setSelection(
                Integer.parseInt(prefs.getString(PrefsKeys.GridColumns,
                        getResources().getString(R.string.default_columns))
                )-1);
        String defaultPath = prefs.getString(PrefsKeys.FilePath,
                getResources().getString(R.string.default_storage));
        currentStorageDirectory.setText(defaultPath);

        isNsfwSwitch.setChecked(prefs.getBoolean(PrefsKeys.NSFWContent,
                getResources().getBoolean(R.bool.show_NSFW)));

        isNsfwSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                //updates the value in shared preferences for nsfw
                prefs.edit().putBoolean(PrefsKeys.NSFWContent,isChecked).apply());

        gridColumNumberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int item = position + 1;
                //since spinner items start from 1 so we simply add 1 to index
                //for example if 1 is selected then index = 0 then +1 = 1 column
                prefs.edit().putString(PrefsKeys.GridColumns,"" + item).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //We use a storage chooser and build it with our desired options
        final StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(getActivity())
                .withFragmentManager(getActivity().getFragmentManager())
                .withMemoryBar(true)
                .allowCustomPath(true)
                .showFoldersInGrid(true)
                .withPredefinedPath(defaultPath)
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .build();
        //we show the directory select dialog
        changeDirectoryButton.setOnClickListener(v ->
                //using dexter to get permissions to avoid boilerplate code
                Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        chooser.show();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(getContext(),"Permission denied",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check());
        //we put the selected path to shared preferences and update the information in UI
        chooser.setOnSelectListener(s -> {
            prefs.edit().putString(PrefsKeys.FilePath,s).apply();
            currentStorageDirectory.setText(s);
        });
    }

}
