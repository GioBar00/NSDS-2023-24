package com.LabOne.ex_5;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;


public class ContactListSupervisorActor extends AbstractActor {

    private static final SupervisorStrategy strategy =
            new OneForOneStrategy(
                    1, // Max no of retries
                    Duration.ofMinutes(1), // Within what time period
                    DeciderBuilder.match(Exception.class, e -> {
                        System.out.println("ContactListSupervisorActor: Stupid child, do your job!");
                        return SupervisorStrategy.restart();
                    })
                    .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public Receive createReceive() {
        /*! In match() we get the class so that we will have the methods of that class at disposal in the lambda... */
        return receiveBuilder()
                .match(Props.class, props -> {
                    getSender().tell(getContext().actorOf(props), getSelf());
                }).build();
    }

    static Props props() {
        return Props.create(ContactListSupervisorActor.class);
    }

}
