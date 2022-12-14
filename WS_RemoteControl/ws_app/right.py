import RPi.GPIO as gpio
from .consumers import PINS
PINS = PINS()
gpio.output(PINS.IN1, False)
gpio.output(PINS.IN2, True)
gpio.output(PINS.IN3, False)
gpio.output(PINS.IN4, True)
