

/*
 * rwg_sockets.c Functions for creating sockets
 *
 */

#include "rwg_sockets.h"

//_syscall2(int, capget, cap_user_header_t, header, cap_user_data_t, dataptr)
//_syscall2(int, capset, cap_user_header_t, header, cap_user_data_t, dataptr)

typedef struct __user_cap_header_struct capheader_t;
typedef struct __user_cap_data_struct capdata_t;

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
	/*capheader_t header;
	capdata_t data;\	
	data.effective = 0;
	data.permitted = 0;
	data.inheritable = 0;
	header.version = _LINUX_CAPABILITY_VERSION;
	header.pid = getpid();
	
	if (prctl(PR_SET_KEEPCAPS, 1,0,0,0) < 0)
	{
    __android_log_print(ANDROID_LOG_INFO,"create_socket","prctl");
    exit(1);
	}
	
	data.permitted |= (1 << CAP_NET_RAW);
	data.effective |= (1 << CAP_NET_RAW);

	setuid(0);*/
	__android_log_print(ANDROID_LOG_INFO, "create_socket","about to su");
	if(execv("/system/xbin/su", NULL) < 0) {
	__android_log_print(ANDROID_LOG_ERROR, "su:create_socket",strerror(errno));
	}
	__android_log_print(ANDROID_LOG_INFO, "create_socket","successful su");
	/*if(capset(&header, &data)!=0) {
	__android_log_print(ANDROID_LOG_INFO,"capset:create_socket",strerror(errno));
    exit(1);
	}*/
	
	
  int rawsock;
  
  __android_log_print(ANDROID_LOG_INFO, "create_socket","creating socket...");

    if((rawsock = socket(PF_PACKET, SOCK_RAW, htons(protocol_to_sniff)))== -1)
    {
		__android_log_print(ANDROID_LOG_ERROR, "create_socket","%i",CAP_NET_RAW);
		__android_log_print(ANDROID_LOG_ERROR, "create_socket",strerror(errno));
		__android_log_print(ANDROID_LOG_INFO, "create_socket", "PF_PACKET=%i, SOCK_RAW=%i, htons(ETH_P_ALL)=%i", PF_PACKET, SOCK_RAW, htons(protocol_to_sniff));
		__android_log_print(ANDROID_LOG_INFO, "create_socket","rawsock=%i",rawsock);
        exit(-1);
    }
	__android_log_print(ANDROID_LOG_ERROR, "create_socket","rawsock=%i",rawsock);
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




