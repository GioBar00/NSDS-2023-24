package com.LabOne.ex_3;

public class ReplyMsg {
    private boolean empty;
    private final String email;

    public ReplyMsg() {
        this.empty = true;
        this.email = "";
    }

    public ReplyMsg(String email) {
        this.email = email;
        empty = false;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmpty() {
        return empty;
    }
}
