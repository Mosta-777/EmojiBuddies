package com.example.mostafa.emojibuddies;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final int RC_PHOTO_PICKER_PROFILE= 2;
    private static final String ARG_PROFILE_PICS = "profile_pics";
    private static final String ARG_USER_PROFILE_PIC = "profilePicUri";
    private static final String FRIENDS_LISTS_NODE = "friends_lists";
    public static final String EMOJI_FROM_GALLERY_KEY = "emojiFromGallery";
    public static final String USER_ID_KEY = "user_id";
    Context context=MainActivity.this;
    public static String USERS_NODE="users";
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersReference;
    private DatabaseReference friendsListsReference;
    private FirebaseStorage firebaseStorage;
    private StorageReference profilePicsStorage;
    public static final int RC_SIGN_IN = 1;
    private ListView storiesListView;
    private TextView emptyListTextView;
    private StoriesListAdapter storiesListAdapter;
    private List<User> friends;
    private User previuoseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        final Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/SweetSensations.ttf");
        collapsingToolbar.setCollapsedTitleTypeface(tf);
        collapsingToolbar.setExpandedTitleTypeface(tf);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) context,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                } else {
                    toEmojifiedImagePreviewClass();
                }
            }
        });
        storiesListView=(ListView)findViewById(R.id.stories_listview);
        emptyListTextView=(TextView)findViewById(R.id.empty_list_text);
        storiesListView.setEmptyView(emptyListTextView);
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        usersReference = firebaseDatabase.getReference().child(USERS_NODE);
        friendsListsReference = firebaseDatabase.getReference().child(FRIENDS_LISTS_NODE);
        profilePicsStorage=firebaseStorage.getReference().child(ARG_PROFILE_PICS);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    attachDatabaseReadListener();
                } else {
                    // User is signed out
                    storiesListAdapter.clear();
                    detachEventListener();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(
                                            Arrays.asList(
                                                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
        friends=new ArrayList<>();
        storiesListAdapter=new StoriesListAdapter(this,R.layout.stories_list_item,friends);
        storiesListView.setAdapter(storiesListAdapter);
        storiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> listView, View itemView, int itemPosition, long itemId) {
                Intent toStory=new Intent(MainActivity.this,StoryPreview.class);
                toStory.putExtra(USER_ID_KEY,friends.get(itemPosition).getUid());
                startActivity(toStory);
            }
        });

}

    private void detachEventListener() {
        if (mChildEventListener != null) {
            friendsListsReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    User addedUser=dataSnapshot.getValue(User.class);
                    if (previuoseUser!=addedUser) {
                        storiesListAdapter.add(addedUser);
                        previuoseUser=addedUser;
                    }
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {}
            };
            friendsListsReference.child(FirebaseAuth.getInstance()
                    .getCurrentUser().getUid()).addChildEventListener(mChildEventListener);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        storiesListAdapter.clear();
        detachEventListener();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, check if it's the user's first time to sign in
                final FirebaseUser user = mFirebaseAuth.getCurrentUser();
                final String userUid=user.getUid();
                String userTag=userUid.substring(user.getUid().length()-5);
                Toast.makeText(this, getResources().getString(R.string.singed_in_text), Toast.LENGTH_SHORT).show();
                usersReference.child(user.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()){
                                    addUserToDatabase(user);
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, getResources().getString(R.string.signed_in_cancelled_text), Toast.LENGTH_SHORT).show();
                finish();
            }
        }else if (requestCode==RC_PHOTO_PICKER_PROFILE){
            if (resultCode==RESULT_OK){
                final FirebaseUser user = mFirebaseAuth.getCurrentUser();
                final String userUid=user.getUid();
                Uri selectedImageUri=data.getData();
                profilePicsStorage.child(userUid).putFile(selectedImageUri)
                        .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl=taskSnapshot.getDownloadUrl();
                        // Then we upload the photo
                        // 1-The general users node
                        usersReference.child(userUid).child(ARG_USER_PROFILE_PIC).setValue(downloadUrl.toString());
                        // 2-Every single place in friends_lists where the user is there
                        //friendsListsReference.
                        Toast.makeText(getBaseContext(),R.string.profile_pic_uploaded,Toast.LENGTH_LONG).show();
                    }
                });
            }else if (resultCode==RESULT_CANCELED)
                Toast.makeText(this,R.string.failed_to_get_photo,Toast.LENGTH_SHORT).show();
        }
    }

    private void addUserToDatabase(final FirebaseUser user) {
        User userToBeAdded=new User(user.getUid().substring(user.getUid().length()-5)
                ,user.getDisplayName(),"empty",
                ColorGenerator.MATERIAL.getRandomColor(),System.currentTimeMillis());
        usersReference.child(user.getUid().substring(user.getUid().length()-5))
                .setValue(userToBeAdded).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getBaseContext(),"Welcome ! You have registered successfully , your tag is "+
                    user.getUid().substring(user.getUid().length()-5),Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(),"Failed to register user",Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    toEmojifiedImagePreviewClass();
                } else {
                    Toast.makeText(this, R.string.permission_to_external_storage_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void toEmojifiedImagePreviewClass(){
        Intent toClass=new Intent(MainActivity.this,EmojifiedImagePreview.class);
        toClass.putExtra(EMOJI_FROM_GALLERY_KEY,false);
        startActivity(toClass);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout){
            AuthUI.getInstance().signOut(this);
            return true;
        } else if (id== R.id.action_add_friend){
            // get prompts.xml view
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View promptView = layoutInflater.inflate(R.layout.enter_friends_tag, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            // set prompts.xml to be the layout file of the alertdialog builder
            alertDialogBuilder.setView(promptView);
            final EditText input = (EditText) promptView.findViewById(R.id.userInput);
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final String friendsTag= input.getText().toString();
                            // 1- First read the user
                            usersReference.child(friendsTag)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()){
                                                User user=dataSnapshot.getValue(User.class);
                                                friendsListsReference.child(FirebaseAuth.getInstance()
                                                        .getCurrentUser().getUid()).child(friendsTag).setValue(user)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                Toast.makeText(getBaseContext()
                                                                        ,R.string.friend_added,Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                            }else {
                                            Toast.makeText(getBaseContext(),R.string.no_such_tag,Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            // create an alert dialog
            AlertDialog alertD = alertDialogBuilder.create();
            alertD.show();
            return true;
        }else if (id==R.id.action_show_tag){
            final FirebaseUser user = mFirebaseAuth.getCurrentUser();
            final String userUid=user.getUid();
            String userTag=userUid.substring(user.getUid().length()-5);
            Toast.makeText(getBaseContext(),"Your tag is "+userTag,Toast.LENGTH_LONG).show();
            return true;
        }else if (id==R.id.action_change_emoji_set){
            Intent intent=new Intent(this, SelectEmojiSet.class);
            startActivity(intent);
        }else if (id==R.id.action_emojify_from_gallery){
            Intent intent=new Intent(this,EmojifiedImagePreview.class);
            intent.putExtra(EMOJI_FROM_GALLERY_KEY,true);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
