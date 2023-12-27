package com.java.models;

public class Question {
    private int QuestionID;
    private String subject;
    private String type;
    private String content;
    private String difficulty;
    private String answer;

    public Question(int id, String subject, String type, String difficulty, String content, String answer) {
    	this.QuestionID = id;
    	this.subject = subject;
        this.type = type;
        this.content = content;
        this.difficulty = difficulty;
        this.answer = answer;
    }

    // Getter 方法
    public String getSubject() { return subject; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public String getDifficulty() { return difficulty; }
	public String getAnswer() { return answer; }
	public int getId() { return QuestionID; }
}

