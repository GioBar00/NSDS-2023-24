/*
 * Copyright (c) 2006, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This file is part of the Contiki operating system.
 *
 */

/**
 * \file
 *         A very simple Contiki application showing how Contiki programs look
 * \author
 *         Adam Dunkels <adam@sics.se>
 */

#include "contiki.h"

#include <stdio.h> /* For printf() */

#define MAX_QUEUE 100
/*---------------------------------------------------------------------------*/
PROCESS(ex1_consumer_process, "Hello world process");

PROCESS(ex1_producer_process, "Hello world process");
AUTOSTART_PROCESSES(&ex1_producer_process, &ex1_consumer_process);

/*---------------------------------------------------------------------------*/
static process_event_t queue_add_event;
static process_event_t queue_pop_event;
static int queue[MAX_QUEUE];
static int size = 0;
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(ex1_producer_process, ev, data) {
    PROCESS_BEGIN();

    queue_add_event = process_alloc_event();
        while (1) {
            printf("Producer go brrrr\n");
            if (size < MAX_QUEUE - 1){
                printf("Producer: I have produced something hihi\n");
                queue[size] = 1;
                size ++ ;
                process_post(&ex1_consumer_process,queue_add_event, NULL);
                PROCESS_PAUSE();
            }
            else{
                PROCESS_WAIT_EVENT_UNTIL(ev = queue_pop_event);
            }

        }

    PROCESS_END();
}

/*---------------------------------------------------------------------------*/

PROCESS_THREAD(ex1_consumer_process, ev, data) {
    PROCESS_BEGIN();
        int got_message;
        while (1) {
            PROCESS_PAUSE();
            queue_pop_event = process_alloc_event();
            printf("Consumer go brrrr\n");
            if(size>0)
            {
                got_message = queue[size];
                size--;
                printf("Consumer got message: %d", got_message);
                if(size==MAX_QUEUE-1){
                    process_post(&ex1_producer_process,queue_pop_event, NULL);
                }
            }
            else{
                printf("No messages, sad consumer :(");
                PROCESS_WAIT_EVENT_UNTIL(ev == queue_add_event);
            }
        }

    PROCESS_END();
}


/*---------------------------------------------------------------------------*/