package com.LabOne.ex_3;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.LabOne.DecrementMessage;
import com.LabOne.IncrementMessage;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactsList {

    private static final int numThreads = 10;
    private static final int numMessages = 100;

    public static void main(String[] args) {

        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef contactsList = sys.actorOf(ContactsListActor.props(), "contactsList");
        final ActorRef contactDreamer = sys.actorOf(ContactDreamerActor.props(), "contactDreamer");
        /*! We send the contact list actor reference, specifying no sender since the main is not an actor...*/
        contactDreamer.tell(contactsList, ActorRef.noSender());



        contactDreamer.tell(new PutMsg("Bill Jobs", "info@example.com"),ActorRef.noSender());
        contactDreamer.tell(new GetMsg("Bill Jobs"), ActorRef.noSender());
        contactDreamer.tell(new GetMsg("Billy Works"), ActorRef.noSender());


    }
}