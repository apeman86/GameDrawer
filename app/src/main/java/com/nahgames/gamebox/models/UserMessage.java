package com.nahgames.gamebox.models;

/**
 * Created by Michael on 4/17/2017.
 */

public class UserMessage {
    String key;
    String fromName;
    String fromUsername;
    String message;
    String uid;
    TYPE type;
    public enum TYPE {
        FRIENDINVITE,
        CHAT,
        GAMEINVITE
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
