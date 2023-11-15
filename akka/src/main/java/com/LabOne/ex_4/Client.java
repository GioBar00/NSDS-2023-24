package com.LabOne.ex_4;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.LabOne.ex_3.ContactsListActor;


public class Client {
    private static final int numThreads = 10;
    private static final int numMessages = 100;

    public static void main(String[] args) {

        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef client = sys.actorOf(ClientActor.props(), "client");
        final ActorRef server = sys.actorOf(ServerActor.props(), "server");
        /*! We send the server actor reference, specifying no sender since the main is not an actor...*/
        client.tell(server, ActorRef.noSender());
        client.tell(new Ping("Ciao1"), ActorRef.noSender());
        client.tell(new Sleep(), ActorRef.noSender());
        client.tell(new Ping("Ciao2"), ActorRef.noSender());
        client.tell(new Ping("Ciao3"), ActorRef.noSender());
        client.tell(new Wakeup(), ActorRef.noSender());

    }

}
