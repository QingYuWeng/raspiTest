# encoding: utf-8
import RPi.GPIO as GPIO
import socket
import time
import sys
import threading
import signal  
import atexit  
import serial
import os

data=""

p=""


GPIO.setmode(GPIO.BOARD)
GPIO.setup(12, GPIO.OUT)

HOST_IP="172.27.35.4"
HOST_PORT=7654
ser=serial.Serial('/dev/ttyACM0',9600,timeout=1)
print("Starting socket :TCP...")
socket_tcp=socket.socket(socket.AF_INET,socket.SOCK_STREAM)

print("TCP server listen @%s:%d!" %(HOST_IP,HOST_PORT))
host_addr = (HOST_IP,HOST_PORT)
socket_tcp.bind(host_addr)
socket_tcp.listen(3)

def light(socket_2):
    global data
    print(data)
    socket_2.send(data)
    GPIO.output(12, 1)

def down(socket_2):
    global data
    print(data)
    socket_2.send(data)
    GPIO.output(12, 0)

def sFan(socket_2):
    global data
    print(data)
    socket_2.send(data)
    ser.write('s')

def mFan(socket_2):
    global data
    print(data)
    socket_2.send(data)
    ser.write('m')

def lFan(socket_2):
    global data
    print(data)
    socket_2.send(data)
    ser.write('l')

def tFan(socket_2):
    global data
    print(data)
    socket_2.send(data)
    ser.write('t')

def stop(socket_2):
    global data
    global p
    print(data)
    socket_2.send(data)
    p.stop()

def shake(socket_2):
    global data
    global p
    print(data)
    socket_2.send(data)
    servopin = 7
    GPIO.setup(servopin, GPIO.OUT, initial=False) 
    p = GPIO.PWM(servopin,50) #50HZ  
    p.start(0) 
    while True:
        for i in range(0,181,10):
            p.ChangeDutyCycle(2.5 + 10 * i / 180)
            time.sleep(0.02)
            p.ChangeDutyCycle(0)
            time.sleep(0.02)

        for i in range(181,0,-10):
            p.ChangeDutyCycle(2.5 + 10 * i / 180)
            time.sleep(0.02) 
            p.ChangeDutyCycle(0)
            time.sleep(0.02)

def monitor(arg):
    os.system("python3 server.py")


def th4(socket_4):
    while True:
        state=ser.readall()
        if len(state)>0:
            if(state[0]=="D"):
                print "Down"
                socket_4.send("Down!")
            if(state[0]=="N"):
                print "NoDown"
                socket_4.send("NoDown")

def th1(socket_1):

#    t = threading.Thread(target=dump, args=(socket_1,))
#    t.start()

    state=""
    SENSOR = 16
    GPIO.setup(SENSOR, GPIO.IN, pull_up_down=GPIO.PUD_UP)
    while True:
        if (GPIO.input(SENSOR) == 0):
            socket_1.send("Abnormal sound!!")
        elif (GPIO.input(SENSOR)==1):
            socket_1.send("Normal")

#        state=ser.readall()
#        if len(state)>0:
#            if(state[0]=="D"):
#                print "Down"
#                socket_1.send("Down!")
#            if(state[0]=="N"):
#                print "NoDown"
#                socket_1.send("NoDown")
        time.sleep(0.1)
def th2(socket_2):
    global data
    socket_2.send("welcome to server")
    while True:
        data=socket_2.recv(1024)
        if not data:
            break
        if data=='open':
            t = threading.Thread(target=light, args=(socket_2,))
            t.start()
        if data=='down':
            t = threading.Thread(target=down, args=(socket_2,))
            t.start()
        if data=="shake":
            t = threading.Thread(target=shake, args=(socket_2,))
            t.start()
        if data=="stop":
            t = threading.Thread(target=stop, args=(socket_2,))
            t.start()        
        if data=="s":
            t = threading.Thread(target=sFan, args=(socket_2,))
            t.start()
        if data=="t":
            t = threading.Thread(target=tFan, args=(socket_2,))
            t.start()
        if data=="m":
            t = threading.Thread(target=mFan, args=(socket_2,))
            t.start()
        if data=="l":
            t = threading.Thread(target=lFan, args=(socket_2,))
            t.start()

def th3(socket_3):
    while True:
        THdata=[]
        j=0
        GPIO.setup(3,GPIO.OUT)
        GPIO.output(3, GPIO.LOW)
        time.sleep(0.02)
        GPIO.output(3, GPIO.HIGH)
        GPIO.setup(3,GPIO.IN)
        while GPIO.input(3)==GPIO.LOW:
            continue
        
        while GPIO.input(3)==GPIO.HIGH:
            continue
        
        while j<40:
            k=0
            while GPIO.input(3)==GPIO.LOW:
                continue

            while GPIO.input(3)==GPIO.HIGH:
                k+=1
                if k>100:
                    break
                
            if k<8:
                THdata.append(0)
            else:
                THdata.append(1)
            j+=1
        print  "sensor is working."

        humidity_bit = THdata[0:8]
        humidity_point_bit = THdata[8:16]

        temperature_bit=THdata[16:24]
        temperature_point_bit=THdata[24:32]

        check_bit=THdata[32:40]

        humidity=0
        humidity_point=0
        temperature=0
        temperature_point=0
        check=0

        for i in range(8):
            humidity+=humidity_bit[i]*2**(7-i)
            humidity_point+=humidity_point_bit[i]*2**(7-i)
            temperature+=temperature_bit[i]*2**(7-i)
            temperature_point+=temperature_point_bit[i]*2**(7-i)
            check+=check_bit[i]*2**(7-i)

        tmp=humidity+humidity_point+temperature+temperature_point

        if check==tmp:
            print "temperature : ",temperature, ",humidity : ",humidity
            T="%d"%temperature
            H="%d"%humidity
            socket_3.send("Tem_Hum:"+T+" "+H)
        else:
            print "wrong"
            print "temperature : ",temperature, ",humidity :",humidity,"check : ",check,"tmp : ",tmp
            T="%d"%temperature
            H="%d"%humidity
        time.sleep(5)

while True:
    print('waiting for connection..')

    socket_con, (client_ip,client_port)=socket_tcp.accept()
    print('connection accepted from %s.'%client_ip)
    
    t = threading.Thread(target=th1, args=(socket_con,))
    t.start()
    
    socket_con, (client_ip, client_port) = socket_tcp.accept()
    print("Connection accepted from %s." %client_ip)

    t2 = threading.Thread(target=th3, args=(socket_con,))
    t2.start()

    socket_con, (client_ip, client_port) = socket_tcp.accept()
    print("Connection accepted from %s." %client_ip)
                
    t3 = threading.Thread(target=th4, args=(socket_con,))
    t3.start()
    
    socket_con, (client_ip, client_port) = socket_tcp.accept()
    print("Connection accepted from %s." %client_ip)
    
    t1 = threading.Thread(target=th2, args=(socket_con,))
    t1.start()

    t4 = threading.Thread(target=monitor, args=(1,))
    t4.start()
  
    t.join()
    t2.join()
    t3.join()
    t1.join()
    t4.join()
GPIO.cleanup()
socket_tcp.close()
ser.close()

