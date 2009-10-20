
/*
 * rwg_send.c, Send routines for RWG-packets
 *
 */

#include "rwg_send.h"

/*FUNC: Takes a string, and the its length and passes the information to the protocol*/
int rwg_write(char* input, int length){

  if(input[length-1] != '\0'){return 0;}
  
  // put this in some buffer
  
  return 1;
}

/*FUNC: The function that is sending all the raw packets*/
int rwg_send_raw(int rawsock, unsigned char *pkt, int pkt_len)
{
  int sent= 0;
  /* A simple write on the socket ..thats all it takes ! */
  if((sent = write(rawsock, pkt, pkt_len)) != pkt_len){
    if(TRACE){printf("rwg_send_raw, Could only send %d bytes of packet of length %d\n", sent, pkt_len);}
    return 0;
  }
  return 1;
}

/*FUNC: matches against packets in the REQF packet buffer, returns a position in the buffer
  if there is a match, if there is no matchor buffer is empty it returns -1 */
int rwg_match_packetid(unsigned char packet_id[8],packetBuffer *packetBuff){

  /*check which REQF to forward by matching packet_id, i.e origin and packet id (saved in packetBuff)*/
  unsigned short int sequenceNumber;
  
  int rc;  
  if(packetBuff->reqf_counter > 0){ 
    rc = packetBuff->reqf_counter - 1;
  }else{
    return -1;
  }

  for(;rc >= 0;rc--){ 
    
    if(memcmp(((rwg_header *)packetBuff->reqf[rc].reqf)->origin, packet_id, sizeof(((rwg_header *)packetBuff->reqf[rc].reqf)->origin)) == 0 && memcmp(&((rwg_header *)packetBuff->reqf[rc].reqf)->sequenceNumber,packet_id+6,sizeof(((rwg_header *)packetBuff->reqf[rc].reqf)->sequenceNumber)) == 0){   
      if(TRACE){printf("rwg_match_packetid, rc: %i\n", rc);}
      return rc;
    }   
  }
  return -1;
}

/*FUNC: creates new packet with type REQF*/
unsigned char* rwg_create_reqf_n(packetBuffer *packetBuff){
 
  /*create a brand new REQF with the payload taken from the input file*/
  if(TRACE){printf("rwg_create_reqf_n: Creates new reqf\n");}
 
  rwg_header *rwghdr; 
  rwghdr = (rwg_header *)packetBuff->active_reqf.reqf;

  /*sets the fields in the rwg header, packetLength is set in rwg_listen*/
  rwghdr->type = 0x01; 
  rwghdr->hops = 0x01; 
  rwghdr->groupSize = groupSize;
  rwghdr->TTL = TTL;
  rwghdr->sequenceNumber = sequenceNumber++;
  memcpy(rwghdr->origin,sender,sizeof(unsigned char)*6);
  rwghdr->target[0] = 0xff;rwghdr->target[1] = 0xff;rwghdr->target[2] = 0xff;
  rwghdr->target[3] = 0xff;rwghdr->target[4] = 0xff; rwghdr->target[5] = 0xff;
  memcpy(rwghdr->sender,sender,sizeof(unsigned char)*6);

  
  //int i = 0;

  /*clean the memory*/
  memset(rwghdr->visited, '\0', sizeof(rwghdr->visited));
  memset(rwghdr->recentVisited, '\0', sizeof(rwghdr->recentVisited));

  rwg_bitvector_setbit(rwghdr->recentVisited,hashedAddr);
  rwg_bitvector_setbit(rwghdr->visited,hashedAddr);

  /*stores the pointer to the reqf_info in the reqf buffer*/
  packetBuff->waiting[packetBuff->w_front % (sizeof((*packetBuff).waiting)/4)] = &packetBuff->reqf[packetBuff->reqf_counter]; 

  /*saves the position, that the reqf pointer is placed within the waiting buffer, in the reqf buffer*/
  packetBuff->reqf[packetBuff->reqf_counter].wait_pos = packetBuff->w_front % (sizeof((*packetBuff).waiting)/4); 

  /*fetch a new time stamp*/
  t_stamp stamp;
  set_time_stamp(&stamp);

  /*sets the waiting timestamp in the reqf_info*/
  packetBuff->reqf[packetBuff->reqf_counter].w_stamp = stamp;

  /*set wait flag*/
  //packetBuff->reqf[packetBuff->reqf_counter].wait = 1;

  packetBuff->w_front++;

  /*make sure front wont reach the tail (when the buffers get full)*/
  if(packetBuff->w_front % (sizeof((*packetBuff).waiting)/4) == packetBuff->w_tail % (sizeof((*packetBuff).waiting)/4) &&
     packetBuff->w_front != packetBuff->w_tail){
    packetBuff->waiting[packetBuff->w_tail % (sizeof((*packetBuff).waiting)/4)] = NULL;
    packetBuff->w_tail++;
  }

  /*stores the pointer to the reqf in the reqf buffer, and saves the time stamp*/
  packetBuff->reqf[packetBuff->reqf_counter].reqf = (unsigned char *)rwghdr; 
  packetBuff->reqf[packetBuff->reqf_counter].arrived_at = stamp;
  packetBuff->reqf[packetBuff->reqf_counter].reqf_pos = packetBuff->reqf_counter;
  packetBuff->reqf_counter++; 

  if(TRACE){printf("rwg_send_reqf_n, reqf_counter: %i\n",packetBuff->reqf_counter);}

  /*makes a copy to send*/
  unsigned char *rwgPacket;
  rwgPacket = (unsigned char *)malloc(rwghdr->packetLength);
  memcpy(rwgPacket, rwghdr, rwghdr->packetLength);
 
  return rwgPacket;
}

/*FUNC: forwards old REQF after getting an OKTF or sending a REQF from the wake buffer*/
unsigned char* rwg_create_reqf_f(packetBuffer *packetBuff){
 
  if(TRACE){printf("rwg_create_reqf_f, Forwards old reqf\n");}
  unsigned char *rwgPacket; 

  /*check if active_reqf is set*/ // temporary solution to some odd bugg ... !! 
  if(packetBuff->active_reqf.reqf != NULL && packetBuff->reqf_counter > packetBuff->active_reqf.reqf_pos){
    
    if(TRACE){
    printf("reqf_counter %i\n",packetBuff->reqf_counter);
    printf("reqf_pos %i\n",packetBuff->active_reqf.reqf_pos);
    printf("Is forwarded: \n");}
    if(TRACE){printPacket(packetBuff->active_reqf.reqf,((rwg_header *)packetBuff->active_reqf.reqf)->packetLength);}

    /*increase hop counter, change sender address and update visited list*/
    rwg_header *rwghdr = (rwg_header *)packetBuff->active_reqf.reqf;
    memcpy(rwghdr->sender,sender,sizeof(unsigned char)*6); 
    rwg_bitvector_update(rwghdr->visited,rwghdr->recentVisited); 

    //printf("WAKE_C: %i\n",packetBuff->wake_counter);
    /*check if the REQF was from the wake buffer, */
    if(packetBuff->wake_counter > 0 &&
       packetBuff->wake[packetBuff->wake_counter -1] != NULL &&
       packetBuff->active_reqf.reqf == packetBuff->wake[packetBuff->wake_counter -1]->reqf){
      if(TRACE){printf("rwg_create_reqf_f, Forwards from wake buffer\n");}
      if(TRACE){printf("rwg_create_reqf_f, wake_counter: %i\n", packetBuff->wake_counter);}
      packetBuff->wake[packetBuff->wake_counter - 1]->wake = 0;
      packetBuff->wake[packetBuff->wake_counter -1] = NULL;
      packetBuff->wake_counter--;
    }else{
      rwghdr->hops++; // since there will be a lot of resending...
    }

    /*Fetch a new time stamp*/
    t_stamp stamp;
    set_time_stamp(&stamp);
   
    /*make sure the TTL have not expired before sending this packet*/
    if((rwghdr->TTL - (stamp.seconds - packetBuff->reqf[packetBuff->active_reqf.reqf_pos].arrived_at.seconds)) < 0){
      //packetBuff->active_reqf.reqf = NULL;
      return NULL;
    }
   
    /*stores the pointer to the reqf_info in the waiting buffer*/
    packetBuff->waiting[packetBuff->w_front % (sizeof((*packetBuff).waiting)/4)] = &packetBuff->reqf[packetBuff->active_reqf.reqf_pos];   
    packetBuff->reqf[packetBuff->active_reqf.reqf_pos].wait_pos = packetBuff->w_front % (sizeof((*packetBuff).waiting)/4);
    //packetBuff->waiting[packetBuff->w_front % (sizeof((*packetBuff).waiting)/4)].w_stamp = stamp;
    packetBuff->reqf[packetBuff->active_reqf.reqf_pos].w_stamp = stamp;
    packetBuff->w_front++;

   
    /*make sure front wont reach the tail (when the buffers get full)*/
    if((packetBuff->w_front % (sizeof((*packetBuff).waiting)/4) == packetBuff->w_tail % (sizeof((*packetBuff).waiting)/4)) &&
       packetBuff->w_front != packetBuff->w_tail){
      packetBuff->waiting[packetBuff->w_tail % (sizeof((*packetBuff).waiting)/4)] = NULL;
      packetBuff->w_tail++;
    }

    /*make a copy to send*/
    rwgPacket = (unsigned char *)malloc(((rwg_header *)packetBuff->active_reqf.reqf)->packetLength);
    memcpy(rwgPacket,packetBuff->active_reqf.reqf,((rwg_header *)packetBuff->active_reqf.reqf)->packetLength);
    return rwgPacket;
 
  }else{
    if(TRACE){printf("rwg_create_reqf_f, active_reqf is NULL\n");}
    /*This may happen when TTL have expired and the REQF have been removed since the refreshing of the REQF buffer 
     does not update the wake buffer, so the wake counter(-1) may be pointing at a NULL element*/
    packetBuff->wake[packetBuff->wake_counter - 1]->wake = 0;
    packetBuff->wake_counter--; 
    return NULL;
  }
  return rwgPacket;
}

/*FUNC: forwards a random REFQ, when network is silent*/ 
unsigned char* rwg_create_reqf_r(packetBuffer *packetBuff)
{
  unsigned char *rwgPacket; 

  if(TRACE){printf("rwg_create_reqf_r: reqf_counter: %i\n", packetBuff->reqf_counter);}
  
  /*chose the refq with least marks in the visited list*/
  int reqf_c = packetBuff->reqf_counter - 1;
  if(reqf_c > -1){
    
    int matches ;
    int matchesPrevious = rwg_bitvector_count(((rwg_header *)packetBuff->reqf[reqf_c].reqf)->visited);
    int reqfPrevious = reqf_c;
   
    if(TRACE){printf("MATCHES(Visited): ");}
    for(;-1 < reqf_c;reqf_c--){
      matches = rwg_bitvector_count(((rwg_header *)packetBuff->reqf[reqf_c].reqf)->visited);
      if(TRACE){printf(" %i", matches);}
      if(matchesPrevious > matches){
	matchesPrevious = matches;
	reqfPrevious = reqf_c;
	if(TRACE){printf("rwg_create_reqf_r, matchesPrevious: %d reqfPrevious: %d\n", matchesPrevious, reqfPrevious);}
      }
    }

    if(TRACE){printf("\n");}
   
    /*copy, return, and set active_reqf if visited < groupSize, and change the sender of the packet*/
    if(((rwg_header *)packetBuff->reqf[reqfPrevious].reqf)->groupSize > matchesPrevious){
  
    /*Fetch time, used for timestamps*/
      t_stamp stamp;
      set_time_stamp(&stamp);
   
      rwg_header *rwghdr;
      rwghdr = (rwg_header *)packetBuff->reqf[reqfPrevious].reqf;
   
      /*make sure the TTL have not expired before sending this packet*/
      if((rwghdr->TTL - (stamp.seconds - packetBuff->reqf[reqfPrevious].arrived_at.seconds)) < 0){
	//packetBuff->active_reqf.reqf = NULL;
	return NULL;
      }
   
      memcpy(rwghdr->sender,sender,sizeof(char)*6);
      rwghdr->TTL = rwghdr->TTL - (stamp.seconds - packetBuff->reqf[reqfPrevious].arrived_at.seconds); //decrease TTL
      packetBuff->reqf[reqfPrevious].arrived_at = stamp; // update the arrived_at value
      rwgPacket = (unsigned char *)malloc(rwghdr->packetLength);
      memcpy(rwgPacket,rwghdr,rwghdr->packetLength);

      /*stores the pointer to the reqf in the waiting buffer (reqfs that are waiting for acks)*/
      packetBuff->waiting[packetBuff->w_front % (sizeof((*packetBuff).waiting)/4)] = &packetBuff->reqf[reqfPrevious];   
      //packetBuff->waiting[packetBuff->w_front % (sizeof((*packetBuff).waiting)/4)]->w_stamp = stamp;
      packetBuff->reqf[reqfPrevious].w_stamp = stamp;
      packetBuff->reqf[reqfPrevious].wait_pos = packetBuff->w_front % (sizeof((*packetBuff).waiting)/4);
      packetBuff->w_front++;

      /*make sure front wont reach the tail (when the buffers get full)*/
      if(packetBuff->w_front % (sizeof((*packetBuff).waiting)/4) == packetBuff->w_tail % (sizeof((*packetBuff).waiting)/4) &&
	 packetBuff->w_front != packetBuff->w_tail){
	packetBuff->waiting[packetBuff->w_tail % (sizeof((*packetBuff).waiting)/4)] = NULL;
	packetBuff->w_tail++;
      }

      if(TRACE){printf("rwg_create_r, TTL %i\n",rwghdr->TTL);}
      return rwgPacket;
    }
  }

  return NULL;
}

/*FUNC: creates packets with type ACK*/
unsigned char* rwg_create_ack(packetBuffer *packetBuff){

  if(TRACE){ printf("rwg_create_ack: Creates new ACK\n");}

  /*creates new ACK, contains the updated recentVisited vector*/
  rwg_header *rwghdr;
  rwghdr = rwg_create_rwghdr(); 
  rwghdr->packetLength = sizeof(rwg_header);
  rwghdr->type = 0x02;
  rwghdr->hops = 0x00; 
  rwghdr->TTL = 0x00;
  rwghdr->groupSize = groupSize;
  rwghdr->sequenceNumber = ((rwg_header *)packetBuff->active_reqf.reqf)->sequenceNumber;
  memcpy(rwghdr->origin,((rwg_header *)packetBuff->active_reqf.reqf)->origin, sizeof(char)*6);
  memcpy(rwghdr->target, ((rwg_header *)packetBuff->active_reqf.reqf)->sender,sizeof(char)*6);
  memcpy(rwghdr->sender,sender,6);
  memcpy(rwghdr->recentVisited,((rwg_header *)packetBuff->active_reqf.reqf)->recentVisited,sizeof(rwghdr->recentVisited));
  memcpy(rwghdr->visited,((rwg_header *)packetBuff->active_reqf.reqf)->visited,sizeof(rwghdr->visited));

  /*copy the sender of the reqf to target, will be used when creating the ethheader to reduce network load*/
  memcpy(target,rwghdr->target,sizeof(unsigned char)*6);

  /*set active_reqf to NULL*/
  //packetBuff->active_reqf.reqf = NULL;

  return (unsigned char *)rwghdr;
}

/*FUNC: creates packets with type OKTF*/
unsigned char* rwg_create_oktf(packetBuffer *packetBuff){
 
  if(TRACE){ printf("rwg_create_oktf: Creates new OKTF\n");}
  rwg_header *rwghdr;

  /*checks that the reqf actually exisits..*/
  if(packetBuff->w_tail == packetBuff->w_front){
    if(TRACE){printf("rwg_create_oktf, waiting is empty\n");}
    packetBuff->waiting[packetBuff->w_tail % (sizeof((*packetBuff).waiting)/4)] = NULL;
    return NULL;
    /*this case might occur when the REQF buffer have been refreshed*/
  }else if(packetBuff->waiting[packetBuff->w_tail % (sizeof((*packetBuff).waiting)/4)] == NULL){
    if(TRACE){printf("rwg_create_oktf, packetBuff->waiting[packetBuff->w_tail mod (sizeof((*packetBuff).waiting)/4)] is NULL \n");}
    packetBuff->w_tail++;
    return NULL;
  }else{
    packetBuff->active_reqf.reqf = packetBuff->waiting[packetBuff->w_tail % (sizeof((*packetBuff).waiting)/4)]->reqf;
    /*find the ACKs belonging to the last reqf in the wainting buffer*/
    unsigned char ack_id[8];
    unsigned char reqf_id[8];   
    int ack_c = 0;
    unsigned char *temp_acks[sizeof((*packetBuff).ack)/4]; //a temporary buffer for storing pointers to acks
    int temp_ack_c = 0;
    
    memcpy(reqf_id,((rwg_header *)packetBuff->waiting[packetBuff->w_tail % (sizeof((*packetBuff).waiting)/4)]->reqf)->origin,sizeof(unsigned char)*6);
    memcpy(reqf_id+sizeof(unsigned char)*6,&((rwg_header *)packetBuff->waiting[packetBuff->w_tail % (sizeof((*packetBuff).waiting)/4)]->reqf)->sequenceNumber,sizeof(unsigned short int));
    
    /*loops through the whole ack buffer to find acks matching the reqf*/
    for(; ack_c < sizeof((*packetBuff).ack)/4 ; ack_c++){
      if(packetBuff->ack[ack_c] != NULL){
	memcpy(ack_id,((rwg_header *)packetBuff->ack[ack_c])->origin,sizeof(unsigned char)*6);
	memcpy(ack_id+sizeof(unsigned char)*6,&((rwg_header *)packetBuff->ack[ack_c])->sequenceNumber,sizeof(unsigned short int));
	/*if match add the ack to the temp buffer..*/
	if(memcmp(ack_id,reqf_id,sizeof(ack_id)) == 0){
	  temp_acks[temp_ack_c] = packetBuff->ack[ack_c];
	  packetBuff->ack[ack_c] = NULL; // the ack will be freed later
	  temp_ack_c++;
	}
      }      
    }

    if(TRACE){printf("rwg_create_oktf, temp_ack_c: %i\n",temp_ack_c);}
    /*if there are no matching ACKS, set old pointers to null. increase the counter and return*/
    if(temp_ack_c == 0){
      if(TRACE){printf("rwg_create_oktf, there are no matching ACKS in the buffer... \n");}
      packetBuff->waiting[packetBuff->w_tail % (sizeof((*packetBuff).waiting)/4)] = NULL;
      packetBuff->w_tail++;
      //packetBuff->active_reqf.reqf = NULL;
      return NULL;
    }

    /*chose random ack*/
    srand((int)time(NULL));
    int ackno = rand();
    if(TRACE){printf("rwg_create_oktf, ackno before mod: %i\n",ackno);}
    ackno = ackno % temp_ack_c;
    if(TRACE){printf("rwg_create_oktf, ackno: %i\n",ackno);}
    
    /*sets the values in the OKTF*/
    rwghdr = rwg_create_rwghdr();
    rwghdr->packetLength = sizeof(rwg_header);
    rwghdr->type = 0x03;
    rwghdr->hops = ((rwg_header *)packetBuff->active_reqf.reqf)->hops;  
    rwghdr->TTL = 0x00;
    rwghdr->groupSize = ((rwg_header *)packetBuff->active_reqf.reqf)->groupSize;
    rwghdr->sequenceNumber = ((rwg_header *)packetBuff->active_reqf.reqf)->sequenceNumber;
    memcpy(rwghdr->origin,((rwg_header *)packetBuff->active_reqf.reqf)->origin,sizeof(unsigned char)*6);
    memcpy(rwghdr->target, ((rwg_header *)temp_acks[ackno])->sender, sizeof(unsigned char)*6);
    memcpy(rwghdr->sender,sender,sizeof(unsigned char)*6);
    
    /*set the values to zero (so there is nothin bad in mem)*/
    //int i = 0;
    /*for(;i < (sizeof(rwghdr->visited)/2);i++){
      rwghdr->visited[i] =  0x00; 
      rwghdr->recentVisited[i] = 0x00;
      }*/

    memset(rwghdr->visited, '\0', sizeof(rwghdr->visited));
    memset(rwghdr->recentVisited, '\0', sizeof(rwghdr->recentVisited));

    /*update recentVisited and visited with the incoming ACKS, for the oktf and the reqf saved in the buffer, also free all the acks.. since there is no more use for them*/
    temp_ack_c = temp_ack_c - 1;
    for(; -1 < temp_ack_c; temp_ack_c--){
      rwg_bitvector_update(rwghdr->recentVisited,((rwg_header *)temp_acks[temp_ack_c])->recentVisited);
      rwg_bitvector_update(((rwg_header *)packetBuff->active_reqf.reqf)->recentVisited,((rwg_header *)temp_acks[temp_ack_c])->recentVisited);
      rwg_bitvector_update(rwghdr->visited,((rwg_header *)temp_acks[temp_ack_c])->visited);
      rwg_bitvector_update(((rwg_header *)packetBuff->active_reqf.reqf)->visited,((rwg_header *)temp_acks[temp_ack_c])->visited);
      free(temp_acks[temp_ack_c]);
      temp_acks[temp_ack_c]=NULL;
    }

    rwg_bitvector_update(rwghdr->visited,rwghdr->recentVisited);
    rwg_bitvector_update(((rwg_header *)packetBuff->active_reqf.reqf)->visited,((rwg_header *)packetBuff->active_reqf.reqf)->recentVisited);
    
    /*check the hop limit, update visited if it exceeds the limit and zero out recent visited and hops*/
    if(rwghdr->hops > hops){
      rwghdr->hops = 0;
      ((rwg_header *)packetBuff->active_reqf.reqf)->hops = 0;
      
      /*clean recent visited*/
      memset(rwghdr->recentVisited, '\0', sizeof(rwghdr->recentVisited));
      memset(((rwg_header *) packetBuff->active_reqf.reqf)->recentVisited, '\0',sizeof(rwghdr->recentVisited));
    }
  }

  /*set old pointers to NULL and increase the w_tail*/
  packetBuff->waiting[packetBuff->w_tail % (sizeof((*packetBuff).waiting)/4)] = NULL;
  packetBuff->w_tail++;
  //packetBuff->active_reqf.reqf = NULL;
  return (unsigned char*)rwghdr;
}

/*FUNC: creates packets with type BS*/
unsigned char* rwg_create_bs(packetBuffer* packetBuff){

  if(TRACE){printf("rwg_create_bs: Creates new BS\n");}

  unsigned char *rwgPacket;

  /*To avoid segmentation fault, even though this case should not happen*/
  if((rwg_header *)packetBuff->active_reqf.reqf != NULL){
    /*find the correct packet to send BS on, match packet id (origin sequence number)*/
    unsigned short int sequenceNumber;
    unsigned char packet_id[8];
    /*send BS, change sender and type from existing REQF, and send without payload*/
    rwgPacket = (unsigned char *)malloc(sizeof(rwg_header));
    memcpy(rwgPacket,packetBuff->active_reqf.reqf,sizeof(rwg_header));   
    /*change sender address, type, packetSize*/  
    rwg_header *rwghdr = (rwg_header *)rwgPacket;
    rwghdr->packetLength = (unsigned short int)sizeof(rwg_header);
    memcpy(rwghdr->sender,sender,sizeof(unsigned char)*6);
    rwghdr->type = 0x4;
  }else{
    if(TRACE){printf("rwg_create_bs, active_reqf is NULL\n");}
    return NULL;
  }

  /*set active reqf to NULL and return*/
  //packetBuff->active_reqf.reqf = NULL;
  return rwgPacket; 
}

/*FUNC: handles the send routine*/
int rwg_send_routine(int socket, packetBuffer *packetBuff)
{ 
  unsigned char *packet;
  int ehdr_len;
  int rwghdr_len;
  unsigned char *ehdr;
  rwg_header *rwghdr; 
  unsigned char* rwgPacket;

  if(SEND_ACK){
    rwgPacket = rwg_create_ack(packetBuff);
    SEND_ACK = 0;
    if(rwgPacket == NULL){return 0;}
    //set specific target, to reduce network load
    ehdr = rwg_create_ethhdr(sender, target, RWG_ETHER_TYPE);
  }else if(SEND_REQF_N){
    rwgPacket = rwg_create_reqf_n(packetBuff);
    SEND_REQF_N = 0;
    if(rwgPacket == NULL){return 0;}
    ehdr = rwg_create_ethhdr(sender, broadcast, RWG_ETHER_TYPE);

  }else if(SEND_REQF_F){
    rwgPacket = rwg_create_reqf_f(packetBuff);
    SEND_REQF_F = 0;
    if(rwgPacket == NULL){return 0;} 
    if(TRACE){printf("rwg_send_routine, LISTEN_ACK set\n");}
    ehdr = rwg_create_ethhdr(sender, broadcast, RWG_ETHER_TYPE);

  }else if(SEND_REQF_R){
    rwgPacket = rwg_create_reqf_r(packetBuff);
    SEND_REQF_R = 0;
    if(rwgPacket == NULL){return 0;} 
    if(TRACE){printf("rwg_send_routine, LISTEN_ACK set\n");}
    ehdr = rwg_create_ethhdr(sender, broadcast, RWG_ETHER_TYPE);

  }else if(SEND_OKTF){
    rwgPacket = rwg_create_oktf(packetBuff);
    SEND_OKTF = 0;
    if(rwgPacket == NULL){return 0;}
    ehdr = rwg_create_ethhdr(sender, broadcast, RWG_ETHER_TYPE);

  }else if(SEND_BS){
    rwgPacket = rwg_create_bs(packetBuff);
    SEND_BS = 0;
    if(rwgPacket == NULL){return 0;}
    ehdr = rwg_create_ethhdr(sender, broadcast, RWG_ETHER_TYPE);

  }else{
    if(TRACE){printf("SHOULD NOT GET HERE (besides during exit)\n");}
    return 0;
  }
  
  /*creates a ethernet header*/ 
  // ehdr = rwg_create_ethhdr(sender, BROADCAST_ADDR, RWG_ETHER_TYPE);
  rwghdr_len = sizeof(rwg_header);
  ehdr_len = sizeof(struct ethhdr);


  /*adds ethern header*/
  rwghdr = (rwg_header *)rwgPacket; 
  packet = (unsigned char *)malloc(rwghdr->packetLength+ehdr_len);
  memcpy(packet,ehdr,ehdr_len);
  memcpy(packet+ehdr_len,rwgPacket,rwghdr->packetLength);

  if(TRACE){printPacket(packet,rwghdr->packetLength+ehdr_len);}

  /*Send the packet*/
  if(!rwg_send_raw(socket, packet, ehdr_len + rwghdr->packetLength)){
    perror("Error sending packet");
    free(rwgPacket); 
    free(packet);
    free(ehdr);
    return 0;
  }
    
  if(TRACE){printf("rwg_send_routine: Packet sent!\n");}
  
  /* Free the allocated memory */ 
  free(rwgPacket); 
  free(packet);
  free(ehdr);
  
  return 1;
}



