/*
 * rwg_header.c
 *
 */

#include "rwg_header.h"

/*FUNC: creates a pointer to rwg_header*/
rwg_header* rwg_create_rwghdr(){

  rwg_header *header;
  header = (rwg_header *)malloc(sizeof(rwg_header));
  
  return header;
}

/*FUNC: creates a new ethernet header*/
unsigned char* rwg_create_ethhdr(char *src_mac, char *dst_mac, int protocol)
{

    struct ethhdr *ethernet_header;
    ethernet_header = (struct ethhdr *)malloc(sizeof(struct ethhdr));
    /* copy the Src mac addr */
    memcpy(ethernet_header->h_source, src_mac, 6);
    /* copy the Dst mac addr */
    memcpy(ethernet_header->h_dest, dst_mac, 6);
    /* copy the protocol */
    ethernet_header->h_proto = htons(protocol);
    /* done ...send the header back */

    return ((unsigned char *)ethernet_header);
}
