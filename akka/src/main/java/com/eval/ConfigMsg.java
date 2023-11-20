package com.eval;

import akka.actor.ActorRef;

// this class is used to send the reference of the dispatcher to one sensor
public class ConfigMsg {
    private final ActorRef dispatcher;

    public ConfigMsg (ActorRef dispatcher) {
        this.dispatcher = dispatcher;
    }

    public ActorRef getDispatcherRef() {
        return dispatcher;
    }
}
