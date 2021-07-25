package com.developers.wajbaty.Models;

public class UserMessage {

    private String chattingDestinationId;
    private MessageMap chattingLatestMessageMap;
    private String messagingUserId;
    private long lastMessageRead;
    private long messagesCount;

    public UserMessage(String chattingDestinationId, MessageMap chattingLatestMessageMap, String messagingUserId, long lastMessageRead, long messagesCount) {
        this.chattingDestinationId = chattingDestinationId;
        this.chattingLatestMessageMap = chattingLatestMessageMap;
        this.messagingUserId = messagingUserId;
        this.lastMessageRead = lastMessageRead;
        this.messagesCount = messagesCount;
    }

    public String getChattingDestinationId() {
        return chattingDestinationId;
    }

    public void setChattingDestinationId(String chattingDestinationId) {
        this.chattingDestinationId = chattingDestinationId;
    }

    public MessageMap getChattingLatestMessageMap() {
        return chattingLatestMessageMap;
    }

    public void setChattingLatestMessageMap(MessageMap chattingLatestMessageMap) {
        this.chattingLatestMessageMap = chattingLatestMessageMap;
    }

    public String getMessagingUserId() {
        return messagingUserId;
    }

    public void setMessagingUserId(String messagingUserId) {
        this.messagingUserId = messagingUserId;
    }

    public long getLastMessageRead() {
        return lastMessageRead;
    }

    public void setLastMessageRead(long lastMessageRead) {
        this.lastMessageRead = lastMessageRead;
    }

    public long getMessagesCount() {
        return messagesCount;
    }

    public void setMessagesCount(long messagesCount) {
        this.messagesCount = messagesCount;
    }
}
