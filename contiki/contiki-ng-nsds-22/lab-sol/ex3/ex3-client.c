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
#define STOP 10

static struct simple_udp_connection udp_conn;

#define START_INTERVAL        (15 * CLOCK_SECOND)
#define SEND_INTERVAL          (60 * CLOCK_SECOND)

static struct simple_udp_connection udp_conn;

/*---------------------------------------------------------------------------*/
PROCESS(udp_client_process, "UDP client");
AUTOSTART_PROCESSES(&udp_client_process);
/*---------------------------------------------------------------------------*/
static bool done = false;
static struct etimer timer;
static unsigned count = 0;

static void
udp_rx_callback(struct simple_udp_connection *c,
                const uip_ipaddr_t *sender_addr,
                uint16_t sender_port,
                const uip_ipaddr_t *receiver_addr,
                uint16_t receiver_port,
                const uint8_t *data,
                uint16_t datalen) {
    etimer_restart(&timer);
    LOG_INFO("CallbackReached!\n");
    if (count > STOP) {
        LOG_INFO_("Treshold reached!\n");
        done = true;
        return;
    }
    unsigned temp = *(unsigned *) data;
    LOG_INFO("Received response %u from ", temp);
    LOG_INFO_6ADDR(sender_addr);
    LOG_INFO_("\n");
    if (temp == (count + 1)) {
        count = temp + 1;
        LOG_INFO("Sending response %u to ", count);
        LOG_INFO_6ADDR(sender_addr);
        LOG_INFO_("\n");
        simple_udp_sendto(&udp_conn, &count, sizeof(count), sender_addr);
    } else {
        LOG_INFO("Ignore wrong packet in sequence order!\n");
    }
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(udp_client_process, ev, data) {
    static struct etimer periodic_timer;
    uip_ipaddr_t dest_ipaddr;

    PROCESS_BEGIN();
                /* Initialize UDP connection */
                simple_udp_register(&udp_conn, UDP_CLIENT_PORT, NULL,
                                    UDP_SERVER_PORT, udp_rx_callback);
                etimer_set(&periodic_timer, random_rand() % SEND_INTERVAL);
                /* Loop until you reach the root of the DAG */
                while (1) {
                    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&periodic_timer));
                    if (NETSTACK_ROUTING.node_is_reachable() && NETSTACK_ROUTING.get_root_ipaddr(&dest_ipaddr)) {
                        /* Send to DAG root */
                        LOG_INFO("Sending request %u to ", count);
                        LOG_INFO_6ADDR(&dest_ipaddr);
                        LOG_INFO_("\n");
                        etimer_set(&timer, CLOCK_SECOND * 60);
                        simple_udp_sendto(&udp_conn, &count, sizeof(count), &dest_ipaddr);
                        etimer_stop(&periodic_timer);
                        break;
                    } else {
                        LOG_INFO("Not reachable yet\n");
                    }
                    /* Add some jitter */
                    etimer_set(&periodic_timer, SEND_INTERVAL
                                                - CLOCK_SECOND + (random_rand() % (2 * CLOCK_SECOND)));
                }
                /* Stop the cycle and the thread once the last packet has been received */
                while (!done) {
                    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&timer));
                    etimer_set(&timer, CLOCK_SECOND * 60);
                    LOG_INFO("Timer expired, packet lost. Restart ping pong.\n");
                    /* If the timer has been resetted try to recontact the server and set count = 0 */
                    count = 0;
                    simple_udp_sendto(&udp_conn, &count, sizeof(count), &dest_ipaddr);
                }

    PROCESS_END();
}
/*---------------------------------------------------------------------------*/
