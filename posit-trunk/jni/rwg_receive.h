
/*
 * receive.h, Handles incoming packets
 *
 */

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netinet/if_ether.h> 
#include <net/ethertypes.h>
#include <netinet/ether.h> 
#include <sys/time.h>
#include "rwg_header.h"
#include "rwg_send.h"
#include "rwg.h"

/*FUNC: Just for printing out packets in hex to terminal, is used in trace mode*/
void printPacket(unsigned char* buffer,int length);

/*FUNC: handles control messages*/
int rwg_handle_control(char* ctrlMess, int length, packetBuffer *packetBuff);

/*FUNC: takes a pointer to a time stamp and sets the current time*/
void set_time_stamp(t_stamp *t);

/*FUNC: takes two stamps and an interval(us) and checks if stamp1+interval < stamp2*/
int check_time_stamp(t_stamp stamp1,t_stamp stamp2, unsigned int interval);

/*FUNC: takes payload from received REFQ and writes it to the output buffer*/
int rwg_write_output(unsigned char* outputBuffer, int outPipe, int length);

/*FUNC: It's a hash function, used for the vectors visited n' recentVisited*/
int rwg_hash(unsigned int address);

/*FUNC: looks up a position in the bit vector, returns 0 if 0 and 1 if 1*/
int rwg_bitvector_lookup(unsigned short int *bitVector, unsigned short int pos);

/*FUNC: sets a position in the bit vector to 1 */
void rwg_bitvector_setbit(unsigned short int *bitVector, unsigned short int pos);

/*FUNC: takes two bit vectors and performs or*/
void rwg_bitvector_update(unsigned short int *bitVector1, unsigned short int *bitVector2);

/*FUNC: counts the 1:s in the bit vector*/
int rwg_bitvector_count(unsigned short int* bitVector);

/*FUNC: Looks up all messages that a sender of an incoming REQF do not posess*/
int rwg_wake(unsigned char incomingSender[6], packetBuffer *packetBuff);

/*FUNC: Handles incoming packets*/
int rwg_handle_incoming(unsigned char *packet, packetBuffer *packetBuff, int outPipe, int length);

/*FUNC: listen on socket*/
int rwg_listen(int socket, int inPipe, int outPipe, packetBuffer *packetBuff);

/*FUNC: handles incoming REQF:s*/
int rwg_handle_reqf(rwg_header *rwghdr, packetBuffer *packetBuff, int outPipe);

/*FUNC: handles incoming OKTF:s*/
int rwg_handle_oktf(rwg_header *rwghdr, packetBuffer *packetBuff);

/*FUNC: handles incoming BS:s, returns 1 if there is a match 0 if not*/
int rwg_handle_bs(rwg_header *rwghdr, packetBuffer *packetBuff);

/*FUNC: handle ACK*/ 
int rwg_handle_ack(rwg_header *rwghdr, packetBuffer *packetBuff);


