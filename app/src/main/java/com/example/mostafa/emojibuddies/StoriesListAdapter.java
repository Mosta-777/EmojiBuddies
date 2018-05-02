package com.example.mostafa.emojibuddies;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Mostafa on 4/30/2018.
 */

public class StoriesListAdapter extends ArrayAdapter<User> {
    private SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM");
    private static final long MINUTE_MILLIS = 1000 * 60;
    private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    public StoriesListAdapter(@NonNull Context context, int resource, List<User> users) {
        super(context, resource,users);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.stories_list_item
                    , parent, false);
        }
            ImageView profilePicImageView=(ImageView)convertView.findViewById(R.id.user_pic);
            TextView userName=(TextView)convertView.findViewById(R.id.textView);
            TextView lastUploaded=(TextView)convertView.findViewById(R.id.textView2);
            User user=getItem(position);
            userName.setText(user.getName());
            lastUploaded.setText(convertIntoReadableTimeOrDate(user.getLastTimeUploadedTimeStamp()));
            if (user.getProfilePicUri().equals("empty"))
                profilePicImageView.setImageDrawable(TextDrawable.builder()
                        .buildRound(user.getName().substring(0,1),
                                user.getDefaultProfilePicColor()));
            else     Glide.with(profilePicImageView.getContext())
                    .load(Uri.parse(user.getProfilePicUri()))
                    .into(profilePicImageView);

        return convertView;
    }

        private String convertIntoReadableTimeOrDate(long timeStamp) {
        String date = "";
        long now = System.currentTimeMillis();
        // Change how the date is displayed depending on whether it was written in the last minute,
        // the hour, etc.
        if (now - timeStamp < (DAY_MILLIS)) {
            if (now - timeStamp < (HOUR_MILLIS)) {
                long minutes = Math.round((now - timeStamp) / MINUTE_MILLIS);
                date = String.valueOf(minutes) + "m";
            } else {
                long minutes = Math.round((now - timeStamp) / HOUR_MILLIS);
                date = String.valueOf(minutes) + "h";
            }
        } else {
            Date dateDate = new Date(timeStamp);
            date = sDateFormat.format(dateDate);
        }

        // Add a dot to the date string
        date = "\u2022 " + date;
        return date;
    }
}
