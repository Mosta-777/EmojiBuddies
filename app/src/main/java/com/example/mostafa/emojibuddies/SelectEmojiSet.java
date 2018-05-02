package com.example.mostafa.emojibuddies;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Created by Mostafa on 4/30/2018.
 */

public class SelectEmojiSet extends AppCompatActivity {
    public static final String SHARED_PREF_KEY = "sharedPref";
    public static final String RADIO_BUTTON_TAG_KEY = "TAG";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_emoji_set);
        SharedPreferences sharedPreferences=getSharedPreferences(SHARED_PREF_KEY,MODE_PRIVATE);
        String selectedViewTag=sharedPreferences.getString(RADIO_BUTTON_TAG_KEY,"EmojiOne");
        RadioButton selectedRadioButton=(RadioButton)findViewById(R.id.select_emoji_set_linear_layout)
                .findViewWithTag(selectedViewTag);
        selectedRadioButton.setChecked(true);
        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup1);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                String radioButtonTag= (String) findViewById(i).getTag();
                SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE).edit();
                editor.putString(RADIO_BUTTON_TAG_KEY, radioButtonTag);
                editor.apply();
            }
        });
    }
}

