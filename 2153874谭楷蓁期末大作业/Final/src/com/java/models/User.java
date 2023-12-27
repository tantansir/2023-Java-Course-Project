package com.java.models;

public class User {
    private int userID;
    private String username;
    private String password;
    private String avatar;
    private String information;
    private String email;

    // 构造函数
    public User(int userID, String username, String password, String avatar, String information, String email) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.avatar = avatar;
        this.information = information;
        this.email = email;
    }

    // Getter 和 Setter 方法
    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

	public String getAvatarPath() {
		return avatar;
	}

	public void setAvatarPath(String absolutePath) {
		this.avatar = avatar;
	}
}

