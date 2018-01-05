package com.nahgames.gamebox.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Michael on 4/5/2017.
 */
@IgnoreExtraProperties
public class User {


    private String name;
    private String username;
    private String email;
    private String lastLogIn;
    private Map<String, Friend> friends;
    private Map<String, UserMessage> messages;

    public User() {
    }

    public User(String name, String email, String lastLogIn) {
        this.name = name;
        this.email = email;
        this.lastLogIn = lastLogIn;
    }

    public User(String name, String username, String email, String lastLogIn) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.lastLogIn = lastLogIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastLogIn() {
        return lastLogIn;
    }

    public void setLastLogIn(String lastLogIn) {
        this.lastLogIn = lastLogIn;
    }

    public Map<String, Friend> getFriends() {
        return friends;
    }

    public void setFriends(Map<String, Friend> friends) {
        this.friends = friends;
    }

    public Map<String, UserMessage> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, UserMessage> messages) {
        this.messages = messages;
    }

    @Exclude
    public Map<String,Object> toMapAllFields(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("username", username);
        result.put("email", email);
        result.put("lastLogIn", lastLogIn);
        return result;
    }
    @Exclude
    public Map<String,Object> toMapNameAndLastLoginFields(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("email", email);
        result.put("lastLogIn", lastLogIn);
        return result;
    }

    public ArrayList<Friend> getFriendsList() {

        return new ArrayList<Friend>(friends.values());
    }
}
