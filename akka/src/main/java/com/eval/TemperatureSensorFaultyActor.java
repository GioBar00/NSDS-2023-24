package com.eval;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class TemperatureSensorFaultyActor extends TemperatureSensorActor {

	private ActorRef dispatcher;
	private final static int FAULT_TEMP = -50;

	@Override
	public AbstractActor.Receive createReceive() {
		return receiveBuilder()
				.match(GenerateMsg.class, this::onGenerate)
				.match(ConfigMsg.class, this::configure)
				.build();
	}

	private void onGenerate(GenerateMsg msg) {
		System.out.println("TEMPERATURE SENSOR "+self()+": Sensing temperature!");
		dispatcher.tell(new TemperatureMsg(FAULT_TEMP,self()), self());
	}

	// set the address of the dispatcher
	private void configure(ConfigMsg msg) {
		System.out.println("TEMPERATURE SENSOR "+self()+": Received configuration message!");
		dispatcher = msg.getDispatcherRef();
	}

	static Props props() {
		return Props.create(TemperatureSensorFaultyActor.class);
	}

}
