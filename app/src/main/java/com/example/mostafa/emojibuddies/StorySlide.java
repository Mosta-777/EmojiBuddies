package com.example.mostafa.emojibuddies;

/**
 * Created by Mostafa on 5/1/2018.
 */

public class StorySlide {
    private String slideImageUrl;
    private String slideText;

    public StorySlide(){}
    public StorySlide(String slideImageUrl,String slideText){
        this.slideImageUrl=slideImageUrl;
        this.slideText=slideText;
    }

    public String getSlideImageUrl() {
        return slideImageUrl;
    }

    public void setSlideImageUrl(String slideImageUrl) {
        this.slideImageUrl = slideImageUrl;
    }

    public String getSlideText() {
        return slideText;
    }

    public void setSlideText(String slideText) {
        this.slideText = slideText;
    }
}
