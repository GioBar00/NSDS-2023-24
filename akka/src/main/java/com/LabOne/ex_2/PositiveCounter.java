package com.LabOne.ex_2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.LabOne.DecrementMessage;
import com.LabOne.IncrementMessage;
import com.LabOne.MessageWithField;
import com.LabOne.ex_1.AdvancedCounterActor;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PositiveCounter {

    private static final int numThreads = 10;
    private static final int numMessages = 100;

    public static void main(String[] args) {

        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef counter = sys.actorOf(PositiveCounterActor.props(), "counter");

        // Send messages from multiple threads in parallel
        final ExecutorService exec = Executors.newFixedThreadPool(numThreads);

        /*exec.submit(() -> counter.tell(new MessageWithField(false), ActorRef.noSender()));
        exec.submit(() -> counter.tell(new MessageWithField(false), ActorRef.noSender()));
        exec.submit(() -> counter.tell(new MessageWithField(true), ActorRef.noSender()));
        exec.submit(() -> counter.tell(new MessageWithField(false), ActorRef.noSender()));
        exec.submit(() -> counter.tell(new MessageWithField(true), ActorRef.noSender()));
        exec.submit(() -> counter.tell(new MessageWithField(true), ActorRef.noSender()));*/

        exec.submit(() -> counter.tell(new DecrementMessage(), ActorRef.noSender()));
        exec.submit(() -> counter.tell(new DecrementMessage(), ActorRef.noSender()));
        exec.submit(() -> counter.tell(new IncrementMessage(), ActorRef.noSender()));
        exec.submit(() -> counter.tell(new DecrementMessage(), ActorRef.noSender()));
        exec.submit(() -> counter.tell(new IncrementMessage(), ActorRef.noSender()));
        exec.submit(() -> counter.tell(new IncrementMessage(), ActorRef.noSender()));

        /*for (int i = 0; i < numMessages; i++) {
            exec.submit(() -> counter.tell(new DecrementMessage(), ActorRef.noSender()));
            exec.submit(() -> counter.tell(new IncrementMessage(), ActorRef.noSender()));
            exec.submit(() -> counter.tell(new MessageWithField(true), ActorRef.noSender()));

        }*/

        // Wait for all messages to be sent and received
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        exec.shutdown();
        sys.terminate();

    }

}