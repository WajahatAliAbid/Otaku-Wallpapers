package com.ali.otaku.otakuwallpapers;

import android.Manifest;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.ChangeBounds;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ali.otaku.models.FirebaseReferences;
import com.ali.otaku.models.Wallpaper;
import com.ali.otaku.models.WallpaperDirectory;
import com.ali.otaku.models.WallpaperItem;
import com.ali.otaku.otakuwallpapers.fragments.FolderFragment;
import com.ali.otaku.otakuwallpapers.fragments.SearchFragment;
import com.ali.otaku.otakuwallpapers.fragments.SettingsFragment;
import com.ali.otaku.otakuwallpapers.fragments.WallpaperFragment;
import com.ali.otaku.otakuwallpapers.fragments.listeners.AddWallpaperClickListener;
import com.ali.otaku.otakuwallpapers.fragments.listeners.FolderListener;
import com.ali.otaku.otakuwallpapers.fragments.listeners.OnFolderPassListener;
import com.ali.otaku.otakuwallpapers.fragments.listeners.OnSearchPassListener;
import com.ali.otaku.otakuwallpapers.fragments.listeners.OnWallpaperPassListener;
import com.ali.otaku.otakuwallpapers.fragments.listeners.SearchListener;
import com.ali.otaku.otakuwallpapers.fragments.listeners.WallpaperListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.koushikdutta.ion.Ion;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, AddWallpaperClickListener, WallpaperListener, FolderListener {

    private FloatingActionButton searchButton;
    private FirebaseDatabase firebaseDatabase;

    ConstraintLayout mainConstraintLayout;

    private FolderFragment mainFolderFragment, favoriteFolderFragment;

    private SettingsFragment settingsFragment;
    private EditText searchText;
    private SharedPreferences settingsPrefs;
    private SearchFragment searchFragment;

    private Fragment activeFragment;

    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchText = findViewById(R.id.search_text);
        searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(this);


        mainConstraintLayout = findViewById(R.id.mainContainer);

        //Initializing firebase parameters
        FirebaseApp firebaseApp = FirebaseApp.initializeApp(this);
        assert firebaseApp != null;
        firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);

        settingsPrefs = getSharedPreferences(PrefsKeys.SETTINGS, Context.MODE_PRIVATE);


        //Bottom navigation view listener
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        searchText.setOnEditorActionListener((v, actionId, event) -> {
            doSearch();
            return true;
        });
        navigation.setSelectedItemId(R.id.navigation_home);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            clearBackStack();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //we show app bar using this method
                    animateToActivityWithAppBar();
                    //if for memory limiation, mainFolderFragment becomes null
                    //we initiate it again in order to avoid runtime errors
                    if (mainFolderFragment == null) {
                        mainFolderFragment = new FolderFragment();
                        mainFolderFragment.setAddButtonClickListener(MainActivity.this);
                        mainFolderFragment.setFolderListener(MainActivity.this);
                    }
                    activeFragment = mainFolderFragment;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, mainFolderFragment)
                            .commit();
                    //we replace the fragment in container with mainFolderFragment

                    return true;
                case R.id.navigation_favorites:
                    animateToActivityWithAppBar();
                    //checks if favoriteFolderFragment is null
                    //if yes then initialize it and set fragment in container
                    if (favoriteFolderFragment == null) {
                        favoriteFolderFragment = new FolderFragment();
                        favoriteFolderFragment.setFolderListener(new FolderListener() {
                            @Override
                            public void onFolderClicked(@NonNull WallpaperDirectory folder) {
                                MainActivity.this.onFolderClicked(folder);
                            }

                            @Override
                            public void onFoldersPassed(@NonNull OnFolderPassListener listener) {
                                //we take favorites in shared preferences and pass it to onFolderPassListener
                                SharedPreferences prefs = getSharedPreferences(PrefsKeys.FAVORITES, Context.MODE_PRIVATE);
                                Map<String, ?> map = prefs.getAll();
                                Set<String> set = map.keySet();
                                Iterator<String> iterator = set.iterator();
                                ArrayList<WallpaperDirectory> list = new ArrayList<>(set.size());
                                while (iterator.hasNext()) {
                                    //iterates through keys of favorites which are folder titles
                                    String key = iterator.next();
                                    list.add(new WallpaperDirectory(key, map.get(key).toString()));
                                }
                                listener.onFoldersPassed(list);
                            }
                        });
                        favoriteFolderFragment.setAddButtonClickListener(MainActivity.this);
                    }
                    activeFragment = favoriteFolderFragment;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, favoriteFolderFragment)
                            .commit();

                    return true;
                case R.id.navigation_settings:
                    //hide app bar and then set settings Fragment in container
                    animateToActivityWithoutAppBar();
                    if (settingsFragment == null) {
                        settingsFragment = new SettingsFragment();
                    }
                    activeFragment = settingsFragment;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, settingsFragment)
                            .commit();

                    return true;
            }
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        if(activeFragment==null){
            activeFragment = favoriteFolderFragment;
//            if(getSupportFragmentManager().getBackStackEntryCount()==1){
//                activeFragment = favoriteFolderFragment;
//                Log.d("Backstack","to fav fragment");
//            }
            super.onBackPressed();
        }else if(activeFragment.equals(favoriteFolderFragment)||
                activeFragment.equals(settingsFragment)||
                activeFragment.equals(searchFragment)){
            //sets active fragment to main fragment
            navigation.setSelectedItemId(R.id.navigation_home);
        }else{
            clearBackStack();
            super.onBackPressed();
        }
    }

    //This is view onClick listener
    @Override
    public void onClick(View v) {
        if (v.equals(searchButton)) {
            doSearch();
        }
    }

    private void doSearch() {
        String query = searchText.getText().toString().toLowerCase();
        if (TextUtils.isEmpty(query)) {
            Toast.makeText(this, "Please enter search query", Toast.LENGTH_SHORT).show();
        }
        //it searches the query in database, matches with CharacterName and
        //returns the wallpapers with title as query
        boolean isNSFWAllowed = settingsPrefs.getBoolean(PrefsKeys.NSFWContent, getResources().getBoolean(R.bool.show_NSFW));

        if (isNSFWAllowed) {
            firebaseDatabase.getReference(FirebaseReferences.WALLPAPER_REF)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            handleSearchData(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } else {
            firebaseDatabase.getReference(FirebaseReferences.WALLPAPER_REF)
                    .orderByChild("IsNotSafeForWork")
                    .equalTo(false)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            handleSearchData(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    private void handleSearchData(DataSnapshot dataSnapshot) {
        String query = searchText.getText().toString().trim().toLowerCase();
        searchText.setText("");
        if (!dataSnapshot.exists()) {
            return;
        }
        Log.d("Query", dataSnapshot.toString());
        if (searchFragment == null) {
            searchFragment = new SearchFragment();
        }
        activeFragment = searchFragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, searchFragment)
                .commitAllowingStateLoss();
        searchFragment.setSearchPassListener(new SearchListener() {
            @Override
            public void onSearchItemsPassed(@NonNull OnSearchPassListener searchPassListener) {
                final ArrayList<WallpaperItem> wallpaperItems = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    wallpaperItems.add(data.getValue(WallpaperItem.class));
                }
                searchPassListener.onSearchItemsPassed(query, wallpaperItems);
            }

            @Override
            public void onWallpaperClicked(@NonNull Wallpaper wallpaper) {
                MainActivity.this.onWallpaperClicked(wallpaper);
            }

            @Override
            public void onFolderClicked(@NonNull WallpaperDirectory folder) {
                MainActivity.this.onFolderClicked(folder);
            }
        });

    }

    //This listens to click on plus button
    @Override
    public void onClickAdd(@Nullable WallpaperDirectory wallpaperDirectory) {
        Intent intent = new Intent(this, AddWallpaperActivity.class);
        //when we click add, we check if wallpaperDirectory is passed or not, if yes then pass title to it
        if (wallpaperDirectory != null) {
            intent.putExtra(AddWallpaperActivity.DIRECTORY, wallpaperDirectory.Title);
        }
        startActivity(intent);
    }

    //It basically loads wallpapers with wallpaperDirectory to the grid view and passes it to
    //the WallpaperFragment in our case
    @Override
    public void onWallpapersPassed(WallpaperDirectory wallpaperDirectory,
                                   @NonNull final OnWallpaperPassListener wallpaperPassListener) {
        if (wallpaperDirectory != null) {
            firebaseDatabase.getReference()
                    .child(FirebaseReferences.WALLPAPER_REF)
                    .orderByChild("Title")
                    .equalTo(wallpaperDirectory.Title)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final ArrayList<Wallpaper> wallpapers = new ArrayList<>();

                            boolean isNSFWAllowed = settingsPrefs.getBoolean(PrefsKeys.NSFWContent, getResources().getBoolean(R.bool.show_NSFW));
                            if (isNSFWAllowed) {
                                for (DataSnapshot item : dataSnapshot.getChildren()) {
                                    Wallpaper wallpaperItem = item.getValue(Wallpaper.class);
                                    wallpapers.add(wallpaperItem);
                                }
                            } else {
                                for (DataSnapshot item : dataSnapshot.getChildren()) {
                                    Wallpaper wallpaperItem = item.getValue(Wallpaper.class);
                                    if (!wallpaperItem.IsNotSafeForWork) {
                                        wallpapers.add(wallpaperItem);
                                    }
                                }
                            }

                            wallpaperPassListener.onWallpapersPassed(wallpapers);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

    }


    //passes wallpaper on clicking on wallpaper
    @Override
    public void onWallpaperClicked(@NonNull Wallpaper wallpaper) {
        //Here we create a full screen dialog to show the wallpaper download screen
        Dialog dialog = new Dialog(this, R.style.DialogFullscreen);
        dialog.setContentView(R.layout.layout_wallpaper_view);
        TextView titleView = dialog.findViewById(R.id.image_title);
        TextView imageResolutionText = dialog.findViewById(R.id.image_resolution);
        Spinner resolutionSelector = dialog.findViewById(R.id.resolution_selector);

        ImageView wallpaperPreview = dialog.findViewById(R.id.wallpaper_preview_image);
        Button downloadButton = dialog.findViewById(R.id.download_button);
        ProgressBar progressBar = dialog.findViewById(R.id.download_progress);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        resolutionSelector.setAdapter(adapter);
        titleView.setText(wallpaper.CharacterName);
        AtomicReference<Resolution> requestedSize = new AtomicReference<>();
        AtomicReference<Resolution> imageResolution = new AtomicReference<>();
        AtomicReference<Bitmap> imageBitmap = new AtomicReference<>();
        String tempFile = "Temp" + wallpaper.CharacterName+wallpaper.Category+".jpg";
        Ion.with(this).load(wallpaper.Url)
                .progressBar(progressBar)
                .write(new File(getCacheDir(), tempFile))
                .setCallback((exception, file) -> {
                    //We get the full quality file from server and show progress of loading on screen
                    //with progressBar
                    resolutionSelector.setSelection(0);
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                    imageBitmap.set(bitmap);
                    wallpaperPreview.setImageBitmap(bitmap);
                    resolutionSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selection = resolutionSelector.getSelectedItem().toString();
                            if(selection.equals(ResolutionOptions.ORIGINAL_RESOLUTION)){
                                wallpaperPreview.setImageBitmap(bitmap);
                            }else{
                                Resolution resolution = getResolution(selection);
                                requestedSize.set(getResizeResolution(bitmap,resolution));
//                                Toast.makeText(MainActivity.this,"Request " + requestedSize.get().width +" " + requestedSize.get().height,Toast.LENGTH_SHORT).show();
                                Ion.with(wallpaperPreview)
                                        .resize(requestedSize.get().width,requestedSize.get().height)
                                        .load(file.getPath());

                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    imageResolutionText.setText("(" + bitmap.getWidth()+" x " + bitmap.getHeight() +")");
                    imageResolution.set(new Resolution(bitmap.getWidth(), bitmap.getHeight()));
                    adapter.addAll(imageResolution.get().getResolutionArray());
                    downloadButton.setVisibility(View.VISIBLE);
                    resolutionSelector.setVisibility(View.VISIBLE);

                    //after image is downloaded do downloadButton stuff
                });

//        when clicked on downloadButton it takes the path and stores file there
        downloadButton.setOnClickListener(v -> {
            //first checks permission
            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            String selectedItem = resolutionSelector.getSelectedItem().toString();
                            SharedPreferences prefs = getSharedPreferences(PrefsKeys.SETTINGS, Context.MODE_PRIVATE);
                            String defaultPath = prefs.getString(PrefsKeys.FilePath,
                                    getResources().getString(R.string.default_storage));
                            File destinationPath = new File(defaultPath);
                            if (!destinationPath.exists()) {
                                destinationPath.mkdir();
                            }
                            //we set file name
                            String fileName = wallpaper.Category + new Date().getTime() + ".jpg";
                            File destinationFile = new File(destinationPath, fileName);


                            Resolution destinationResolution;
                            //we handle resolution related things here and save files to location provided
                            if (selectedItem.equals(ResolutionOptions.ORIGINAL_RESOLUTION)) {
                                //when it is original resolution then we simply download file to
                                //the location stored in sharedPreferences
                                Ion.with(MainActivity.this)
                                        .load(wallpaper.Url)
                                        .progressBar(progressBar)
                                        .write(destinationFile)
                                        .setCallback((e, result) -> {
                                            if (e != null) {
                                                if (e.getMessage() != null) {
                                                    Toast.makeText(MainActivity.this, "Exception" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                } else
                                                    Toast.makeText(MainActivity.this, "Exception", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(MainActivity.this, result.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                destinationResolution = getResolution(selectedItem);
                                Resolution desiredSize =requestedSize.get();
                                Bitmap originalBitmap = imageBitmap.get();
                                Bitmap workingBitmap = Bitmap
                                        .createScaledBitmap(originalBitmap,desiredSize.width,desiredSize.height,false);
                                Uri uri = getImageUri(workingBitmap);

                                //we use CropImage library to crop the file and store it in output destination
//                                Toast.makeText(MainActivity.this, destinationResolution.toString(), Toast.LENGTH_SHORT).show();
                                CropImage.activity(uri)
                                        .setAllowRotation(false)
                                        .setOutputUri(Uri.fromFile(destinationFile))
                                        .setMinCropResultSize(destinationResolution.width, destinationResolution.height)
                                        .setMaxCropResultSize(destinationResolution.width, destinationResolution.height)
                                        .setAutoZoomEnabled(false)
                                        .setMaxZoom(1)
                                        .setActivityTitle("Select Region")
                                        .start(MainActivity.this);
                            }
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();


        });
        dialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //We tell user their file has been saved or was there an error
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Toast.makeText(this, "File saved " + resultUri, Toast.LENGTH_SHORT).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Save error " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    //listens to click on folder
    @Override
    public void onFolderClicked(@NonNull WallpaperDirectory folder) {
        WallpaperFragment wallpaperFragment = new WallpaperFragment();
        //when folder is clicked, we replace container with wallpaperFragment
        //and pass folder title as argument to it and set it's listeners
        Bundle args = new Bundle();
        args.putString(WallpaperFragment.FOLDER_TITLE, folder.Title);
        args.putString(WallpaperFragment.FOLDER_PREVIEW, folder.PreviewUrl);
        wallpaperFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, wallpaperFragment)
                .addToBackStack(null)
                .commit();
        activeFragment = null;
        wallpaperFragment.setAddButtonClickListener(MainActivity.this);
        wallpaperFragment.setWallpaperListener(MainActivity.this);
    }

    @Override
    public void onFoldersPassed(@NonNull final OnFolderPassListener listener) {
        //When folders are loaded from firebase, we pass it to FolderFragment and it handles the folders
        //get stored value of isNotSafeForWork
        SharedPreferences sharedPreferences = getSharedPreferences(PrefsKeys.SETTINGS, Context.MODE_PRIVATE);
        boolean isNSFWAllowed = sharedPreferences.getBoolean(PrefsKeys.NSFWContent, getResources().getBoolean(R.bool.show_NSFW));
        Log.d("Directory", "" + isNSFWAllowed);
        if (!isNSFWAllowed) {
            firebaseDatabase.getReference().child(FirebaseReferences.WALLPAPER_REF)
                    .orderByChild("IsNotSafeForWork")
                    .equalTo(false)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d("Directory Data", dataSnapshot.toString());
                            final ArrayList<WallpaperDirectory> list = new ArrayList<>();
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                WallpaperItem item = data.getValue(WallpaperItem.class);
                                WallpaperDirectory dir = new WallpaperDirectory(item.Title, item.Url);
                                if (!list.contains(dir)) {
                                    list.add(dir);
                                }
                            }
                            listener.onFoldersPassed(list);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
            Log.d("Directory", "Doing safe work check");
        } else {
            firebaseDatabase.getReference().child(FirebaseReferences.WALLPAPER_REF)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d("Directory Data", dataSnapshot.toString());
                            final ArrayList<WallpaperDirectory> list = new ArrayList<>();
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                WallpaperItem item = data.getValue(WallpaperItem.class);
                                WallpaperDirectory dir = new WallpaperDirectory(item.Title, item.Url);
                                if (!list.contains(dir)) {
                                    list.add(dir);
                                }
                            }
                            listener.onFoldersPassed(list);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    //we show the app bar by moving it from it's current location to above container
    private void animateToActivityWithAppBar() {
        final ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this, R.layout.activity_main);
        Transition transition = new ChangeBounds();
        transition.setInterpolator(new DecelerateInterpolator(1.0f));
        transition.setDuration(300);

        TransitionManager.beginDelayedTransition(mainConstraintLayout, transition);
        constraintSet.applyTo(mainConstraintLayout);
    }

    //we hide the app bar by moving it outside the screen
    private void animateToActivityWithoutAppBar() {
        final ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this, R.layout.activity_main_without_appbar);
        Transition transition = new ChangeBounds();
        transition.setInterpolator(new DecelerateInterpolator(1.0f));
        transition.setDuration(300);

        TransitionManager.beginDelayedTransition(mainConstraintLayout, transition);
        constraintSet.applyTo(mainConstraintLayout);
    }

    @NonNull
    private Resolution getScreenResolution() {
        //we get device corner points to get full resolution of device
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(point);
        return new Resolution(point.x, point.y);
    }

    public Resolution getResolution(String type){
        switch (type){
            case ResolutionOptions.DEVICE_RESOLUTION:
                return getScreenResolution();
            case ResolutionOptions.RESOLUTION_360P:
                return new Resolution(360, 480);
            case ResolutionOptions.RESOLUTION_480P:
                return new Resolution(480, 720);
            case ResolutionOptions.RESOLUTION_HD:
                return new Resolution(720, 1280);
            case ResolutionOptions.RESOLUTION_FHD:
                return new Resolution(1080, 1920);
            case ResolutionOptions.RESOLUTION_UHD:
                return new Resolution(2160, 3840);
                default:
                    return new Resolution(0,0);
        }
    }
    public Resolution getResizeResolution(Bitmap bm, Resolution resolution){
        int originalWidth = bm.getWidth();
        int originalHeight = bm.getHeight();
        int desiredWidth, desiredHeight;
        if(originalWidth<=originalHeight){
            double scale = (double) originalWidth/resolution.width;
            desiredHeight= (int) (originalHeight/scale);
            desiredWidth = resolution.width;
        }else{
            double scale = (double) originalHeight/resolution.height;
            desiredWidth = (int) (originalWidth/scale);
            desiredHeight = resolution.height;
        }
        return new Resolution(desiredWidth,desiredHeight);
    }

    class Resolution {
        public int width;
        public int height;

        Resolution(int width, int height) {
            this.width = width;
            this.height = height;
        }


        @Override
        public String toString() {
            return "Width: " + width + ",Height: " + height;
        }


        //makes array depending on resolution of image
        ArrayList<String> getResolutionArray() {
            ArrayList<String> resolutionArray = new ArrayList<>();
            Resolution screenResolution = getScreenResolution();
            resolutionArray.add(ResolutionOptions.ORIGINAL_RESOLUTION);
            if (width >= screenResolution.width && height >= screenResolution.height) {
                resolutionArray.add(ResolutionOptions.DEVICE_RESOLUTION);
            }
            if (width >= 360) {
                resolutionArray.add(ResolutionOptions.RESOLUTION_360P);
            }
            if (width >= 480) {
                resolutionArray.add(ResolutionOptions.RESOLUTION_480P);
            }
            if (width >= 720) {
                resolutionArray.add(ResolutionOptions.RESOLUTION_HD);
            }
            if (width >= 1080) {
                resolutionArray.add(ResolutionOptions.RESOLUTION_FHD);
            }
            if (width >= 2160) {
                resolutionArray.add(ResolutionOptions.RESOLUTION_UHD);
            }
            return resolutionArray;
        }
    }
    private Uri getImageUri(Bitmap bitmap){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void clearBackStack(){
        int count = getSupportFragmentManager().getBackStackEntryCount();
        while(count>0){
            getSupportFragmentManager().popBackStackImmediate();
            count = getSupportFragmentManager().getBackStackEntryCount();
        }
    }
}
