import RPi.GPIO as gpio
from .consumers import PINS
import sys
PINS = PINS()
vitess=int(sys.argv[1])
gpio.output(PINS.IN1, True)
gpio.output(PINS.IN2, False)
gpio.output(PINS.IN3, False)
gpio.output(PINS.IN4, True)
