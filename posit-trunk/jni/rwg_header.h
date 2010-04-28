


/*
 * rwg_header.h
 *
 *
 * Random walk gossip header
 *
 * 0     -     15 16     -      31
 * -------------------------------
 *| packetLength | type | hops    |
 * -------------------------------
 *| groupSize    | sequenceNumber | 
 * -------------------------------
 *|           origin              | mac
 * ------------------------------- 
 *|              |   target       | mac
 * -------------------------------
 *|                               | mac
 * -------------------------------
 *|           sender              |
 * -------------------------------
 *|              |    visited     |
 * -------------------------------
 *|           ...                 | 256 bitv
 *|           ...                 | 
 * -------------------------------
 *|           recentVisited       | 256 bitv
 *|           ...                 |
 * -------------------------------
 *
 * packetLength: length of packet in bytes
 *
 * type: REQF - Request to forward , ACK - Acknowledgement, OKTF - Ok to forward, 
 *       BF - be silent
 *
 * hops: number of hops made by the message
 *
 * groupSize: to know how many nodes the message should be delivered to
 *
 * sequenceNumber + origin: unique identifier of a message
 *
 * target: used when a specific node node is intended to receive the message
 *
 * sender: node that sent the message
 *
 * visited: bitvector(256) which is used to indicate which nodes that have seen the message
 *
 * recentVisited: bitvector(256) which indicates recently infected nodes
 *
 */

#ifndef _
#define _

#include <stdlib.h>
#include <linux/if_ether.h>
#include <netinet/if_ether.h> 
#include <net/ethertypes.h>
//#include <netinet/ether.h> 
#include <netinet/in.h> 
#include <stdio.h>
#include <string.h>


/*the rwg header*/
typedef struct{
  
  unsigned short int packetLength;
  unsigned char type;
  unsigned char hops;
  unsigned short int TTL;
  unsigned short int groupSize;
  unsigned short int sequenceNumber;
  unsigned char origin[6];
  unsigned char target[6];
  unsigned char sender[6];
  unsigned short int visited[16];    
  unsigned short int recentVisited[16];
} rwg_header;

/*time_stamp*/
typedef struct{
  unsigned int seconds;
  unsigned int u_seconds;
}t_stamp;

/*used in the reqf buff*/
typedef struct{
  unsigned char *reqf;
  t_stamp arrived_at;
  t_stamp w_stamp;
  int wake; // 1 if reqf is in wake buffer
  int wait; // 1 if reqf is waiting on ACK
  int wake_pos;
  int wait_pos;
  int reqf_pos;
}reqf_info;

typedef struct{
  unsigned char *reqf;
  int reqf_pos;
}a_reqf;

/* One to rule them all? */
typedef struct{

  reqf_info *wake[128]; // keeps track of REFQs that should be sent (because of wake)
  unsigned int wake_counter;
  unsigned char *ack[20];  // keeps track of incoming ACKs
  unsigned int ack_counter;
  reqf_info *waiting[20]; 
  unsigned int w_front; // points at the next free slot in waiting/stamp
  unsigned int w_tail; // points at first element
  reqf_info reqf[128]; // keeps track of all messages (REQFs)
  unsigned int reqf_counter;
  reqf_info temp_reqf[128]; // used while cleaning the real buffer...
  unsigned int temp_reqf_counter;
  a_reqf active_reqf; // the reqf that is beeing processed at the moment

} packetBuffer;

/*FUNC: creates a pointer to rwg_header, size 256*/
rwg_header* rwg_create_rwghdr();

/*FUNC: creates a new ethernet header*/
unsigned char* rwg_create_ethhdr(char *src_mac, char *dst_mac, int protocol);

#endif
