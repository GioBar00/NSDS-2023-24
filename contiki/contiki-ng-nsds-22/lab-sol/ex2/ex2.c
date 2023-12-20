#include "contiki.h"

#include <stdio.h> /* For printf() */

/*---------------------------------------------------------------------------*/
static void ctimer_callback(void *data);

#define CTIMER_INTERVAL (2 * CLOCK_SECOND)
static struct ctimer print_ctimer;

/*---------------------------------------------------------------------------*/
static void rtimer_callback(struct rtimer *t, void *data);

#define RTIMER_HARD_INTERVAL (2 * RTIMER_SECOND)
static struct rtimer print_rtimer;
/*---------------------------------------------------------------------------*/
PROCESS(hello_world_ctimer, "Hello world process");
AUTOSTART_PROCESSES(&hello_world_ctimer);

/*---------------------------------------------------------------------------*/
static void ctimer_callback(void *data) {
    printf("%s", (char *) data);
}

/*---------------------------------------------------------------------------*/
static void rtimer_callback(struct rtimer *t, void *data) {

    printf("%s", (char *) data);

    /* Schedule the ctimer */
    ctimer_set(&print_ctimer, 0, ctimer_callback, "Hello world CT\n");

    /* Reschedule the rtimer */
    rtimer_set(&print_rtimer, RTIMER_NOW() + RTIMER_HARD_INTERVAL, 0, rtimer_callback, "Hello world RT\n");
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(hello_world_ctimer, ev, data) {
    PROCESS_BEGIN();

                rtimer_init();

                /* Schedule the rtimer: absolute time */
                rtimer_set(&print_rtimer, RTIMER_NOW() + RTIMER_HARD_INTERVAL, 0, rtimer_callback, "Hello world RT\n");

                /* Only useful for platform native */
                PROCESS_WAIT_EVENT();

    PROCESS_END();
}
