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
check the number of clients that has served and in case there's an available slot registers the new client and serves its
request.
In order to serve a request the server has a static list of values that has received, if the list is full it removes the 
last value and adds the new one.
In our solution the server is not responsive, since it is not request we do not guarantee any type of delivery (at most 
once, exactly one...) and the client if it cannot be served keep acting as a normal client (it doesn't terminate its normal
execution).
Since the program should be as light as possible we decided to schedule a single timer that triggers both the reading and 
the sending. To guarantee the order of sends the process yield after the call of send of the average value.
