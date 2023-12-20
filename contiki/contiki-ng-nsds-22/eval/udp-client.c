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
static struct ctimer send_timer;
static float readings[MAX_READINGS];
static int stored_readings;
static unsigned len = 0;
static bool batched;


/*---------------------------------------------------------------------------*/
PROCESS(udp_client_process, "UDP client");
AUTOSTART_PROCESSES(&udp_client_process);

/*---------------------------------------------------------------------------*/
static unsigned
get_temperature() {
    static unsigned fake_temps[FAKE_TEMPS] = {30, 25, 20, 15, 10};
    return fake_temps[random_rand() % FAKE_TEMPS];
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

/*! Callback called every minute by the ctimer for sending the temperature to server */
static void send_temperature(){
    uip_ipaddr_t dest_ipaddr;
    // get the temperature
    float value = (float)get_temperature();
    /*! If connected sends the temperature every minute */
    if(NETSTACK_ROUTING.node_is_reachable()&& NETSTACK_ROUTING.get_root_ipaddr(&dest_ipaddr)) {
        LOG_INFO("CLIENT - The server is reachable");
        if (batched) {
            LOG_INFO("CLIENT - There are some locally stored values... Computing avg and sending it...");
            // send the single value
            unsigned sum = 0;
            unsigned no = 0;
            for (int i = 0; i < len; i++) {
                if (readings[i] != 0) {
                    sum = sum + readings[i];
                    no++;
                }
            }
            /*! Adds the last temperature reading */
            sum += value;
            no++;
            value = ((float) sum) / no;
            // free the queue
            len = 0;
            batched = false;
        }
        LOG_INFO("CLIENT - Sending to server a reading");
        LOG_INFO("Sending reading: %f", value);
        simple_udp_sendto(&udp_conn, &value, sizeof(value), &dest_ipaddr);
    }
    /*! If the node is not reachable, therefore is disconnected, saves locally the lasts readings */
    else{
        LOG_INFO("CLIENT - The server is not reachable, I am disconnected... Storing locally");
        batched = true;
        readings[stored_readings] = value;
        stored_readings++;
        if (stored_readings >= MAX_READINGS) {
            stored_readings = 0;
        }
        if (len < MAX_READINGS) {
            len++;
        }
    }
    ctimer_set(&send_timer, SEND_INTERVAL, send_temperature, NULL);
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(udp_client_process, ev, data) {

    PROCESS_BEGIN();
                stored_readings = 0;
                batched = false;
                /* Initialize UDP connection */
                simple_udp_register(&udp_conn, UDP_CLIENT_PORT, NULL,
                                    UDP_SERVER_PORT, udp_rx_callback);
                LOG_INFO("CLIENT - I have registered an udp connection to the server");
                /*! Sets the timer s.t. every SEND_INTERVAL reads the temperature and sends it to the server */
                ctimer_set(&send_timer, SEND_INTERVAL, send_temperature, NULL);

    PROCESS_END();
}

/*---------------------------------------------------------------------------*/
