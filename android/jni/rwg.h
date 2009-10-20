
/*
 * rwg.h
 *
 */

#ifndef _rwg
#define _rwg

/*Necessary headers*/
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netinet/if_ether.h> 
#include <net/ethertypes.h>
#include <netinet/ether.h> 
#include <string.h>
#include <sys/types.h>
#include <sys/time.h>
#include <android/log.h>

#include "rwg_send.h"
#include "rwg_header.h"
#include "rwg_receive.h"
#include "rwg_sockets.h"

/*Some constants*/
#define RWG_ETHER_TYPE 0x1111 // shows that an eth frame carries a rwg packet as payload
#define BROADCAST_ADDR "ff:ff:ff:ff:ff:ff"
#define MTU 1024 // maximum transmission unit (bytes)

/*FLAGS*/
int SEND_ACK;
int SEND_REQF_N; // create new from input
int SEND_REQF_F; // forward from buffer
int SEND_REQF_R; // send random when network is silent
int SEND_OKTF;
int SEND_BS;
int LISTEN_ACK;
int RETRANSMIT;
int SEND_RETRANSMIT;
int TRACE; // print trace
int WO_MODE; // decides what will be written on the output pipe
int LOOP; //loop flag for the threads

/*RWG sender*/
unsigned char sender[6];

/*RWG hashed sender value*/
int hashedAddr;

/*RWG target*/
unsigned char target[6];

/*RWG broadcast*/
unsigned char broadcast[6];

/*RWG groupSize*/
unsigned short int groupSize;

/*RWG hops*/
unsigned short int hops;

/*sequence number for REFQ*/
unsigned short int sequenceNumber;

/*Time to live*/
unsigned short int TTL;

/*FUNC: Called to exit protocol*/
void exit();

/*FUNC: Remove all packets in the reqf buffer*/
void rwg_clean_buffer(packetBuffer *packetBuff);

#endif
