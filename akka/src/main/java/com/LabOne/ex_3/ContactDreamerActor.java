package com.LabOne.ex_3;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.LabOne.DecrementMessage;
import com.LabOne.IncrementMessage;
import com.LabOne.MessageWithField;
import com.faultTolerance.counter.CounterActor;
import com.faultTolerance.counter.DataMessage;

import java.util.concurrent.TimeoutException;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ContactDreamerActor extends AbstractActor {

    private ActorRef contactsList;

    public ContactDreamerActor(){}

    @Override
    public Receive createReceive() {
        /*! Choose accordingly the method to call in case of the different kinds of messages */
        return receiveBuilder()
                .match(GetMsg.class, this::handleMessage)
                .match(PutMsg.class, this::handleMessage)
                .match(ActorRef.class, ref -> {this.contactsList = ref;})
                .build();
    }

    private void handleMessage(GetMsg msg) {
        scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);
        try{
            scala.concurrent.Future<Object> waitingForReply = ask(contactsList, msg, 5000);
            ReplyMsg rmsg = (ReplyMsg) waitingForReply.result(timeout, null);
            if (rmsg.isEmpty())
                System.out.println("ContactDreamerActor: no email for contact " + msg.getName());
            else
                System.out.println("ContactDreamerActor: the email of the contact " + msg.getName() + " is " + rmsg.getEmail());

        } catch (TimeoutException | InterruptedException e1) {

            e1.printStackTrace();
        }
    }
    private void handleMessage(PutMsg msg)
    {
        contactsList.tell(msg, self());
    }
    public static Props props() {
        return Props.create(ContactDreamerActor.class);
    }

}
