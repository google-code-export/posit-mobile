#
# Makefile for RWG
#

CC=arm-none-linux-gnueabi-gcc -static
#CC=gcc

rwgexec : rwg.o rwg_header.o rwg_send.o rwg_sockets.o rwg_receive.o
	$(CC) rwg.o rwg_header.o rwg_send.o rwg_sockets.o rwg_receive.o -o rwgexec -lpthread

rwg.o : rwg.c
	$(CC) -c rwg.c

rwg_header.o : rwg_header.c
	$(CC) -c rwg_header.c

rwg_send.o : rwg_send.c
	$(CC) -c rwg_send.c

rwg_sockets.o : rwg_sockets.c
	$(CC) -c rwg_sockets.c

rwg_receive.o : rwg_receive.c
	$(CC) -c rwg_receive.c

#rwg_ack_handler.o : rwg_ack_handler.c
#	$(CC) -c rwg_ack_handler.c

clean:
	-rm *.o *~
