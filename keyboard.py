import pygame

import RPi.GPIO as GPIO
from time import sleep
 
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
 
class motor():
    def __init__(self,Ena,In1,In2,Enb,In3,In4):
        self.Ena = Ena
        self.In1 = In1
        self.In2 = In2
        self.Enb = Enb
        self.In3 = In3
        self.In4 = In4
        GPIO.setup(self.Ena,GPIO.OUT)
        GPIO.setup(self.In1,GPIO.OUT)
        GPIO.setup(self.In2,GPIO.OUT)
        GPIO.setup(self.Enb,GPIO.OUT)
        GPIO.setup(self.In3,GPIO.OUT)
        GPIO.setup(self.In4,GPIO.OUT)
        self.pwma = GPIO.PWM(self.Ena, 100)
        self.pwma.start(0)
        self.pwmb = GPIO.PWM(self.Enb, 100)
        self.pwmb.start(0)
    def moveF(self,x=100,t=0):
        self.pwma.ChangeDutyCycle(x)
        self.pwmb.ChangeDutyCycle(x)
        GPIO.output(self.In1,GPIO.HIGH)
        GPIO.output(self.In2,GPIO.LOW)
        GPIO.output(self.In3,GPIO.HIGH)
        GPIO.output(self.In4,GPIO.LOW)
        sleep(t)
    def moveB(self,x=100,t=0):
        self.pwma.ChangeDutyCycle(x)
        self.pwmb.ChangeDutyCycle(x)
        GPIO.output(self.In1,GPIO.LOW)
        GPIO.output(self.In2,GPIO.HIGH)
        GPIO.output(self.In3,GPIO.LOW)
        GPIO.output(self.In4,GPIO.HIGH)
        sleep(t)
    def moveright(self,x=100,t=0):
        self.pwma.ChangeDutyCycle(x)
        self.pwmb.ChangeDutyCycle(60)
        GPIO.output(self.In1,GPIO.HIGH)
        GPIO.output(self.In2,GPIO.LOW)
        GPIO.output(self.In3,GPIO.HIGH)
        GPIO.output(self.In4,GPIO.LOW)
        sleep(t)
    def moveLeft(self,x=100,t=0):
        self.pwma.ChangeDutyCycle(60)
        self.pwmb.ChangeDutyCycle(x)
        GPIO.output(self.In1,GPIO.HIGH)
        GPIO.output(self.In2,GPIO.LOW)
        GPIO.output(self.In3,GPIO.HIGH)
        GPIO.output(self.In4,GPIO.LOW)
        sleep(t) 
def init():
    pygame.init()
    win = pygame.display.set_mode((100,100))
 
def getKey(keyName):
    ans = False
    for eve in pygame.event.get():pass
    keyInput = pygame.key.get_pressed()
    myKey = getattr(pygame,'K_{}'.format(keyName))
    if keyInput [myKey]:
        ans = True
    pygame.display.update()
 
    return ans
 
def main():
    motor1 = motor(2,3,4,17,27,22)
    if getKey('LEFT'):
        motor1.moveLeft(t=1)
    if getKey('RIGHT'):
        motor1.moveright(t=1)
    if getKey('UP'):
        motor1.moveF(80,t=1)
    if getKey('DOWN'):
        motor1.moveB(80,t=1)
 
if __name__ == '__main__':
    init()
    while True:
        main()
