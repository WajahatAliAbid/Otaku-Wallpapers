package com.ali.otaku.otakuwallpapers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.ali.otaku.models.FirebaseReferences;
import com.ali.otaku.models.Wallpaper;
import com.ali.otaku.models.WallpaperItem;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Date;
import java.util.Objects;

import in.myinnos.awesomeimagepicker.activities.AlbumSelectActivity;
import in.myinnos.awesomeimagepicker.helpers.ConstantsCustomGallery;
import in.myinnos.awesomeimagepicker.models.Image;

public class AddWallpaperActivity extends AppCompatActivity implements View.OnClickListener{

    //Intent arguments
    public static String DIRECTORY = "WallpaperDirectory";

    private Spinner wallpaperCategory;
    private AutoCompleteTextView folderName,characterName;
    private Button selectImage,uploadWallpaper;
    private ImageView wallpaperPreview;
    private Switch isNotSafeForWork;

    //Request code for image picking
    private final int _PICKIMAGE = 2565;

    private ConstraintLayout mainConstraintLayout;
    ArrayAdapter<String> folderNameAdapter,characterNameAdapter;

    private Image selectedWallpaper;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wallpaper);

        //Initializing firebase parameters
        FirebaseApp firebaseApp = FirebaseApp.initializeApp(this);
        assert firebaseApp != null;
        firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);
        firebaseStorage = FirebaseStorage.getInstance(firebaseApp);

        //Initializing parameters
        wallpaperCategory = findViewById(R.id.categorySpinner);
        folderName = findViewById(R.id.folder_name);
        characterName = findViewById(R.id.character_name);
        selectImage = findViewById(R.id.select_wallpaper);
        uploadWallpaper = findViewById(R.id.upload_wallpaper);
        wallpaperPreview = findViewById(R.id.wallpaper_image);
        isNotSafeForWork = findViewById(R.id.is_not_safe_for_work);
        mainConstraintLayout = findViewById(R.id.addWallpaperView);

        //Adapters for text view so it can give suggestions to user
        folderNameAdapter = new ArrayAdapter<>(this,android.R.layout.select_dialog_item);
        characterNameAdapter = new ArrayAdapter<>(this,android.R.layout.select_dialog_item);

        //Setting on click listeners for buttons
        selectImage.setOnClickListener(this);
        uploadWallpaper.setOnClickListener(this);

        //if user passes category and directory then set values from that
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            String directoryName = extras.getString(DIRECTORY);
            folderName.setText(directoryName);
            folderName.setEnabled(false);
        }



    }

    public boolean validateFields(){
        boolean returnValue = true;
        if(TextUtils.isEmpty(folderName.getText())){
            folderName.setError("This field is required");
            returnValue = false;
        }
        if(TextUtils.isEmpty(characterName.getText())){
            characterName.setError("This field is required");
            returnValue = false;
        }
        if(selectedWallpaper==null){
            Toast.makeText(this,"Please select a wallpaper",Toast.LENGTH_SHORT).show();
            returnValue = false;
        }
        return returnValue;
    }

    @Override
    public void onClick(View v) {
        if(v.equals(selectImage)){
            //Opens gallery and selects 1 image cause INTENT_EXTRA_LIMIT is set to 1
            final Intent intent = new Intent(this, AlbumSelectActivity.class);
            intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT,1);
            startActivityForResult(intent,_PICKIMAGE);
        }else if(v.equals(uploadWallpaper)){
            if(!validateFields())
                return;
            //Checks if any view is in focus and hides the keyboard
            if(getCurrentFocus()!=null){
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
                }
            }
            final String Category = wallpaperCategory.getSelectedItem().toString().toLowerCase();
            final String DirectoryNameText = folderName.getText().toString().toLowerCase().replace("/"," ").trim();
            final String CharacterNameText = characterName.getText().toString().toLowerCase().replace("/"," ").trim();
            final boolean NotSafeForWork = isNotSafeForWork.isChecked();

            //Uploading and setting window not touchable
            startProgress();
            String fileName = Category + "wallpaper" + CharacterNameText + new Date().getTime();
            final StorageReference storageReference =firebaseStorage.getReference()
                    .child("wallpapers")
                    .child(DirectoryNameText)
                    .child(fileName);
            Uri uri = Uri.fromFile(new File(selectedWallpaper.path));
            final UploadTask uploadTask = storageReference.putFile(uri);
            //first of all we want to upload the image to firebase storage and get download url
            uploadTask.addOnFailureListener(exception ->
                    Toast.makeText(AddWallpaperActivity.this,exception.getMessage(),Toast.LENGTH_SHORT).show()
            ).addOnSuccessListener(taskSnapshot -> uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }

                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    Log.d("UpladedImageUri",downloadUri.toString());
                    //we got the download URI
                    //we push the values to database
                    final WallpaperItem wallpaperItem =
                            new WallpaperItem(CharacterNameText,NotSafeForWork,downloadUri.toString(),
                                    Category,DirectoryNameText);
                    firebaseDatabase.getReference()
                            .child(FirebaseReferences.WALLPAPER_REF)
                            .push().setValue(wallpaperItem)
                            .addOnFailureListener(exception->{
                                Toast.makeText(AddWallpaperActivity.this,exception.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                stopProgress();
                            })
                            .addOnCompleteListener(task1 -> {
                                Toast.makeText(AddWallpaperActivity.this,"Wallpaper uploaded",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(AddWallpaperActivity.this,MainActivity.class));
                                finish();
                            });
                }else{
                    Toast.makeText(AddWallpaperActivity.this,
                            task.getException().getMessage(),Toast.LENGTH_SHORT)
                            .show();
                    stopProgress();
                }

            }));
        }
    }

    //it copies constraints from one layout to another to show progress and hides everything else
    private void startProgress(){
        //making everything not touchable
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        final ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this,R.layout.add_wallpaper_in_progress);
        Transition transition = new ChangeBounds();
        transition.setInterpolator(new DecelerateInterpolator(1.0f));
        transition.setDuration(300);

        TransitionManager.beginDelayedTransition(mainConstraintLayout,transition);
        constraintSet.applyTo(mainConstraintLayout);
    }
    //it hides progress and shows regular controls like input etc
    private void stopProgress(){
        //back to normal, everything touchable
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


        final ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this,R.layout.add_wallpaper_original);
        Transition transition = new ChangeBounds();
        transition.setInterpolator(new DecelerateInterpolator(1.0f));
        transition.setDuration(300);

        TransitionManager.beginDelayedTransition(mainConstraintLayout,transition);
        constraintSet.applyTo(mainConstraintLayout);

        if(selectedWallpaper!=null){
            wallpaperPreview.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Checks for result with request code _PICKIMAGE and sets image to it
        if(requestCode==_PICKIMAGE && resultCode== Activity.RESULT_OK && data!=null) {
            selectedWallpaper = (Image) data.getParcelableArrayListExtra(ConstantsCustomGallery.INTENT_EXTRA_IMAGES).get(0);

            Uri uri = Uri.fromFile(new File(selectedWallpaper.path));
            wallpaperPreview.setImageURI(uri);
            wallpaperPreview.setVisibility(View.VISIBLE);
        }
    }
}
