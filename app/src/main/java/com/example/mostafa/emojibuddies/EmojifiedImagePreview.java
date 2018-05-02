package com.example.mostafa.emojibuddies;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Mostafa on 4/20/2018.
 */

public class EmojifiedImagePreview extends AppCompatActivity{

    private static final int EMOJI_PICKER_REQUEST_CODE = 2;
    public static final String STORIES = "stories";
    private static final int RC_PHOTO_PICKER = 69;
    private String tempCapturedPhotoPath;
    private Bitmap mResultsBitmap;
    private String FILE_PROVIDER_AUTH="com.example.android.EmojiBuddiesFileprovider";
    private Uri photoUri;
    private FirebaseStorage firebaseStorage;
    private StorageReference storiesReference;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference storiesDatabaseReference;
    public static int OPEN_CAMERA_REQUEST_CODE=1;
    private String userTag;
    private ProgressDialog dialog;
    private Uri emojifiedPhotoUri;
    private String emojifiedImageFilePath;


    @BindView(R.id.image_view) ImageView imagePreview;
    @BindView(R.id.clear_button) FloatingActionButton clear_button;
    @BindView(R.id.add_to_story) FloatingActionButton add_to_story_button;
    @BindView(R.id.share_button) FloatingActionButton share_button;
    @BindView(R.id.retake_pic) FloatingActionButton retake_pic_button;
    @BindView(R.id.save_button) FloatingActionButton save_button;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.image_preview);
        ButterKnife.bind(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userUid=user.getUid();
        userTag=userUid.substring(user.getUid().length()-5);
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        storiesDatabaseReference=firebaseDatabase.getReference().child(STORIES).child(userTag);
        storiesReference=firebaseStorage.getReference().child(STORIES);
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        if (getIntent().getBooleanExtra(MainActivity.EMOJI_FROM_GALLERY_KEY,false))
            pickImageFromGallery();
        else openCameraApp();
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
    }

    private void openCameraApp() {
        Intent toCameraAppIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (toCameraAppIntent.resolveActivity(getPackageManager()) != null) {
            // We create a temp file to put our captured photo
            File photoFile=null;
            try {
                photoFile = PhotoFileUtilities.createTempImageFile(this);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                tempCapturedPhotoPath = photoFile.getAbsolutePath();
                Uri capturedPhotoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTH,
                        photoFile);
                photoUri=capturedPhotoURI;
                toCameraAppIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedPhotoURI);
                startActivityForResult(toCameraAppIntent, OPEN_CAMERA_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==OPEN_CAMERA_REQUEST_CODE && resultCode==RESULT_OK){
            try {
                mResultsBitmap = PhotoFileUtilities.resamplePic(this, tempCapturedPhotoPath);
                mResultsBitmap = EmojifierBox.detectFacesandPutEmojisonThem(this,mResultsBitmap);
                prepareTheEmojifiedPhoto();
                imagePreview.setImageBitmap(mResultsBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (requestCode==OPEN_CAMERA_REQUEST_CODE && resultCode==RESULT_CANCELED) finish();
        else if (requestCode==EMOJI_PICKER_REQUEST_CODE && resultCode==RESULT_OK){
            Toast.makeText(this,data.getStringExtra(EmojiPicker.EMOJI_PICKER_INTENT_KEY),Toast.LENGTH_LONG).show();
        }
        else if (requestCode==RC_PHOTO_PICKER && resultCode==RESULT_OK){
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                mResultsBitmap = EmojifierBox.detectFacesandPutEmojisonThem(this,bitmap);
                prepareTheEmojifiedPhoto();
                imagePreview.setImageBitmap(mResultsBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            PhotoFileUtilities.deleteImageFile(this, tempCapturedPhotoPath);
        }
    }
    @OnClick(R.id.save_button)
    public void savePhoto(){
        if (!getIntent().getBooleanExtra(MainActivity.EMOJI_FROM_GALLERY_KEY,true))
        PhotoFileUtilities.deleteImageFile(this,tempCapturedPhotoPath);
        PhotoFileUtilities.saveImage(this,mResultsBitmap);
    }
    @OnClick(R.id.share_button)
    public void sharePhoto(){
        if (!getIntent().getBooleanExtra(MainActivity.EMOJI_FROM_GALLERY_KEY,true)) {
            PhotoFileUtilities.deleteImageFile(this, tempCapturedPhotoPath);
            PhotoFileUtilities.shareImage(this,tempCapturedPhotoPath,null);
        }
        PhotoFileUtilities.saveImage(this,mResultsBitmap);
        PhotoFileUtilities.shareImage(this,null,emojifiedPhotoUri);
    }
    @OnClick(R.id.clear_button)
    public void clearButtonAction(){
        if (!getIntent().getBooleanExtra(MainActivity.EMOJI_FROM_GALLERY_KEY,true))
            PhotoFileUtilities.deleteImageFile(this,tempCapturedPhotoPath);
        finish();
    }
    @OnClick(R.id.retake_pic)
    public void retakePhoto(){
        if (!getIntent().getBooleanExtra(MainActivity.EMOJI_FROM_GALLERY_KEY,true)) {
            PhotoFileUtilities.deleteImageFile(this, tempCapturedPhotoPath);
            openCameraApp();
        }else {
            pickImageFromGallery();
        }
    }
    private void prepareTheEmojifiedPhoto() {
        File photoFile = null;
        try {
            photoFile = PhotoFileUtilities.createTempImageFile(this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (photoFile != null) {
            emojifiedImageFilePath=photoFile.getAbsolutePath();
            FileOutputStream fout;
            try {
                fout = new FileOutputStream(photoFile);
                mResultsBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fout);
                fout.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
            emojifiedPhotoUri = Uri.fromFile(photoFile);
        }
    }

    @OnClick(R.id.add_to_story)
    public void addPhotoToStory() {
        final StorySlide storySlide=new StorySlide();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.add_photo_caption, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        final EditText input = (EditText) promptView.findViewById(R.id.userInput69);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String caption=input.getText().toString();
                        storySlide.setSlideText(input.getText().toString());
                        continueUploading(storySlide);
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                storySlide.setSlideText("");
                                dialog.cancel();
                                continueUploading(storySlide);
                            }
                        });
        AlertDialog alertD = alertDialogBuilder.create();
        alertD.show();
        }

    private void continueUploading(final StorySlide storySlide) {
        dialog.setMessage("Uploading photo to your story , please wait ....");
        dialog.show();
        storiesReference.child(userTag).child(emojifiedPhotoUri.getLastPathSegment())
                .putFile(emojifiedPhotoUri)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        PhotoFileUtilities.deleteImageFile(getBaseContext(),emojifiedImageFilePath);
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        storySlide.setSlideImageUrl(downloadUrl.toString());
                        storiesDatabaseReference.push().setValue(storySlide)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getBaseContext(), R.string.photo_uploaded_to_story, Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                });
                    }
                });
    }
}
