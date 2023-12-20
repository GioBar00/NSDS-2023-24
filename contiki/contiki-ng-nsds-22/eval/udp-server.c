/*
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

#include "contiki.h"
#include "net/routing/routing.h"
#include "net/netstack.h"
#include "net/ipv6/simple-udp.h"

#include "sys/log.h"
#include "random.h"

#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_INFO

#define UDP_CLIENT_PORT    8765
#define UDP_SERVER_PORT    5678
#define MAX_RECEIVERS 10
#define MAX_READINGS 10

#define ALERT_THRESHOLD 19

static struct simple_udp_connection udp_conn;

static float readings[MAX_READINGS];
static unsigned next_reading = 0;
static unsigned len = 0;

static uip_ipaddr_t receivers[MAX_RECEIVERS];
static unsigned receivers_len = 0;

PROCESS(udp_server_process, "UDP server");
AUTOSTART_PROCESSES(&udp_server_process);

/*---------------------------------------------------------------------------*/
static void add_value(float value) {
    readings[next_reading] = value;
    next_reading++;
    if (next_reading >= MAX_READINGS) {
        next_reading = 0;
    }
    if (len < MAX_READINGS) {
        len++;
    }
}

static bool can_process_client(const uip_ipaddr_t *sender_addr) {
    // check if client is already registered
    for (int i = 0; i < receivers_len; i++) {
        if (uip_ipaddr_cmp(&receivers[i], sender_addr)) {
            return true;
        }
    }
    // check if there is space for new client
    if (receivers_len < MAX_RECEIVERS) {
        LOG_INFO("Added new client ");
        LOG_INFO_6ADDR(sender_addr);
        LOG_INFO_("\n");
        LOG_INFO_("Available slots %d\n", MAX_RECEIVERS - receivers_len);
        uip_ipaddr_copy(&receivers[receivers_len], sender_addr);
        receivers_len++;
        return true;
    }
    return false;
}

static void
udp_rx_callback(struct simple_udp_connection *c,
                const uip_ipaddr_t *sender_addr,
                uint16_t sender_port,
                const uip_ipaddr_t *receiver_addr,
                uint16_t receiver_port,
                const uint8_t *data,
                uint16_t datalen) {

    // check if the client can be processed
    if (!can_process_client(sender_addr)) {
        LOG_INFO("Client ");
        LOG_INFO_6ADDR(sender_addr);
        LOG_INFO(" ignored\n");
        return;
    }

    float reading = *(float *) data;

    LOG_INFO("Received %f from ", reading);
    LOG_INFO_6ADDR(sender_addr);
    LOG_INFO_("\n");

    batch_val(reading);

    /* Compute average */
    float average;
    float sum = 0;
    for (int i = 0; i < len; i++) {
        if (readings[i] != 0) {
            sum = sum + readings[i];
        }
    }
    average = sum / len;
    LOG_INFO("Current average is %f \n", average);

    /// ...

}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(udp_server_process, ev, data) {
    PROCESS_BEGIN();

                /* Initialize temperature buffer */
                next_reading = 0;
                len = 0;

                /* Initialize receivers buffer */
                receivers_len = 0;

                /* Initialize DAG root */
                NETSTACK_ROUTING.root_start();

                /* Initialize UDP connection */
                simple_udp_register(&udp_conn, UDP_SERVER_PORT, NULL,
                                    UDP_CLIENT_PORT, udp_rx_callback);

    PROCESS_END();
}
/*---------------------------------------------------------------------------*/
