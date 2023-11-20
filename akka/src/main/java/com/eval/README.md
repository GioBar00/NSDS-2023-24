# Evaluation lab - Akka

## Group number: 33

## Group members

- Andrea Alari - 10707336
- Giovanni Barbiero - 10722159
- Nicolò Caruso - 10705618

## Description of message flows

The message flow are between the Temperature sensors and the dispatcher and between the dispatcher and the processors.
The sensors will receive the configuration message which pass to them the reference to the dispatcher.
The sensors send the TemperatureMsg, which contains the temperature readings and the reference of the sensor after receiving a GenerateMsg from the main.
The Dispatcher receives the policy through the DispatchLogicMsg and Temperature message.
As soon as the Dispatcher is created its assigned strategy is Load Balancing, moreover the SensorProcessorActors are spawned when the Dispatcher is instantiated.
The main may send a message DispatchLogicMsg to configure the dispatcher's policy (Round Robin or Load Balancing).
In case of a DispatchLogicMsg, the dispatcher does change the behavior to the chosen policy (RR or LB).
In case of a TemperatureMsg, the dispatcher will, in case of LoadBalancing, update the local maps for load balancing and 
send the message to the correct SensorDataProcessor.
The SensorProcessorActor just receives the TemperatureMsg from the dispatcher and process it.