
/*
 * rwg_sockets.h
 *
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netinet/if_ether.h> 
#include <net/ethertypes.h>
#include <netinet/ether.h> 
#include <netpacket/packet.h> 
#include <features.h>
#include <linux/if_packet.h>
#include <linux/if_ether.h>
#include <sys/ioctl.h>
#include <net/if.h>
#include <fcntl.h>

#include <sys/types.h>
#include <unistd.h>
#include <sys/prctl.h>
#include <linux/unistd.h>
#include <linux/capability.h>

#include <android/log.h>




/*FUNC: sets socket in non blocking mode, wont block if buffer is empty*/
int rwg_nonblock_socket(int socket, int value);

/*FUNC: creates a new raw socket*/
int rwg_create_socket(int protocol_to_sniff);

/*FUNC: Binds a socket to the interface..,*/
int rwg_bind_socket(char *device, int rawsock, int protocol);

/*FUNC: Gets the mac addr from the network device*/
int rwg_get_macaddr (unsigned char* addr, int s, unsigned char* dev);

