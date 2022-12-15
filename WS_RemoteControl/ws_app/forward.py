import RPi.GPIO as gpio
from .consumers import PINS
import sys
PINS = PINS()
vitess=int(sys.argv[1])
gpio.output(PINS.IN1, False)
gpio.output(PINS.IN2, True)
gpio.output(PINS.IN3, True)
gpio.output(PINS.IN4, False)
