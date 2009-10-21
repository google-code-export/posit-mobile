
/*
 * rwg_send.h 
 *
 */

#include<stdlib.h>
#include<stdio.h>
#include<string.h>
#include<netinet/if_ether.h> 
#include<net/ethertypes.h>
#include<netinet/ether.h> 
#include<arpa/inet.h>
#include<netinet/in.h>
#include<sys/socket.h>
#include<sys/ioctl.h>
#include<errno.h>
#include<net/if.h>

#include "rwg_sockets.h"
#include "rwg_header.h"
#include "rwg.h"
#include "rwg_header.h"


/*FUNC: The function that is sending all the raw packets*/
int rwg_send_raw(int rawsock, unsigned char *pkt, int pkt_len);

/*FUNC: matches against packets in the REQF packet buffer, returns a position in the buffer
  if there is a match, if there is no matchor buffer is empty it returns -1 */
int rwg_match_packetid(unsigned char packet_id[8],packetBuffer *packetBuff);

/*FUNC: creates new packet with type REQF*/
unsigned char* rwg_create_reqf_n(packetBuffer *packetBuff);

/*FUNC: forwards old REQF after getting an OKTF or sending a REQF from the wake buffer*/
unsigned char* rwg_create_reqf_f(packetBuffer *packetBuff);

/*FUNC: forwards a random REFQ, when network is silent*/ 
unsigned char* rwg_create_reqf_r(packetBuffer *packetBuff);

/*FUNC: creates packets with type ACK*/
unsigned char* rwg_create_ack(packetBuffer *packetBuff);

/*FUNC: creates packets with type OKTF*/
unsigned char* rwg_create_oktf(packetBuffer *packetBuff);

/*FUNC: creates packets with type BS*/
unsigned char* rwg_create_bs(packetBuffer* packetBuff);

/*FUNC: handles the send routine*/
int rwg_send_routine(int socket, packetBuffer *packetBuff);
