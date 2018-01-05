package com.nahgames.gamebox.models;

/**
 * Created by Michael on 4/5/2017.
 */

public class Friend {
    private String uid;
    private String name;
    private String onlineStatus;

    public Friend() {
    }

    public Friend(String name, String onlineStatus) {
        this.name = name;
        this.onlineStatus = onlineStatus;
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

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }
}
