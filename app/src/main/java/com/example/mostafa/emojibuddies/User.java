package com.example.mostafa.emojibuddies;

/**
 * Created by Mostafa on 4/29/2018.
 */

public class User {
    private String uid;
    private String name;
    private String profilePicUri;
    private int defaultProfilePicColor;
    private long lastTimeUploadedTimeStamp;

    public User(){}

    public User(String uid,String name,String profilePicUri,int defaultProfilePicColor,long lastTimeUploadedTimeStamp){
        this.uid=uid;
        this.name=name;
        this.profilePicUri=profilePicUri;
        this.defaultProfilePicColor=defaultProfilePicColor;
        this.lastTimeUploadedTimeStamp=lastTimeUploadedTimeStamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicUri() {
        return profilePicUri;
    }

    public void setProfilePicUri(String profilePicUri) {
        this.profilePicUri = profilePicUri;
    }

    public int getDefaultProfilePicColor() {
        return defaultProfilePicColor;
    }

    public void setDefaultProfilePicColor(int defaultProfilePicColor) {
        this.defaultProfilePicColor = defaultProfilePicColor;
    }

    public long getLastTimeUploadedTimeStamp() {
        return lastTimeUploadedTimeStamp;
    }

    public void setLastTimeUploadedTimeStamp(long lastTimeUploadedTimeStamp) {
        this.lastTimeUploadedTimeStamp = lastTimeUploadedTimeStamp;
    }
}
