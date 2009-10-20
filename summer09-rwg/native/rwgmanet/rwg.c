
/*
 * rwg.c
 *
 */


#include "rwg.h"
#include "rwg_send.h"
#include "rwg_header.h"
#include "rwg_receive.h"
#include "rwg_sockets.h"

/*to serialize the threads*/
pthread_cond_t cond_listen = PTHREAD_COND_INITIALIZER;
pthread_cond_t cond_send = PTHREAD_COND_INITIALIZER;
pthread_cond_t cond_ack = PTHREAD_COND_INITIALIZER;
pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

void refresh_reqf_buffer(packetBuffer *packetBuff){

  t_stamp stamp;
  set_time_stamp(&stamp);
  int reqf_c; 
  int ttl_check;
  packetBuff->temp_reqf_counter = 0;
 
  for(reqf_c = 0;reqf_c < packetBuff->reqf_counter;reqf_c++){
    
    ttl_check = ((rwg_header *)packetBuff->reqf[reqf_c].reqf)->TTL - (stamp.seconds - packetBuff->reqf[reqf_c].arrived_at.seconds); 

    if(((rwg_header *)packetBuff->reqf[reqf_c].reqf != NULL) && (ttl_check < 0)){
      //To make sure that there are no pointers pointing to the freed addr at the heap
      if((packetBuff->waiting[packetBuff->reqf[reqf_c].wait_pos].reqf == packetBuff->reqf[reqf_c].reqf)){
	packetBuff->waiting[packetBuff->reqf[reqf_c].wait_pos].reqf = NULL;
      }
      //To make sure that there are no pointers pointing to the freed addr at the heap
      if(packetBuff->wake[packetBuff->reqf[reqf_c].wake_pos] == &packetBuff->reqf[reqf_c]){
	packetBuff->wake[packetBuff->reqf[reqf_c].wake_pos] = NULL;
      }
      // remove the packet, since TTL < 0 and the REQF is not in the waiting buffer or the wake buffer
      free(packetBuff->reqf[reqf_c].reqf);
      packetBuff->reqf[reqf_c].reqf = NULL;
      packetBuff->reqf[reqf_c].arrived_at.seconds = 0;
      packetBuff->reqf[reqf_c].arrived_at.u_seconds = 0;
      packetBuff->reqf[reqf_c].wake = 0;
      //if(TRACE){printf("refresh_reqf_buffer, removing old reqf\n");}
    }else if(((rwg_header *)packetBuff->reqf[reqf_c].reqf != NULL)){
      //move to temp reqf buffer ,,, set new timestamp
      //if(TRACE){printf("refresh_reqf_buffer, save reqf (ttl_check) %i\n", ttl_check);}
      packetBuff->temp_reqf[packetBuff->temp_reqf_counter] = packetBuff->reqf[reqf_c];
      packetBuff->temp_reqf[packetBuff->temp_reqf_counter].arrived_at = stamp;
      packetBuff->temp_reqf[packetBuff->temp_reqf_counter].reqf_pos = packetBuff->temp_reqf_counter;
      if(ttl_check >= 0){
      // Just in case, since the TTL is an unsigned int, it will behave very odd if it will be assigned a neg value 
	((rwg_header *)packetBuff->reqf[reqf_c].reqf)->TTL = ttl_check;
      }else{
	((rwg_header *)packetBuff->reqf[reqf_c].reqf)->TTL = 0;
      }
      packetBuff->temp_reqf_counter++;
    }
  }

  //Just replace the real REQF buffer with the temporary reqf buffer
  packetBuff->reqf_counter = packetBuff->temp_reqf_counter;
  if(packetBuff->reqf_counter != 0){
    memcpy(packetBuff->reqf,packetBuff->temp_reqf,(packetBuff->temp_reqf_counter)*sizeof(reqf_info));
  }
}

/*FUNC: handles thread one, is responsible for sending new packets and forwarding*/
void *handleThread1(void** args){

  pthread_mutex_lock(&mutex);

  /*shared resources*/
  int socket = (int)args[0];
  packetBuffer *packetBuff = (packetBuffer *)args[1];
  //int inPipe = (int)args[2];  
  unsigned char* macaddr = (unsigned char*)args[3];

  /*The main loop for thread one*/
  for(;;){
    pthread_cond_wait(&cond_send,&mutex);  
    rwg_send_routine(socket,packetBuff);
    pthread_cond_signal(&cond_send);
  }
}

/*FUNC: handles thread two, it will be responsible for listening, to new input that will be sent n' incoming traffic such as REQF, OKTF...*/
void *handleThread2(void** args){
  
  pthread_mutex_lock(&mutex);
  /*those are shared between threads*/
  int socket = (int)args[0];
  packetBuffer *packetBuff = (packetBuffer *)args[1];
  int inPipe = (int)args[2]; 
  unsigned char* macaddr = (unsigned char*)args[3];
  unsigned int silentTimer;
  unsigned int refreshReqfTimer;
  int outPipe = (int)args[4]; 
  int retransmit_c;
  int retransmit_stop;
  t_stamp stamp;

  /*the main loop for thread two*/
  if(TRACE){printf("TRACE, inside T2, wait\n");}
  //  pthread_cond_wait(&cond_listen,&mutex);
  silentTimer = (unsigned int)time(NULL); 
  refreshReqfTimer = (unsigned int)time(NULL); 
  
  for(;;){
    
    set_time_stamp(&stamp);
    
    /*make sure the REQF buffer is not full, if it is sleep a short period and refresh the REQF buffer*/
    if(packetBuff->reqf_counter >= (sizeof(packetBuff->reqf)/sizeof(reqf_info))){
      refresh_reqf_buffer(packetBuff);
      usleep(5000);
      /*Will refresh the reqf_buffer every 7 seconds, to remove reqfs with Time To Live < 0*/
    }else if((refreshReqfTimer+7)<(unsigned int)time(NULL)){
      if(TRACE){printf("Refresh the REQF buffer by checking TTL (must be done to avoid overflow)\n");}
      refresh_reqf_buffer(packetBuff);
      refreshReqfTimer = (unsigned int)time(NULL);
      /*Checks if there are any reqfs in the waiting buffer (waiting for ACKS), 
	also checks the timestamp (waits 0.1s). Sets SEND_OKTF and signals the other thread*/
    }else if(packetBuff->w_tail != packetBuff->w_front &&
	     (check_time_stamp(packetBuff->waiting[packetBuff->w_tail % (sizeof((*packetBuff).waiting)/sizeof(wait_info))].w_stamp, stamp, 100000))){
      SEND_OKTF = 1; 
      pthread_cond_signal(&cond_send);
      pthread_cond_wait(&cond_send,&mutex);
      silentTimer = (unsigned int)time(NULL);
      /*If the network have been silent for 5 seconds send a random REQF*/
    }else if((silentTimer+5)<(unsigned int)time(NULL)){
      if(TRACE){printf("Network i silent, remove old reqfs (TTL < 0) and send random REQF\n");}
      refresh_reqf_buffer(packetBuff);
      SEND_REQF_R = 1;
      pthread_cond_signal(&cond_send);
      pthread_cond_wait(&cond_send,&mutex);
      silentTimer = (unsigned int)time(NULL);
      
      /*Check if there is any packets on the socket or the named pipe*/
    }else if(rwg_listen(socket,inPipe,outPipe,packetBuff)){
      if(TRACE){printf("handleThread2, All the counters for the mod buffers; ack_c: %i w_front: %i w_tail: %i\n",packetBuff->ack_counter, packetBuff->w_front, packetBuff->w_tail);}
      if(TRACE){printf("Time... %i : %i\n",stamp.seconds,stamp.u_seconds);}  
      silentTimer = (unsigned int)time(NULL);   
      pthread_cond_signal(&cond_send);
      pthread_cond_wait(&cond_send,&mutex);
      /*If nothing is happening let the CPU rest a little*/
    }else{
       usleep(5000);
    }
  }
}


/******************************* MAIN ************************************/
int main(int argc,char **argv)
{ 
  pthread_t thread1;
  pthread_t thread2;
  pthread_t thread3;
  char *dev;
  void *arguments[5];
  int socket;
  unsigned char macaddr[6];

  /*Actually namned pipes*/
  int outPipe;
  int inPipe;
  
  /*set default values*/
  TRACE = 0;
  groupSize = 20;
  hops = 7;
  TTL = 10;

  /*fetch the device name and check flags*/
  if(argc < 2 || 10 < argc){
    printf("Usage: ./rwgexec -t -h <hops> -l <TTL> -g <groupSize> -i <interface> \n");
    return 0;
  }else{
    int i = 1;
    char arg[2]; 
    arg[2] = '\0';
    for(i;i<argc;i++){
      strncpy(arg,argv[i],2);
      if(!strcmp(arg,"-t")){
	TRACE = 1;
	printf("Setting trace mode\n");
      }else if(!strcmp(arg,"-h")){
	
	hops = atoi(argv[++i]);
	if(0 > hops || hops > 99){
	  printf("hops value to low/high\n");
	  return 0;
	}

      }else if(!strcmp(arg,"-g")){

       	groupSize = atoi(argv[++i]);
	if(0 > groupSize || groupSize > 99){
	  printf("groupSize value to low/high\n");
	  return 0;
	}
	
	
      }else if(!strcmp(arg,"-i")){
	
	if((dev = argv[++i]) == NULL){
	  printf("Incorrect device");
	  return 0;
	}

      }else if(!strcmp(arg,"-l")){
	
	TTL = atoi(argv[++i]);
	if(0 > TTL || TTL > 3600){
	  printf("TTL value to low/high\n");
	  return 0;
	}

      }else{
	printf("Usage: ./rwgexec -t -h <hops> -g <groupSize> -i <interface> \n");
	return 0;
      }
    }
  }

  /*open streams, named pipes for communicating with other processess*/
  inPipe = open("input",O_RDWR|O_NONBLOCK);
  outPipe = open("output",O_RDWR|O_NONBLOCK);

  if(outPipe == -1 || inPipe == -1 ){
    perror("Failed to open pipe");
  }

  if(TRACE){
    printf("hops, %i\n", hops);
    printf("groupSize, %i\n", groupSize);
    printf("TTL, %i\n", TTL);
  }
  
  /*this method is later used to get timestamps*/
  if(TRACE){
 
    struct timeval tv;
    gettimeofday(&tv,NULL);
    double s = (double)tv.tv_sec; 
    double u_s = (double)tv.tv_usec;
    double stamp;
    u_s = u_s * 0.000001;
    stamp = s + u_s;
    printf("Time check: %lf\n",stamp);
  }

  /*creates the packetBuffer. A struct that holds buffers of different types*/
  packetBuffer pbuff;
  pbuff.reqf_counter = 0;
  pbuff.temp_reqf_counter = 0;
  pbuff.ack_counter = 0;
  pbuff.wake_counter = 0;
  pbuff.w_front = 0;
  pbuff.w_tail = 0;
  int i = 0;
  
  for(;i<(sizeof(pbuff.ack)/4);i++){
    pbuff.ack[i] = NULL;
  }
 
  for(i=0;i<(sizeof(pbuff.waiting)/4);i++){
    pbuff.waiting[i].reqf = NULL;
    pbuff.waiting[i].w_stamp.seconds = 0;
    pbuff.waiting[i].w_stamp.u_seconds = 0;
  }

  for(i=0;i<(sizeof(pbuff.reqf)/sizeof(reqf_info));i++){
    pbuff.reqf[i].reqf = NULL;
    pbuff.reqf[i].arrived_at.seconds = 0;
    pbuff.reqf[i].arrived_at.u_seconds = 0;
    pbuff.reqf[i].wake = 0;
    pbuff.temp_reqf[i].reqf = NULL;
    pbuff.temp_reqf[i].arrived_at.seconds = 0;
    pbuff.temp_reqf[i].arrived_at.u_seconds = 0;
    pbuff.temp_reqf[i].wake = 0;
  }

  packetBuffer *packetBuff = &pbuff;

  /*create a raw socket*/
  socket = rwg_create_socket(ETH_P_ALL);

  /*binds socket to interface*/
  if(!rwg_bind_socket(dev, socket, ETH_P_ALL))
    {perror("Error: could not bind raw socket to interface"); return 0;}

  /*sets O_NONBLOCK flag, so it wont block when buffer is empty*/
  rwg_nonblock_socket(socket,1);

  /*get the mac addr from the network device*/
  rwg_get_macaddr(macaddr,socket,dev);

  /*prints the macaddr*/
  if(TRACE){
    int k = 0;
    printf("macaddr: ");  
    for(;k<6;k++){
      printf(" %.2x",macaddr[k]); 
    }
    printf("\n");
  }

  /*sets sender*/
  memcpy(sender, macaddr, sizeof(unsigned char)*6);

  /*set sequenceNumber*/
  sequenceNumber = 0;

  /*arguments for the threads, shared resources.. such as packet buffers*/
  arguments[0] = (void*)socket;
  arguments[1] = (void*)packetBuff;
  arguments[2] = (void*)inPipe;
  arguments[3] = (void*)macaddr;
  arguments[4] = (void*)outPipe;

  /*create the threads, sending, listening, handle acks*/    
  if(0 != pthread_create(&thread1,NULL,(void*)handleThread1,arguments))
    {perror("Error: failed to create thread\n");}
  if(0 != pthread_create(&thread2,NULL,(void*)handleThread2,arguments))
    {perror("Error: failed to create thread\n");}
  if(TRACE){printf("TRACE, inside MAIN\n");}

  /*waits for the threads to finish before exit*/
  pthread_join(thread1,NULL);
  pthread_join(thread2,NULL);

  /*close stream*/
  close(inPipe);
  close(outPipe);

  /*close the socket*/
   close(socket);

  printf("finished\n");
 
  return 1;  
}
