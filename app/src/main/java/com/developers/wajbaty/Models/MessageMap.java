package com.developers.wajbaty.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.HashMap;

@IgnoreExtraProperties
public class MessageMap implements Serializable {

    @PropertyName("content")
    public String content;
    @PropertyName("deleted")
    public boolean deleted;
    @PropertyName("sender")
    public String sender;
    @PropertyName("time")
    public long time;
    @Exclude
    private String id;


    public MessageMap() {
    }

    public MessageMap(HashMap<String, Object> messageHashMap) {

        this.content = (String) messageHashMap.get("content");
        if (messageHashMap.containsKey("deleted")) {
            this.deleted = (boolean) messageHashMap.get("deleted");
        }
        this.sender = (String) messageHashMap.get("sender");
        this.time = (long) messageHashMap.get("time");

    }


    public MessageMap(String content, boolean deleted, String sender, long time) {
        this.content = content;
        this.deleted = deleted;
        this.sender = sender;
        this.time = time;
    }

    public MessageMap(String content, long time, String sender) {
        this.content = content;
        this.time = time;
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
