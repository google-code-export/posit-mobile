
/*
 * receive.c, Handles incoming packets
 *
 */

#include "rwg_receive.h"



/*FUNC: reads the last packet in the buffer and returns it*/
//char* rwg_read(){
void rwg_read(){

  //must make the packetBuffer public ... 

  //check if reqf at packetBuff->reqf_counter -1 != NULL,
  
  //take the last packets payload and returns it
  
}

/*FUNC: just for printing out packets in hex to terminal, is used in trace mode*/
void printPacket(unsigned char* buffer,int length)
{
  int i = 0;
  printf("PACKET: ");
  for(i=0; i<length; i++){
    printf("%.2x ", buffer[i]);
  }
  printf("\n"); 
}

/*FUNC: handles control messages*/
int rwg_handle_control(char* ctrlMess, int length, packetBuffer *packetBuff){

  char type;
  char value[length];

  if(length <4)
    return 0;

  type = ctrlMess[3];

  switch(type){
    //quit the protocol
  case 'q' : 
    rwg_exit(); 
    return 1;
    //remove all packets from the reqf buffer
  case 'c' : 
    rwg_clean_buffer(packetBuff); 
    return 1;
    //change trace mode
  case 't' : 
    if(TRACE)
      TRACE = 0;
    else
      TRACE = 1;
    return 1;
  case 'w' : 
    if(WO_MODE)
      WO_MODE = 0;
    else
      WO_MODE = 1;
    return 1;
    //change WO_MODE
  case 'l' : 
    memcpy(value,ctrlMess+4,length-2);
    value[length-2]='\0';    
    if(0 > atoi(value) || atoi(value) > 3600){
       if(TRACE){printf("rwg_handle_control, TTL value to low/high (0-3600)\n");}
       return 1;
    }
    TTL = atoi(value);
    if(TRACE){printf("rwg_handle_control, New TTL is %i\n", TTL);}
    return 1;
    //change hops
  case 'h' : 
    memcpy(value,ctrlMess+4,length-2);
    value[length-2]='\0';    
    if(0 > atoi(value) || atoi(value) > 99){
       if(TRACE){printf("rwg_handle_control, hops value to low/high (0-99)\n");}
       return 1;
    }
    hops = atoi(value);
    if(TRACE){printf("rwg_handle_control, New hops is %i\n", hops);}
    return 1; 
    //change groupSize
  case 'g' : 
      memcpy(value,ctrlMess+4,length-2);
    value[length-2]='\0';    
    if(0 > atoi(value) || atoi(value) > 99){
       if(TRACE){printf("rwg_handle_control, groupSize to low/high (0-99)\n");}
       return 1;
    }
    groupSize = atoi(value);
    if(TRACE){printf("rwg_handle_control, New groupSize is %i\n", groupSize);}
    return 1; 

  default : return 0;

  }

}

/*FUNC: takes a pointer to a time stamp and sets the current time*/
void set_time_stamp(t_stamp *t)
{  
  struct timeval tv;
  gettimeofday(&tv,NULL);
  t->seconds = tv.tv_sec; 
  t->u_seconds = tv.tv_usec;
}

/*FUNC: takes two stamps and an interval(us) and checks if stamp1+interval < stamp2*/
int check_time_stamp(t_stamp stamp1,t_stamp stamp2, unsigned int interval)
{
  if((stamp1.u_seconds + interval) > 999999){
    stamp1.seconds++;
    stamp1.u_seconds = (interval - (999999 - stamp1.u_seconds));
  }else{
    stamp1.u_seconds = stamp1.u_seconds + interval;
  }
  if(stamp1.seconds < stamp2.seconds){ 
    return 1;
  }
  if(stamp1.seconds == stamp2.seconds && stamp1.u_seconds <= stamp2.u_seconds){
    return 1;
  }
  return 0;
}

/*FUNC: takes payload from received REFQ and writes it to the output buffer*/
int rwg_write_output(unsigned char* rwgPacket, int outPipe, int length)
{ 

  int write_len;
  unsigned char origin[20];
  rwg_header *rwghdr = (rwg_header *)rwgPacket;
  unsigned char out[length - sizeof(rwg_header) + sizeof(origin)];
  sprintf(origin,"<%.2x:%.2x:%.2x:%.2x:%.2x:%.2x>",rwghdr->sender[0],rwghdr->sender[1],rwghdr->sender[2],rwghdr->sender[3],rwghdr->sender[4],rwghdr->sender[5]); 
 
  switch(WO_MODE){

  case 0 : 

    memcpy(out,origin,sizeof(origin)-1); 
    memcpy(out+sizeof(origin)-1,rwgPacket+sizeof(rwg_header),length-sizeof(rwg_header));  
    out[sizeof(out)-1] = '\r'; 
    write_len = sizeof(out);
    break;
  
  case 1 : 
    
    memcpy(out,rwgPacket+sizeof(rwg_header),length-sizeof(rwg_header));
    write_len = length-sizeof(rwg_header) + 1;
    out[length-sizeof(rwg_header)] = '\r'; 
    break;
   
  default :
  
    break;

  }

  if(TRACE){
    int i = 0;
    char temp[length];
    memcpy(temp,out,write_len);
    printf("rwg_write_output, sizeof(out): %i\n", sizeof(out));
    printf("rwg_write_output(%i): ", write_len);
    for(;i<write_len;i++){
      printf("%c",temp[i]);
    }
    printf("\n");
  }

  write(outPipe,out,write_len);

  return 1;

}


/*FUNC: It's a hash function, used for the vectors visited n' recentVisited*/
int rwg_hash(unsigned int address)
{
  unsigned int k = address;
  int p = 7;
  unsigned int pOnes = 0;
  int i;
  for(i = 0;i<p;i++){
    pOnes = pOnes << 1;
    pOnes = pOnes | 1;
  }
  unsigned int A = 2654435769ul;
  unsigned int Ak = A*k;
  int result = (Ak>>(32-p)) & pOnes;

  return result;
}


/*FUNC: looks up a position in the bit vector, returns 0 if 0 and 1 if 1*/
int rwg_bitvector_lookup(unsigned short int *bitVector, unsigned short int pos)
{
  /*check if a position in the bit vector is 1 or 0*/
  unsigned short int rest;
  unsigned short int byte;
  unsigned short int checkpos = 0x1;
  byte = pos/(sizeof(unsigned short int)*8); // find the correct byte pair
  rest = pos % (sizeof(unsigned short int)*8); // which position in the byte pair
  checkpos = checkpos << rest; // shifts the bit to the correct position (-1 since it starts with 0b1)
  if((checkpos & bitVector[byte]) != 0){ 
    return 1;
  }else{
    return 0;
  }
}


/*FUNC: sets a position in the bit vector to 1 */
void rwg_bitvector_setbit(unsigned short int *bitVector, unsigned short int pos)
{
  unsigned short int rest;
  unsigned short int byte;
  unsigned short int setpos = 0x1;
  byte = pos/(sizeof(unsigned short int)*8); // find the correct byte pair
  rest = pos % (sizeof(unsigned short int)*8); // which position in the byte pair
  setpos = setpos << rest; // shifts the bit to the correct position (-1 since it starts with 0b1)
  bitVector[byte] = bitVector[byte] | setpos; //just or the value
}


/*FUNC: takes two bit vectors and performs or*/
void rwg_bitvector_update(unsigned short int *bitVector1, unsigned short int *bitVector2)
{
  int i = 0;
  rwg_header tmphdr;
  for(;i<(sizeof(tmphdr.recentVisited)/2);i++){
    bitVector1[i] = bitVector1[i] | bitVector2[i];
  }
}


/*FUNC: counts the 1:s in the bit vector*/
int rwg_bitvector_count(unsigned short int* bitVector)
{
  int result = 0;
  unsigned short int pos; 
  rwg_header tmphdr;
  int i = 0;
  int j = 0;
  for(;i<(sizeof(tmphdr.recentVisited)/2);i++){
    j = 0;
    pos = 0x1;
    for(;j < 16;j++){
      if((bitVector[i]&pos)> 0){
	result++;
      }
      pos = pos << 1;
    }
  }

  return result;
}


/*FUNC: Looks up all messages that a sender of an incoming REQF do not posess*/
int rwg_wake(unsigned char incomingSender[6], packetBuffer *packetBuff)
{
  /*hashes the sender of a message*/
  unsigned int hashInput;
  memcpy(&hashInput,incomingSender+2,sizeof(int));
  unsigned short int pos = rwg_hash(hashInput);
  int reqf_c = packetBuff->reqf_counter - 1;

  for(;-1 <  reqf_c; reqf_c--){
	

    //this is for copying pointers to the wake buffer
    if(!rwg_bitvector_lookup(((rwg_header *)packetBuff->reqf[reqf_c].reqf)->visited, pos)
       && !rwg_bitvector_lookup(((rwg_header *)packetBuff->reqf[reqf_c].reqf)->recentVisited, pos)){
        
      //Check if the packet already exists in the wake/waiting buffer. Also check that the buffer is not full.
	if(packetBuff->reqf[reqf_c].wake != 1 &&
	   packetBuff->wake_counter < (sizeof(packetBuff->wake)/4) &&
	   packetBuff->waiting[packetBuff->reqf[reqf_c].wait_pos] != &packetBuff->reqf[reqf_c]
	   ){
	  packetBuff->wake[packetBuff->wake_counter] = &packetBuff->reqf[reqf_c];
	  packetBuff->wake[packetBuff->wake_counter]->wake = 1;  
	  packetBuff->reqf[reqf_c].wake_pos = packetBuff->wake_counter; // Will be used when the reqf buffer is refreshed
	  if(TRACE){printf("THE WAKE_COUNTER: %i\n", packetBuff->wake_counter);}
	  packetBuff->wake_counter++;
      }
    }
  }
  if(TRACE){printf("rwg_wake, wake_counter %i\n", packetBuff->wake_counter);}
  return 0;
}


/*FUNC: Handles incoming packets*/
int rwg_handle_incoming(unsigned char *packet, packetBuffer *packetBuff, int outPipe, int length)
{
  int type;
  unsigned char *prot_type;
  unsigned char* srcaddr;
  //unsigned char* packet;
  rwg_header *rwghdr;
  struct ether_header *eptr; 
  eptr = (struct ether_header *) packet;

  /*check the type on the etherheader, RWG type is 0x1111*/
  if(eptr->ether_type == RWG_ETHER_TYPE){ // ethernet headern kastas om (bytevis)

    if(TRACE){
      printf("rwg_handle_incoming: Ethernet packet contains RWG packet!\n");
      printPacket(packet,length);
    } 
  
    /*checking the type of the rwg packet*/
    if(length >= 14){
      rwghdr = (rwg_header *) (packet+14);
      type = (int)rwghdr->type;
    }else{
      if(TRACE){printf("rwg_handle_incoming: It's just an ethernet header\n");}
      return 0;
    }

    /*Switch Case over rwg protocol types : REQF (1), ACK(2), OKTF(3), BS(4)*/  
    switch(type)
      {
      case 1: 
	if(rwg_handle_reqf(rwghdr,packetBuff,outPipe))
	  return 1;
	else
	  break;
	
      case 2:   
	if(packetBuff->w_tail != packetBuff->w_front){ 
	  if(TRACE){printf("rwg_handle_incoming: Type ACK has arrived\n");}
	  rwg_handle_ack(rwghdr,packetBuff);
	}else{
	  if(TRACE){printf("rwg_handle_incoming: Type ACK, but not waiting for one\n");}
	}
	break;
	
      case 3: 
	if(rwg_handle_oktf(rwghdr,packetBuff))
	  return 1;
	else
	  break;
      
      case 4: rwg_handle_bs(rwghdr,packetBuff); break;
	
      default:  if(TRACE){printf("rwg_handle_incoming: Incorrect RWG protocol type\n");}
	break;
      }

  }else{
    if(TRACE){printf("rwg_handle_incoming: Ethernet packet does not contain RWG (type 1111): %i\n", eptr->ether_type);}
  }  
  return 0;
}

/*FUNC: listen on the raw socket, the named pipe "input" and check if there are messages in the wake buffer*/
int rwg_listen(int socket, int inPipe, int outPipe, packetBuffer *packetBuff)
{
  unsigned char buffer[ETH_FRAME_LEN];
  unsigned char pipeBuff[MTU-sizeof(rwg_header)];
  int length = 0;   

  // check if the buffer is full, only listen for ctrl packets
  if(packetBuff->reqf_counter >= (sizeof(packetBuff->reqf)/sizeof(reqf_info))){
    
    length = read(inPipe,pipeBuff,MTU-sizeof(rwg_header));
    if(length >= 3 && pipeBuff[0] == '-' && pipeBuff[1] == 'c' && pipeBuff[2] == '-'){
      rwg_handle_control(pipeBuff,length,packetBuff);
    }

    return 0;

  //check if there is any data on the socket (data received by the NIC)
  }else if ((length = recvfrom(socket, buffer, ETH_FRAME_LEN, 0, NULL, NULL)) > 0 && rwg_handle_incoming(buffer,packetBuff,outPipe,length)){
    return 1;
 
    //check if there is any data in the input pipe, data that will be sent as a new REQF
  }else if((length = read(inPipe,pipeBuff,MTU-sizeof(rwg_header))) > 0){

    // checks if it is a control message for the protocol
    if(length >= 3 && pipeBuff[0] == '-' && pipeBuff[1] == 'c' && pipeBuff[2] == '-'){
      if(rwg_handle_control(pipeBuff,length,packetBuff)){
	return 0;
      }  
    }
 
    //sets the new REQF to active_reqf in the packet buffer, will be handled by rwg_send_reqf_n.
    //Will set the packet length here so I dont have to pass that value.   
    unsigned char *reqf;
    rwg_header rwghdr; 
    reqf = (unsigned char *)malloc(sizeof(rwg_header)+length);
    rwghdr.packetLength = length+sizeof(rwg_header);
    memcpy(reqf,&rwghdr,sizeof(rwg_header));
    memcpy(reqf+sizeof(rwg_header),pipeBuff,length);
    packetBuff->active_reqf.reqf = reqf;
    packetBuff->active_reqf.reqf_pos = packetBuff->reqf_counter; 
    SEND_REQF_N = 1;
    if(TRACE){
      printf("rwg_listen, payload length: %i\n", length);
      printf("SEND_REQF_N is set\n");
    }   
    return 1;
 
    //check if there is data in the wake buffer   
 }else if(packetBuff->wake_counter > 0){ 
   
    //Needs to check that the pointer have not been NULLed when REQF buffer is refreshed
    if(packetBuff->wake[packetBuff->wake_counter - 1] == NULL){
      packetBuff->wake_counter--;
      return 0;
    }
    
    //There is data in the wake buffer, set active and the flag SEND_REQF_F reqf and return 
    packetBuff->active_reqf.reqf = packetBuff->wake[packetBuff->wake_counter - 1]->reqf;
    packetBuff->active_reqf.reqf_pos = packetBuff->wake[packetBuff->wake_counter - 1]->reqf_pos;    
    usleep(25000); // since the other node will wait for ACKS, you don't want to flood the network
    SEND_REQF_F = 1;
    if(TRACE){printf("rwg_listen, data in wake buffer\n");}
    return 1;
  }

  return 0;
}

/*FUNC: handles incoming REQF:s*/
int rwg_handle_reqf(rwg_header *rwghdr, packetBuffer *packetBuff, int outPipe)
{
  if(TRACE){printf("rwg_handle_refq: RWG packet is of type REQF\n");}
  
  //will be used for checking if incoming REQF matches REQF in buffer
  unsigned char packet_id[8];
  int match;
  memcpy(packet_id,rwghdr->origin,sizeof(unsigned char)*6);
  memcpy(packet_id+sizeof(unsigned char)*6,&rwghdr->sequenceNumber,sizeof(unsigned short int));

  unsigned short int* rVisited = rwghdr->recentVisited;
  unsigned short int* visited = rwghdr->visited;
  t_stamp stamp;

  //Checks if this packet already exists in the REQF buffer, and updates the visited list.
  // Also sets this node to the visited list(probably redundant)
 if((match = rwg_match_packetid(packet_id,packetBuff)) >= 0){
   if(TRACE){printPacket((unsigned char *)packetBuff->reqf[match].reqf,((rwg_header *)packetBuff->reqf[match].reqf)->packetLength);}
   rwg_bitvector_update(((rwg_header *)packetBuff->reqf[match].reqf)->visited,rwghdr->visited);
   rwg_bitvector_update(((rwg_header *)packetBuff->reqf[match].reqf)->recentVisited,rwghdr->recentVisited);
 }

 //checks if this node have messages that the sender of the incoming REQF do not have
  unsigned char incomingSender[6];
  memcpy(&incomingSender,rwghdr->sender,sizeof(unsigned char)*6);
  rwg_wake(incomingSender, packetBuff);


  //if already visited but not recent vistited (a message will empty recentVisited when hops > hopLimit)
  if(rwg_bitvector_lookup(visited, hashedAddr) && !rwg_bitvector_lookup(rVisited, hashedAddr)){
    if(TRACE){ printf("rwg_handle_reqf: Is already visited but not recentVisited");}
    if(match >= 0){
      //changes to the new recentVisited vector and changes the sender of the stored packet (so the ACK will be sent to)
      //the correct node)
      memcpy(((rwg_header *)packetBuff->reqf[match].reqf)->recentVisited,rwghdr->recentVisited,sizeof(((rwg_header *)packetBuff->reqf[match].reqf)->recentVisited));
      memcpy(((rwg_header *)packetBuff->reqf[match].reqf)->sender, rwghdr->sender,sizeof(((rwg_header *)packetBuff->reqf[match].reqf)->sender));
      rwg_bitvector_setbit(((rwg_header *)packetBuff->reqf[match].reqf)->recentVisited,hashedAddr);
      packetBuff->active_reqf.reqf = packetBuff->reqf[match].reqf;
      packetBuff->active_reqf.reqf_pos = match;
      SEND_ACK = 1;
      return 1;
    }else{
      if(TRACE){printf("rwg_handle_refq: REQF does not exist in buffer (perhaps restarted node)\n");}
    }

  //if already visited
    }else if(rwg_bitvector_lookup(visited, hashedAddr)){
    if(TRACE){ printf("rwg_handle_refq: Is already visited\n");}
    //Find buffered REFQ with matching packetid and schedule a send BS if groupSize < visted nodes
    if(match >= 0){
      //check size of visited list, save packet id and schedule send bs
      if(rwg_bitvector_count(((rwg_header *)packetBuff->reqf[match].reqf)->visited) >= ((rwg_header *)packetBuff->reqf[match].reqf)->groupSize){
	packetBuff->active_reqf.reqf = packetBuff->reqf[match].reqf;
	packetBuff->active_reqf.reqf_pos = match;
	SEND_BS = 1;
	return 1;
      }
      return 0;
    }else{
      if(TRACE){printf("rwg_handle_refq: REQF does not exist in buffer (perhaps restarted node)\n");}
    }
    //if not already recent visited
  }else if(!rwg_bitvector_lookup(rVisited, hashedAddr)){
    //new reqf have arrived
    if(TRACE){printf("rwg_handle_reqf: Not recent visited\n");}
    if(match < 0){
      //update recentVisited and visited
      rwg_bitvector_setbit(rwghdr->recentVisited,hashedAddr);
      rwg_bitvector_setbit(rwghdr->visited,hashedAddr);

      //writes the payload to the output buffer
      // if(!rwg_write_output((unsigned char *)rwghdr + sizeof(rwg_header),outPipe,rwghdr->packetLength-sizeof(rwg_header))){perror("rwg_handle_refq, failed to write to buffer\n");}
       if(!rwg_write_output((unsigned char *)rwghdr,outPipe,rwghdr->packetLength)){
	 perror("rwg_handle_refq, failed to write to buffer\n");
       }
      //copy packet and add a pointer of the copy to (reqf) packetBuffer buffer, add lenght n' increase counter
      unsigned char *reqfCopy = (unsigned char*)malloc(rwghdr->packetLength);
      memcpy(reqfCopy,rwghdr,rwghdr->packetLength);
      packetBuff->reqf[packetBuff->reqf_counter].reqf = reqfCopy;
      packetBuff->reqf[packetBuff->reqf_counter].wake = 0;
      //set the time stamp arrived_at in buffer
      set_time_stamp(&stamp);
      packetBuff->reqf[packetBuff->reqf_counter].arrived_at.seconds = stamp.seconds;
      packetBuff->reqf[packetBuff->reqf_counter].arrived_at.u_seconds = stamp.u_seconds;
      packetBuff->reqf[packetBuff->reqf_counter].reqf_pos = packetBuff->reqf_counter;
      packetBuff->active_reqf.reqf = packetBuff->reqf[packetBuff->reqf_counter].reqf;
      packetBuff->active_reqf.reqf_pos = packetBuff->reqf_counter;
      packetBuff->reqf_counter++;
      //check if the groupSize have been reached, if so send a BS
      if(rwg_bitvector_count(((rwg_header *)rwghdr)->visited) >= ((rwg_header *)rwghdr)->groupSize){
	SEND_BS = 1;
	return 1;
      }
    }else{
      //this have to be done due to possible missed acks
      if(TRACE){printf("rwg_handle_reqf: REQF exists in buffer but not recentVisited/visited, updates recentVisited\n");}
      rwg_bitvector_setbit(((rwg_header *)packetBuff->reqf[match].reqf)->recentVisited, hashedAddr);
      rwg_bitvector_setbit(((rwg_header *)packetBuff->reqf[match].reqf)->visited, hashedAddr);
      memcpy(((rwg_header *)packetBuff->reqf[match].reqf)->sender,rwghdr->sender,sizeof(char)*6); 
      packetBuff->active_reqf.reqf = packetBuff->reqf[match].reqf;
      packetBuff->active_reqf.reqf_pos = match;
      //Sends out a BS so it the other node will update its bitvectors
      SEND_BS = 1;
      return 1;
    }
    //schedule a send ACK
    SEND_ACK = 1;
    return 1;
  }
  
  if(TRACE){printf("rwg_handle_reqf, recent visited REQF\n");}

  return 0;
}

/*FUNC: handles incoming OKTF:s*/
int rwg_handle_oktf(rwg_header *rwghdr, packetBuffer *packetBuff)
{
  if(TRACE){printf("rwg_handle_oktf: This packet is of type: OKTF\n");}
  int match;
  unsigned char packet_id[8];
  rwg_header tmp;
  
  memcpy(packet_id,rwghdr->origin,sizeof(rwghdr->origin));
  memcpy(packet_id+sizeof(rwghdr->origin),&rwghdr->sequenceNumber,sizeof(rwghdr->sequenceNumber));
  match = rwg_match_packetid(packet_id,packetBuff);

  //update the visted and recent visited vector if the REQF exists in the REQF buffer
  if(match >= 0){
    rwg_bitvector_update(((rwg_header *)packetBuff->reqf[match].reqf)->visited,rwghdr->visited);
    rwg_bitvector_update(((rwg_header *)packetBuff->reqf[match].reqf)->recentVisited,rwghdr->recentVisited);
 }else{
    if(TRACE){printf("rwg_handle_oktf: OKTF no match in reqf buffer\n");}
    return 0;
  }

  //check oktf target
  if(memcmp(rwghdr->target,sender,sizeof(tmp.target)) == 0){
    if(TRACE){ 
      printf("rwg_handle_oktf: Target match\n");
      printPacket((unsigned char *)rwghdr,rwghdr->packetLength);
    }
    packetBuff->active_reqf.reqf = packetBuff->reqf[match].reqf; 
    packetBuff->active_reqf.reqf_pos = match;
  }else{
    if(TRACE){printf("rwg_handle_oktf: OKTF target does not match, visited/recentVisited updated\n");}
    return 0;
  }

  //check if the visited if the visited list >= groupSize
  if((rwg_bitvector_count(((rwg_header *)packetBuff->reqf[match].reqf)->visited) >= ((rwg_header *)packetBuff->reqf[match].reqf)->groupSize)){
    if(TRACE){printf("rwg_handle_oktf, will not forward");}
      return 0;
    }

  //set flag to forward REQF
  SEND_REQF_F = 1;
  return 1;
}

/*FUNC: handles incoming BS:s, returns 1 if there is a match 0 if not*/
int rwg_handle_bs(rwg_header *rwghdr, packetBuffer *packetBuff)
{
  if(TRACE){ printf("rwg_handle_bs: This packet is of type: BS\n");}
  //looks at packet_id (origin n' sequence number), find relating reqf and update visited
  unsigned char packet_id[8];
  int match;
  memcpy(packet_id,rwghdr->origin,sizeof(unsigned char)*6);
  memcpy(packet_id+sizeof(unsigned char)*6,&rwghdr->sequenceNumber,sizeof(unsigned short int));
  match = rwg_match_packetid(packet_id,packetBuff);
  if(match >= 0){
    /*update visited*/
    rwg_bitvector_update(((rwg_header *)packetBuff->reqf[match].reqf)->visited, rwghdr->visited);
    rwg_bitvector_update(((rwg_header *)packetBuff->reqf[match].reqf)->recentVisited, rwghdr->recentVisited);
    return 1;
  }
  return 0;
}

/*FUNC: handle ACK*/ 
int rwg_handle_ack(rwg_header *rwghdr, packetBuffer *packetBuff)
{
  if(TRACE){printf("rwg_handle_ack, this packet is of type: ACK\n");}
  //check if the ACK target matches this nodes mac addr
  if(memcmp(rwghdr->target,sender,sizeof(sender)) == 0){
    if(TRACE){printf("rwg_handle_ack, ACK target matches mac addr\n");}
    //save a copy of the ack
    unsigned char *ackCopy = (unsigned char*)malloc(rwghdr->packetLength);
    memcpy(ackCopy,rwghdr,rwghdr->packetLength);
    packetBuff->ack[packetBuff->ack_counter % (sizeof((*packetBuff).ack)/4)] = ackCopy;
    packetBuff->ack_counter++;
    //free old ACK, if there is one at the new position of the buffer pointer
    if(packetBuff->ack[packetBuff->ack_counter % (sizeof((*packetBuff).ack)/4)] != NULL){
      if(TRACE){printf("rwg_handle_ack, ack_counter: %i\n",packetBuff->ack_counter % (sizeof((*packetBuff).ack)/4));}
      free(packetBuff->ack[packetBuff->ack_counter % (sizeof((*packetBuff).ack)/4)]);
      packetBuff->ack[packetBuff->ack_counter % (sizeof((*packetBuff).ack)/4)] = NULL;  
    }
    return 1;
  }else{
    if(TRACE){printf("rwg_handle_ack, ACK target DOES NOT MATCH mac addr\n");}
  }
  return 0;
}
