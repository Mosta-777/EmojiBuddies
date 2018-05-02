package com.example.mostafa.emojibuddies;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Mostafa on 4/27/2018.
 */

public class EmojiPicker extends AppCompatActivity implements EmojiPickerAdapter.EmojiPickerOnClickHandler{

    private static final String TAG = "EmojiPicker";
    public static String EMOJI_PICKER_INTENT_KEY="key";
    private AssetManager assetManager;
    private String emojiSet;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emoji_picker);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),5);
        recyclerView.setLayoutManager(layoutManager);
        assetManager = getAssets();
        String[] imgPath=null;
        emojiSet=getIntent().getStringExtra(EMOJI_PICKER_INTENT_KEY);
        try {
            imgPath = assetManager.list(emojiSet);
            Log.e(TAG,imgPath[4]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<Bitmap> emojis = prepareData(imgPath);
        EmojiPickerAdapter adapter = new EmojiPickerAdapter(emojis,imgPath,this,getApplicationContext());
        recyclerView.setAdapter(adapter);
    }
    private ArrayList<Bitmap> prepareData(String[] imgPath){
        ArrayList<Bitmap> emojis=new ArrayList<>();
        try {
            for (String anImgPath : imgPath) {
                InputStream is = assetManager.open(emojiSet + "/" + anImgPath);
                Log.d(TAG, anImgPath);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                emojis.add(bitmap);
            }
            }catch (IOException e){
            Log.e(TAG, e.getMessage());
        }
        return emojis;
    }

    @Override
    public void onClick(String selectedEmoji) {
        Intent returnIntent=new Intent();
        returnIntent.putExtra(EMOJI_PICKER_INTENT_KEY,selectedEmoji);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
}
