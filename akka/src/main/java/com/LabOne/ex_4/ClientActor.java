package com.LabOne.ex_4;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class ClientActor extends AbstractActor {
    private ActorRef server;
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActorRef.class, server -> {this.server = server;})
                .match(Ping.class, msg -> {
                    server.tell(msg, self());
                    System.out.println("ClientActor: sent message " + msg.getMessage());
                })
                .match(Pong.class, msg -> System.out.println("ClientActor: received message " + msg.getMessage()))
                .match(Sleep.class, msg -> {
                    server.tell(msg, self());
                    System.out.println("ClientActor: Bewitched Sleep");
                })
                .match(Wakeup.class, msg -> {
                    server.tell(msg, self());
                    System.out.println("ClientActor: Enervate");
                })
                .build();
    }
    static Props props(){
        return Props.create(ClientActor.class);
    }
}
