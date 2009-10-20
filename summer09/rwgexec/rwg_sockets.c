

/*
 * rwg_sockets.c Functions for creating sockets
 *
 */

#include "rwg_sockets.h"

/*FUNC: sets the nonblocking flag on the socket, so it will not block if buffer is empty*/
int rwg_nonblock_socket(int socket, int value)
{
  int oldflags = fcntl(socket,F_GETFL,0);
  if(oldflags < 0)
    return oldflags;
  if(value != 0)
    oldflags |= O_NONBLOCK;
  else
    oldflags &= ~O_NONBLOCK;
  return fcntl(socket,F_SETFL,oldflags);
}

/*FUNC: creates a new raw socket*/
int rwg_create_socket(int protocol_to_sniff)
{
  int rawsock; 
    if((rawsock = socket(PF_PACKET, SOCK_RAW, htons(protocol_to_sniff)))== -1)
    {
        perror("Error creating raw socket: ");
        exit(-1);
    }
    return rawsock;
}

/*FUNC: Binds a socket to the interface..,*/
int rwg_bind_socket(char *device, int rawsock, int protocol)
{   
    struct sockaddr_ll sll;
    struct ifreq ifr;
    bzero(&sll, sizeof(sll));
    bzero(&ifr, sizeof(ifr));
    /* First Get the Interface Index  */
    strncpy((char *)ifr.ifr_name, device, IFNAMSIZ);
    if((ioctl(rawsock, SIOCGIFINDEX, &ifr)) == -1){
        printf("Error getting Interface index !\n");
        exit(-1);
    }
    /* Bind our raw socket to this interface */
    sll.sll_family = AF_PACKET;
    sll.sll_ifindex = ifr.ifr_ifindex;
    sll.sll_protocol = htons(protocol);

    if((bind(rawsock, (struct sockaddr *)&sll, sizeof(sll)))== -1){
        perror("Error binding raw socket to interface\n");
        exit(-1);
    }
    return 1;  
}


/*FUNC: Gets the mac addr from the network device*/
int rwg_get_macaddr (unsigned char* addr, int s, unsigned char* dev){
  struct ifreq ifr;
  strncpy(ifr.ifr_name,dev,sizeof(ifr.ifr_name)-1);
  if(-1 == ioctl(s,SIOCGIFHWADDR,&ifr)){
    perror("ioctl(SIOCGIFHWADDR)");
    return 0;
  }
  memcpy(addr,ifr.ifr_hwaddr.sa_data,sizeof(unsigned char)*6);
  return 1;
}




