package com.example.mostafa.emojibuddies;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import butterknife.OnClick;

/**
 * Created by Mostafa on 4/27/2018.
 */

public class EmojiPickerAdapter extends RecyclerView.Adapter<EmojiPickerAdapter.ViewHolder> {
    ArrayList<Bitmap> emojis;
    String[] emojiNames;
    Context context;
    private final EmojiPickerOnClickHandler mClickHandler;
    public interface EmojiPickerOnClickHandler {
        void onClick(String selectedEmoji);
    }
    public EmojiPickerAdapter(ArrayList<Bitmap> emojis,String[] emojiNames,EmojiPickerOnClickHandler handler,Context context){
        this.emojis=emojis;
        this.emojiNames=emojiNames;
        this.mClickHandler=handler;
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.emoji_picker_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.image.setImageBitmap(emojis.get(position));
    }

    @Override
    public int getItemCount() {
        if (emojis==null) return 0;
        return emojis.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView image;
        private String imageName;
        public ViewHolder(View v) {
            super(v);
            image =(ImageView)v.findViewById(R.id.img);
            v.setOnClickListener(this);
        }
        public ImageView getImage(){ return this.image;}
        public String getImageName(){ return this.imageName;}
        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            String clickedEmojiName = emojiNames[adapterPosition];
            Bitmap clickedEmojiPhoto = emojis.get(adapterPosition);
            mClickHandler.onClick(clickedEmojiName);
        }
    }
}
