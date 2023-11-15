
package com.LabOne.ex_5;

import akka.actor.*;
import com.LabOne.ex_3.ContactDreamerActor;
import com.LabOne.ex_3.ContactsListActor;
import com.LabOne.ex_3.GetMsg;
import com.LabOne.ex_3.PutMsg;

import java.util.concurrent.TimeoutException;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ContactListSupervisor {

    public static final int NORMAL_OP = 0;
    public static final int FAULT_OP = -1;

    public static final int FAULTS = 1;

    public static void main(String[] args) {
        scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef supervisor = sys.actorOf(ContactListSupervisorActor.props(), "supervisor");
        final ActorRef contactDreamer = sys.actorOf(ContactDreamerActor.props(),"contactDreamer");

        ActorRef contactList;
        try {

            // Asks the supervisor to create the child actor and returns a reference
            scala.concurrent.Future<Object> waitingForContactList = ask(supervisor, Props.create(ContactsListActor.class), 5000);
            contactList = (ActorRef) waitingForContactList.result(timeout, null);
            contactDreamer.tell(supervisor, ActorRef.noSender());

            contactDreamer.tell(new PutMsg("hello", "hello@mai.it"), ActorRef.noSender());

            contactDreamer.tell(new PutMsg("Fault!", ""), ActorRef.noSender());

            contactDreamer.tell(new GetMsg("hello"), ActorRef.noSender());

            sys.terminate();

        } catch (TimeoutException | InterruptedException e1) {
            e1.printStackTrace();
        }

    }

}
