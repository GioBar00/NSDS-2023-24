package com.LabOne.ex_1;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.LabOne.DecrementMessage;
import com.LabOne.IncrementMessage;
import com.LabOne.MessageWithField;
import com.counter.CounterActor;

public class AdvancedCounterActor extends AbstractActor{
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
        System.out.println("Counter increased to " + counter);
    }

    void onDecrement(DecrementMessage msg) {
        --counter;
        System.out.println("Counter decreased to " + counter);
    }

    void onMessage(MessageWithField msg){
        if(msg.isIncremental())
            ++counter;
        else
            --counter;
    }

    static Props props() {
        return Props.create(AdvancedCounterActor.class);
    }
}
