package com.websocketdemo.demo.WebSocket;

import com.alibaba.fastjson.JSONObject;

import javax.websocket.Session;

public class UserEntity {
    private String userId;
    private String password;
    private String avatar;
    private String nickname;
    private Session session;

    public UserEntity(String userId, String avatar, String nickname, Session session) {
        this.userId = userId;
        this.avatar = avatar;
        this.nickname = nickname;
        this.session = session;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getUserinfo(){
        JSONObject job = new JSONObject();
        job.put("userId",userId);
        job.put("avatar",avatar);
        job.put("nickname",nickname);
        return job.toString();
    }

}
