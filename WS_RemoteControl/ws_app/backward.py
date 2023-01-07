import RPi.GPIO as gpio
from .consumers import PINS
import sys
PINS = PINS()
vitess=int(sys.argv[1])
# setup GPIO
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

# move backword
gpio.output(PINS.IN1, gpio.LOW)
gpio.output(PINS.IN2, gpio.HIGH)
gpio.output(PINS.IN3, gpio.LOW)
gpio.output(PINS.IN4, gpio.HIGH)
