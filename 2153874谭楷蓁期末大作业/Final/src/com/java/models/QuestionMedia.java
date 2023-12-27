package com.java.models;

public class QuestionMedia {
    private int mediaID;
    private int questionID;
    private String mediaType;
    private String mediaPath;

    // 构造函数、getters 和 setters
    public QuestionMedia(int mediaID, int questionID, String mediaType, String mediaPath) {
        this.mediaID = mediaID;
        this.questionID = questionID;
        this.mediaType = mediaType;
        this.mediaPath = mediaPath;
    }

    public int getMediaID() { return mediaID; }
    public int getQuestionID() { return questionID; }
    public String getMediaType() { return mediaType; }
    public String getMediaPath() { return mediaPath; }
}

