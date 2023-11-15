package com.LabOne.ex_3;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.LabOne.DecrementMessage;
import com.LabOne.IncrementMessage;
import com.LabOne.MessageWithField;
import com.LabOne.ex_2.PositiveCounterActor;

import java.util.HashMap;

public class ContactsListActor extends AbstractActor{
    private final HashMap<String, String> contactList = new HashMap<>();

    public ContactsListActor(){}
    @Override
    public AbstractActor.Receive createReceive() {
        /*! Choose accordingly the method to call in case of the different kinds of messages */
        return receiveBuilder()
                .match(PutMsg.class, this::putContact)
                .match(GetMsg.class, this::getMsg)
                .build();
    }

    private void putContact(PutMsg msg) throws Exception {
        if(msg.getName().equals("Fail"))
            throw new Exception("Failuro Charm");
        contactList.put(msg.getName(),msg.getEmail());
        System.out.println("ContactListActor: put or replaced contact with name " + msg.getName() + " with email " + msg.getEmail());
    }

    /*! getMsg in case exists the key in the map returns its address, otherwise sends a no contact message*/
    private void getMsg(GetMsg msg){
        System.out.println("ContactListActor: received request for contact name " + msg.getName());
        if(contactList.containsKey(msg.getName()))
            sender().tell(new ReplyMsg(contactList.get(msg.getName())), self());
        else
            sender().tell(new ReplyMsg(), self());
    }

    static Props props() {
        return Props.create(ContactsListActor.class);
    }

}
