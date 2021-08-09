package com.developers.wajbaty.Models;

import java.io.Serializable;

public class Notification implements Serializable {

    public static final int TYPE_MESSAGE = 1;

    private String ID;
    private String senderID;
    private String destinationID;
    private String content;
    private int type;
    private long timeCreatedInMillis;
    private boolean seen;

    public Notification() {
    }

    public Notification(String ID, String senderID, String destinationID, String content, int type, long timeCreatedInMillis, boolean seen) {
        this.ID = ID;
        this.senderID = senderID;
        this.destinationID = destinationID;
        this.content = content;
        this.type = type;
        this.timeCreatedInMillis = timeCreatedInMillis;
        this.seen = seen;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getDestinationID() {
        return destinationID;
    }

    public void setDestinationID(String destinationID) {
        this.destinationID = destinationID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimeCreatedInMillis() {
        return timeCreatedInMillis;
    }

    public void setTimeCreatedInMillis(long timeCreatedInMillis) {
        this.timeCreatedInMillis = timeCreatedInMillis;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
