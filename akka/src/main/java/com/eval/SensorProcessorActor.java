package com.eval;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class SensorProcessorActor extends AbstractActor {

    // current average temperature
    private double currentAverage;
    // number of messages used for compute the new average temperature
    private int messagesSeen;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(TemperatureMsg.class, this::gotData).build();
    }

    private void gotData(TemperatureMsg msg) throws Exception {

        System.out.println("SENSOR PROCESSOR " + self() + ": Got data from " + msg.getSender());

        if (msg.getTemperature() < 0) {
            // throws an exception if the value of the temperature is negative
            throw new Exception("Faulty reading generated by: " + msg.getSender() + " Sensor : " + getContext().getSelf().toString());
        } else {
            // compute the average and update the number of messages used for compute the temperature
            currentAverage = (currentAverage * messagesSeen + msg.getTemperature()) / (messagesSeen + 1);
            messagesSeen++;
        }

        System.out.println("SENSOR PROCESSOR " + self() + ": Current avg is " + currentAverage);
    }

    static Props props() {
        return Props.create(SensorProcessorActor.class);
    }

    public SensorProcessorActor() {
        this.currentAverage = 0;
        this.messagesSeen = 0;
    }
}
