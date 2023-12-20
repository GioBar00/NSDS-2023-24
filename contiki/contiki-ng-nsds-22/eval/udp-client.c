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

static struct simple_udp_connection udp_conn;

#define MAX_READINGS 10
#define SEND_INTERVAL (60 * CLOCK_SECOND)
#define FAKE_TEMPS 5

static struct simple_udp_connection udp_conn;

static float readings[MAX_READINGS];
static unsigned next_reading;
static unsigned len;


/*---------------------------------------------------------------------------*/
PROCESS(udp_client_process, "UDP client");
AUTOSTART_PROCESSES(&udp_client_process);

/*---------------------------------------------------------------------------*/
static unsigned
get_temperature() {
    static unsigned fake_temps[FAKE_TEMPS] = {30, 25, 20, 15, 10};
    return fake_temps[random_rand() % FAKE_TEMPS];
}

static void batch_val(float value) {
    readings[next_reading] = value;
    next_reading++;
    /*! In case of complete array, we overwrite cyclically, overwriting the oldest value */
    if (next_reading >= MAX_READINGS) {
        next_reading = 0;
    }
    if (len < MAX_READINGS) {
        len++;
    }
}

static float compute_average() {
    float sum = 0;
    for (int i = 0; i < len; i++) {
        if (readings[i] != 0) {
            sum = sum + readings[i];
        }
    }
    return sum / len;
}

/*---------------------------------------------------------------------------*/
static void
udp_rx_callback(struct simple_udp_connection *c,
                const uip_ipaddr_t *sender_addr,
                uint16_t sender_port,
                const uip_ipaddr_t *receiver_addr,
                uint16_t receiver_port,
                const uint8_t *data,
                uint16_t datalen) {
    // ...
}

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(udp_client_process, ev, data) {
    uip_ipaddr_t dest_ipaddr;
    static struct etimer periodic_timer;
    static float average;
    static float value;
    static bool batched;

    // initialize the readings
    next_reading = 0;
    len = 0;

    PROCESS_BEGIN();
                /* Initialize UDP connection */
                simple_udp_register(&udp_conn, UDP_CLIENT_PORT, NULL,
                                    UDP_SERVER_PORT, udp_rx_callback);
                /*! Sets the timer s.t. every SEND_INTERVAL reads the temperature and sends it to the server */
                etimer_set(&periodic_timer, SEND_INTERVAL);
                while (1) {
                    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&periodic_timer));
                    // get the temperature
                    value = (float) get_temperature();
                    /*! If connected sends the temperature every minute */
                    if (NETSTACK_ROUTING.node_is_reachable() && NETSTACK_ROUTING.get_root_ipaddr(&dest_ipaddr)) {
                        LOG_INFO("CLIENT - The server is reachable and batched: %d\n", batched);
                        if (len > 0) {
                            /*! If there are batched values compute the average of them */
                            LOG_INFO(
                                    "CLIENT - There are some locally stored values... Computing avg and sending it...\n");
                            average = compute_average();
                            /*! Sends the local average temperature */
                            LOG_INFO("CLIENT - Sending old local average: %f\n", average);
                            simple_udp_sendto(&udp_conn, &average, sizeof(average), &dest_ipaddr);
                            /*! Init the buffer */
                            next_reading = 0;
                            len = 0;
                            batched = false;
                            /**
                             * Yield. Therefore go in queue and waits to be scheduled after the other proto-threads
                             * By doing so, the buffer will be consumed
                             */
                            PROCESS_YIELD();
                        }
                        LOG_INFO("CLIENT - Sending to server reading % f\n", value);
                        simple_udp_sendto(&udp_conn, &value, sizeof(value), &dest_ipaddr);
                    }
                        /*! If the node is not reachable, therefore is disconnected, saves locally the lasts readings */
                    else {
                        batch_val(value);
                        LOG_INFO("CLIENT - The server is not reachable, I am disconnected... Storing locally. Batched: %d\n", batched);
                    }
                    etimer_set(&periodic_timer, SEND_INTERVAL);
                }

    PROCESS_END();
}

/*---------------------------------------------------------------------------*/
