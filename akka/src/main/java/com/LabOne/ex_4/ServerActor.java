package com.LabOne.ex_4;

import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.LabOne.ex_3.ContactsListActor;
import scala.sys.Prop;

public class ServerActor extends AbstractActorWithStash {

    private class PingAndSender {
        private final Ping ping;
        private final ActorRef sender;

        public PingAndSender(Ping msg, ActorRef sender) {
            this.ping = msg;
            this.sender = sender;
        }

        public ActorRef getSender() {
            return sender;
        }

        public Ping getPing() {
            return ping;
        }
    }

    private boolean sleeping = false;
    @Override
    public Receive createReceive() {
        return awake();
    }

    public Receive awake() {
        return receiveBuilder()
                .match(Sleep.class, msg -> {
                    getContext().become(asleep());
                    System.out.println("ServerActor: zzzzzzzzzzzzz");
                })
                .match(Ping.class, msg -> {
                    sender().tell(new Pong(msg.getMessage()), self());
                    System.out.println("ServerActor: I got you!");
                })
                .match(PingAndSender.class, msg -> {
                    msg.getSender().tell(new Pong(msg.getPing().getMessage()), self());
                    System.out.println("ServerActor: Sorry, I had other things to do XD");
                })
                .build();

    }

    public Receive asleep(){
        return receiveBuilder()
                .match(Wakeup.class, msg -> {
                    unstashAll();
                    getContext().become(awake());
                    System.out.println("ServerActor: ok ok, I'm awake");
                })
                .match(Ping.class, msg -> {
                    System.out.println("ServerActor: five more minutes XO");
                    self().tell(new PingAndSender(msg, sender()), self());
                })
                .match(PingAndSender.class, msg -> stash())
                .build();
    }

    private void sleep(Sleep msg){
        this.sleeping = true;
    }

    private void wakeUp(Wakeup msg){
        this.sleeping = false;
    }

    static Props props() {
        return Props.create(ServerActor.class);
    }
}
