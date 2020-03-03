package com.parking.model;


import java.sql.Date;

public class Message {
    private int id;
    private String content;
    private int type;
    private Date date;
    private User sender;
    private User receiver;

    public Message() {
    }

    public Message(int id, String content, int type, Date date, User sender, User receiver) {
        this.id = id;
        this.content = content;
        this.type = type;
        this.date = date;
        this.sender = sender;
        this.receiver = receiver;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }
}
