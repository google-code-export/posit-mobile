
/*
 * rwg.h
 *
 */



// headers providing "listen functionality"
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netinet/if_ether.h> 
#include <net/ethernet.h>
#include <netinet/ether.h> 
#include <string.h>

// processes
#include <sys/types.h>

// time
#include <sys/time.h>

/**/
#define BCAST_ETHER_ADDR "ff:ff:ff:ff:ff:ff"

/*FLAGS*/
int SEND_ACK;
int SEND_REQF_N; // create new from input
int SEND_REQF_F; // forward from buffer
int SEND_REQF_R; // send when network is silent
int SEND_OKTF;
int SEND_BS;
int LISTEN_ACK;
int RETRANSMIT;
int SEND_RETRANSMIT;
int TRACE; // print trace

/*RWG sender*/
unsigned char sender[6];

/*RWG groupSize*/
unsigned short int groupSize;

/*RWG hops*/
unsigned short int hops;

/*sequence number for REFQ*/
unsigned short int sequenceNumber;

/*Time to live*/
unsigned short int TTL;
