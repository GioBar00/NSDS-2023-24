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
#include <stdlib.h>/* For rand() */
#include <string.h>
#include <stdbool.h>
#include "time.h"

#define MAX_LEN 5
#define MAX_FOR_TIMER 2
/*---------------------------------------------------------------------------*/
PROCESS(producer_process, "Producer process");

PROCESS(consumer_process, "Consumer process");
AUTOSTART_PROCESSES(&producer_process, &consumer_process);
/*---------------------------------------------------------------------------*/
// Global variables
static int queue[MAX_LEN];
static int queue_len = 0;
static bool initialized = false;

static process_event_t produced_event;
static process_event_t consumed_event;
/*---------------------------------------------------------------------------*/
// Static functions
static void init_all() {
    memset(queue, 0, MAX_LEN * sizeof(int));
    srand(time(NULL));
}

static void q_print() {
    printf("## queue -> [");
    for (int i = 0; i < queue_len - 1; i++) {
        printf("%d,  ", queue[i]);
    }
    if (queue_len > 0) {
        printf("%d]\n\n", queue[queue_len - 1]);
    } else {
        printf("]\n\n");
    }
}

static bool q_is_full() {
    return queue_len == MAX_LEN;
}

static bool q_is_empty() {
    return queue_len == 0;
}

static void q_enqueue(int val) {
    if (q_is_full()) {
        return;
    }
    queue[queue_len] = val;
    queue_len++;
}

static int q_dequeue() {
    int val = queue[0];
    memmove(queue, queue + 1, (queue_len - 1) * sizeof(int));
    queue_len--;
    return val;
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(producer_process, ev, data) {
    static struct etimer timer;

    if (!initialized) {
        init_all();
        initialized = true;
    }

    PROCESS_BEGIN()
                produced_event = process_alloc_event();
                // used only for the first iteration
                etimer_set(&timer, CLOCK_SECOND * (rand() % MAX_FOR_TIMER + 1));

                while (1) {
                    if (q_is_full()) {
                        printf("[prod] queue FULL -> waiting for consumer\n");
                        PROCESS_WAIT_EVENT_UNTIL(ev == consumed_event);
                    } else {
                        PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&timer));
                        printf("[prod] new element pushed -> random timer + notify consumer\n");
                        q_enqueue(rand() % 100);
                        q_print();
                        process_post(&consumer_process, produced_event, NULL);
                    }
                    etimer_set(&timer, CLOCK_SECOND * (rand() % MAX_FOR_TIMER + 1));
                }

    PROCESS_END()
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(consumer_process, ev, data) {
    static struct etimer timer;

    if (!initialized) {
        init_all();
        initialized = true;
    }



    PROCESS_BEGIN()

                consumed_event = process_alloc_event();
                // used only for the first iteration
                etimer_set(&timer, CLOCK_SECOND * (rand() % MAX_FOR_TIMER + 1));

                while (1) {
                    if (q_is_empty()) {
                        printf("[cons] queue EMPTY -> waiting for producer\n");
                        PROCESS_WAIT_EVENT_UNTIL(ev == produced_event);
                    } else {
                        PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&timer));
                        printf("[cons] new element popped -> random timer + notify producer\n");
                        q_dequeue();
                        q_print();
                        process_post(&producer_process, consumed_event, NULL);
                    }
                    etimer_set(&timer, CLOCK_SECOND * (rand() % MAX_FOR_TIMER + 1));
                }

    PROCESS_END()
}
/*---------------------------------------------------------------------------*/
