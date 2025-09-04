package com.example.chat.models;

public class Message {
    private String id;
    private String senderId;
    private String receiverId;
    private String text;       // nội dung text (nếu là text)
    private String imageUrl;   // ảnh (nếu có)
    private String audioUrl;   // voice (nếu có)
    private int audioDuration; // giây
    private long timestamp;    // System.currentTimeMillis()
    private boolean recalled;  // đã thu hồi?

    public Message() {}

    public Message(String id, String senderId, String receiverId, String text,
                   String imageUrl, String audioUrl, int audioDuration,
                   long timestamp, boolean recalled) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.imageUrl = imageUrl;
        this.audioUrl = audioUrl;
        this.audioDuration = audioDuration;
        this.timestamp = timestamp;
        this.recalled = recalled;
    }

    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getText() { return text; }
    public String getImageUrl() { return imageUrl; }
    public String getAudioUrl() { return audioUrl; }
    public int getAudioDuration() { return audioDuration; }
    public long getTimestamp() { return timestamp; }
    public boolean isRecalled() { return recalled; }

    public void setRecalled(boolean recalled) { this.recalled = recalled; }
}
