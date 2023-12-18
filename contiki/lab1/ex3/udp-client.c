#include "contiki.h"
#include "net/routing/routing.h"
#include "random.h"
#include "net/netstack.h"
#include "net/ipv6/simple-udp.h"

#include "sys/log.h"

#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_INFO

#define WITH_SERVER_REPLY  1
#define UDP_CLIENT_PORT    8765
#define UDP_SERVER_PORT    5678
#define MAX_COUNT 10

static struct simple_udp_connection udp_conn;

#define START_INTERVAL        (15 * CLOCK_SECOND)
#define SEND_INTERVAL          (60 * CLOCK_SECOND)

static struct simple_udp_connection udp_conn;

static unsigned count;
static bool received_pong = false;
static struct etimer periodic_timer;
static struct etimer pong_timer;

/*---------------------------------------------------------------------------*/
PROCESS(udp_client_process, "UDP client");
AUTOSTART_PROCESSES(&udp_client_process);
/*---------------------------------------------------------------------------*/
/*! Callback just show things! */
static void
pong_callback(struct simple_udp_connection *c,
              const uip_ipaddr_t *sender_addr,
              uint16_t sender_port,
              const uip_ipaddr_t *receiver_addr,
              uint16_t receiver_port,
              const uint8_t *data,
              uint16_t datalen) {

    unsigned countReceived = *(unsigned *) data;
    if(count==countReceived){
        received_pong = true;
        /*! Stop the timer that says if the */
        count++;
    }
    LOG_INFO("Received response %u from ", count);
    LOG_INFO_6ADDR(sender_addr);
    LOG_INFO_("\n");
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(udp_client_process, ev, data) {

    uip_ipaddr_t dest_ipaddr;

    PROCESS_BEGIN();

                /* Initialize UDP connection */
                simple_udp_register(&udp_conn, UDP_CLIENT_PORT, NULL,
                                    UDP_SERVER_PORT, pong_callback);
                received_pong = true;
                etimer_set(&periodic_timer, random_rand() % SEND_INTERVAL);
                while (count < MAX_COUNT) {
                    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&periodic_timer));
                    /*! If there is a parent can communicate, otherwise no! */
                    if (NETSTACK_ROUTING.node_is_reachable() && NETSTACK_ROUTING.get_root_ipaddr(&dest_ipaddr)) {
                        /* Send to DAG root */
                        LOG_INFO("Sending request %u to ", count);
                        LOG_INFO_6ADDR(&dest_ipaddr);
                        LOG_INFO_("\n");
                        simple_udp_sendto(&udp_conn, &count, sizeof(count), &dest_ipaddr);
                    } else {
                        LOG_INFO("Not reachable yet\n");
                    }

                    /* Add some jitter */
                    etimer_set(&periodic_timer, SEND_INTERVAL
                                                - CLOCK_SECOND + (random_rand() % (2 * CLOCK_SECOND)));
                }

    PROCESS_END();
}
/*---------------------------------------------------------------------------*/
