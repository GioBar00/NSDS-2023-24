package com.eval;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;


import java.time.Duration;
import java.util.*;

public class DispatcherActor extends AbstractActorWithStash {

    private final static int NO_PROCESSORS = 2;

    /*! Queue used for RoundRobin dispatch policy */
    private final Queue<ActorRef> roundRobinStack = new LinkedList<>();
    /*! Map temperature sensor -> processor for load balancing */
    private final Map<ActorRef, ActorRef> loadBalancerMap = new HashMap<>();
    /*! Map for the referenced nr. of sensors for each processor for load balancing */
    private final Map<ActorRef, Integer> sensorsMappedToProcessor = new HashMap<>();

    /* The strategy of the supervisor is to resume the computation for the failed SensorDataProcess(es)
       By doing so, the old average will be retained and the computation resumed. */
    private static SupervisorStrategy strategy =
            new OneForOneStrategy(
                    1,
                    Duration.ofMinutes(1),
                    DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume()).build()
            );

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    public DispatcherActor() {
        for (int i = 0; i < NO_PROCESSORS; i++) {
            // create the sensor processors
            ActorRef processor = getContext().actorOf(SensorProcessorActor.props());
            // initialize the queue and map
            roundRobinStack.add(processor);
            sensorsMappedToProcessor.put(processor, 0);
        }
    }


    @Override
    public AbstractActor.Receive createReceive() {
        System.out.println("DISPATCHER: Started in load balancer mode!");
        return loadBalance();
    }

    /*! Round Robin policy behavior */
    private Receive roundRobin() {
        return receiveBuilder()
                .match(TemperatureMsg.class, this::dispatchDataRoundRobin)
                .match(DispatchLogicMsg.class, msg -> {
                    // change behavior only if specified dispatch logic is different from the current one
                    if (msg.getLogic() == DispatchLogicMsg.LOAD_BALANCER) {
                        getContext().become(loadBalance());
                        System.out.println("DISPATCHER: Changed behavior (LOAD BALANCING)!");
                    }
                })
                .build();
    }

    /*! Load balancer behaviour */
    private Receive loadBalance() {
        return receiveBuilder()
                .match(TemperatureMsg.class, this::dispatchDataLoadBalancer)
                .match(DispatchLogicMsg.class, msg -> {
                    // change behavior only if specified dispatch logic is different from the current one
                    if (msg.getLogic() == DispatchLogicMsg.ROUND_ROBIN) {
                        getContext().become(roundRobin());
                        System.out.println("DISPATCHER: Changed behavior (ROUND ROBIN)!");
                    }
                })
                .build();
    }

    /*! Dispatcher function in case of load-balancing mode */
    private void dispatchDataLoadBalancer(TemperatureMsg msg) {
        if (!loadBalancerMap.containsKey(msg.getSender())) {
            // assign new temperature sensor to the processor with the lowest load
            int min = Collections.min(sensorsMappedToProcessor.values());
            for (Map.Entry<ActorRef, Integer> entry : sensorsMappedToProcessor.entrySet()) {
                if (entry.getValue() == min) {
                    // update the map
                    sensorsMappedToProcessor.put(entry.getKey(), min + 1);
                    loadBalancerMap.put(msg.getSender(), entry.getKey());
                    // send the message to the processor
                    entry.getKey().tell(msg, msg.getSender());
                    return;
                }
            }
        } else
            loadBalancerMap.get(msg.getSender()).tell(msg, self());
    }

    /*! Dispatcher function in cases of Round Robin mode */
    private void dispatchDataRoundRobin(TemperatureMsg msg) {
        ActorRef dest = roundRobinStack.poll();
        if (dest == null) {
            System.out.println("DISPATCHER: No processors available!");
            return;
        }
        dest.tell(msg, self());
        roundRobinStack.add(dest);
    }

    static Props props() {
        return Props.create(DispatcherActor.class);
    }
}
