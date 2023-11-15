package com.LabOne.ex_2;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;
import com.LabOne.DecrementMessage;
import com.LabOne.IncrementMessage;
import com.LabOne.MessageWithField;

/*! PositiveCounterActor class. It extends AbstractActorWithStash and by doing so it has the stash, unstash functions */
public class PositiveCounterActor extends AbstractActorWithStash {
    private int counter;

    public void CounterActor() {
        this.counter = 0;
    }

    @Override
    public Receive createReceive() {
        /*! Choose accordingly the method to call in case of the different kinds of messages */
        return receiveBuilder()
                .match(IncrementMessage.class, this::onIncrement)
                .match(DecrementMessage.class, this::onDecrement)
                .match(MessageWithField.class, this::onMessage)
                .build();
    }

    void onIncrement(IncrementMessage msg) {
        ++counter;
        unstash();
        System.out.println("Counter increased to " + counter);
    }

    void onDecrement(DecrementMessage msg) {
        if(counter == 0) {
            stash();
            return;
        }
        --counter;
        System.out.println("Counter decreased to " + counter);
    }

    void onMessage(MessageWithField msg){
        if(msg.isIncremental()) {
            ++counter;
            unstash();
            System.out.println("Processed increment");
        }
        else if(counter > 0) {
            --counter;
            System.out.println("Processed decrement");
        }
        else
            stash();
    }

    /*! SEE that it is correctly set the class! Otherwise, it gives problems!*/
    static Props props() {
        return Props.create(PositiveCounterActor.class);
    }
}
