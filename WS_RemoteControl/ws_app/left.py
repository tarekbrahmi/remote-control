import RPi.GPIO as gpio
import sys
from .consumers import PINS
PINS = PINS()
vitess=int(sys.argv[1])
gpio.output(PINS.IN1, True)
gpio.output(PINS.IN2, False)
gpio.output(PINS.IN3, False)
gpio.output(PINS.IN4, True)
