package com.example.mostafa.emojibuddies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.glide.slider.library.Animations.DescriptionAnimation;
import com.glide.slider.library.SliderLayout;
import com.glide.slider.library.SliderTypes.BaseSliderView;
import com.glide.slider.library.SliderTypes.TextSliderView;
import com.glide.slider.library.Tricks.ViewPagerEx;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mostafa on 5/1/2018.
 */

public class StoryPreview extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener{
    private SliderLayout storySlider;
    private DatabaseReference friendStory;
    private TextView emptyStoryTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.story_preview);
        emptyStoryTextView=(TextView)findViewById(R.id.empty_story_textview);
        storySlider = (SliderLayout) findViewById(R.id.slider);
        HashMap<String, String> url_maps = new HashMap<String, String>();
        final ArrayList<String> listUrl = new ArrayList<String>();
        ArrayList<String> listName = new ArrayList<String>();
        String userTag=getIntent().getStringExtra(MainActivity.USER_ID_KEY);
        if (userTag!=null) {
            friendStory = FirebaseDatabase.getInstance().getReference()
                    .child(EmojifiedImagePreview.STORIES).child(userTag);
            friendStory.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    StorySlide storySlide=dataSnapshot.getValue(StorySlide.class);
                    TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView
                            .image(storySlide.getSlideImageUrl())
                            .setProgressBarVisible(true);
                    textSliderView.bundle(new Bundle());
                    if (storySlide.getSlideText()!=null) {
                        textSliderView.description(storySlide.getSlideText());
                        textSliderView.getBundle().putString("extra",storySlide.getSlideText());
                    }else {
                        textSliderView.description("");
                        textSliderView.getBundle().putString("extra","");
                    }
                    storySlider.addSlider(textSliderView);
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
            friendStory.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount()>0) {
                        emptyStoryTextView.setVisibility(View.GONE);
                        storySlider.setPresetTransformer(SliderLayout.Transformer.Default);
                        storySlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                        storySlider.setCustomAnimation(new DescriptionAnimation());
                        storySlider.setDuration(6000);
                    }else {
                        emptyStoryTextView.setVisibility(View.VISIBLE);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }else {
            Toast.makeText(this,"Error fetching user story",Toast.LENGTH_LONG).show();
            finish();
        }
    }
    @Override
    public void onSliderClick(BaseSliderView baseSliderView) {

    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
