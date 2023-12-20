# Evaluation lab - Contiki-NG

## Group number: 33

## Group members

- Andrea Alari - 10707336
- Giovanni Barbiero - 10722159
- Nicolo' Caruso - 

## Solution description
In our solution the Client schedule an etimer with duration SEND_INTERVAL.
If the client, before sending the value, discovers that it is disconnected batches the value and reschedule the timer.
When the client discovers that is connected if it has batched temperatures compute the average of it, after sending the 
average yield. After the yield it sends to the server the new value it has read.
The Server once it receives a new value check that the sender was in the list of client it has already served, if not
check the number of clients that has served and in case there's availab
