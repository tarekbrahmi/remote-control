import RPi.GPIO as gpio
import sys
from time import sleep
class PINS:
    ######## PINS ########
    EN_LEFT = 2
    IN1 = 3
    IN2 = 4

    EN_RIGHT = 17
    IN3 = 27
    IN4 = 22
vitess=int(sys.argv[1])
# setup GPIO
print("execute forward.py with args %s "%(vitess))
gpio.setmode(gpio.BCM)
gpio.setwarnings(False)
gpio.setup(PINS.IN1, gpio.OUT)
gpio.setup(PINS.IN2, gpio.OUT)
gpio.setup(PINS.IN3, gpio.OUT)
gpio.setup(PINS.IN4, gpio.OUT)
gpio.setup(PINS.EN_LEFT, gpio.OUT)
gpio.setup(PINS.EN_RIGHT, gpio.OUT)
# GPIO.PWM(channel, frequence)
EN_RIGHT_PWM = gpio.PWM(PINS.EN_RIGHT, 100)
# ici, rapport_cyclique vaut entre 0.0 et 100.0
EN_RIGHT_PWM.start(0)
# gpio.output(PINS.EN_RIGHT, gpio.HIGH)
EN_LEFT_PWM = gpio.PWM(PINS.EN_LEFT, 100)
EN_LEFT_PWM.start(0)
EN_LEFT_PWM.ChangeDutyCycle(vitess)
EN_RIGHT_PWM.ChangeDutyCycle(vitess)

# move forward
gpio.output(PINS.IN1, gpio.HIGH)
gpio.output(PINS.IN2, gpio.LOW)
gpio.output(PINS.IN3, gpio.HIGH)
gpio.output(PINS.IN4, gpio.LOW)

sleep(1)
#stop
#gpio.output(PINS.IN1, gpio.LOW)
#gpio.output(PINS.IN2, gpio.LOW)
#gpio.output(PINS.IN3, gpio.LOW)
#gpio.output(PINS.IN4, gpio.LOW)


