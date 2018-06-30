# -*- coding:UTF-8 -*-
import socket
import time
import sys
import threading


def th1(socket_1):
	i=0
	while True:
		print(i)
		socket_1.send("%d"%i)
		i=i+1
		time.sleep(0.5)

def th2(socket_2):
	socket_2.send("Welcome to RPi TCP server!")
	while True:
		data=socket_2.recv(1024)
		
		if data:
			print(data)
			socket_2.send(data)

HOST_IP = "192.168.23.4"
HOST_PORT = 7654
 
print("Starting socket: TCP...")
socket_tcp = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print("TCP server listen @ %s:%d!" %(HOST_IP, HOST_PORT) )
host_addr = (HOST_IP, HOST_PORT)
socket_tcp.bind(host_addr)
socket_tcp.listen(2)

print ('waiting for connection...')

socket_con, (client_ip, client_port) = socket_tcp.accept()
print("Connection accepted from %s." %client_ip)

t = threading.Thread(target=th1, args=(socket_con,))
t.start()

socket_con, (client_ip, client_port) = socket_tcp.accept()
print("Connection accepted from %s." %client_ip)

t1 = threading.Thread(target=th2, args=(socket_con,))
t1.start()

t.join()
t1.join()
 
socket_tcp.close()