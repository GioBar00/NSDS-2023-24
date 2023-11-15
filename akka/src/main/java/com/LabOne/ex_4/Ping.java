package com.LabOne.ex_4;

import akka.actor.ActorRef;

public class Ping {
    private final String message;

    public Ping(String message)
    {
        this.message = message;
    }
    public String getMessage(){
        return this.message;
    }

}
