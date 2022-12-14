from channels.generic.websocket import AsyncWebsocketConsumer
import json
from time import sleep
import RPi.GPIO as gpio
import subprocess


class PINS:
    ######## PINS ########
    EN_LEFT = 13
    IN1 = 7
    IN2 = 11

    EN_RIGHT = 12
    IN3 = 8
    IN4 = 10


class DECISION:
    IDLE = "IDLE"
    FORWARD = "FORWARD"
    BACKWARD = "BACKWARD"
    RIGHT = "RIGHT"
    LEFT = "LEFT"


class CommandConsumer(AsyncWebsocketConsumer):
    # decisions
    DECISION = DECISION
    PINS = PINS
    SLEEP_TIME = 1

    def clean():
        gpio.cleanup()

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        gpio.setmode(gpio.BCM)
        gpio.setwarnings(False)
        gpio.setmode(gpio.BOARD)
        gpio.setup(self.PINS.IN1, gpio.OUT)
        gpio.setup(self.PINS.IN2, gpio.OUT)
        gpio.setup(self.PINS.IN3, gpio.OUT)
        gpio.setup(self.PINS.IN4, gpio.OUT)
        self.EN_RIGHT_PWM = gpio.PWM(self.PINS.EN_RIGHT, 100)
        self.EN_RIGHT_PWM.start(50)
        gpio.output(self.PINS.EN_RIGHT, gpio.HIGH)
        self.EN_LEFT_PWM = gpio.PWM(self.PINS.EN_LEFT, 100)
        self.EN_LEFT_PWM.start(50)
        gpio.output(self.PINS.EN_LEFT, gpio.HIGH)
        print("set up the GPIO")

    async def connect(self):
        self.group_name = "Command_"
        self.groups.append('Command_GROUP_')
        await self.channel_layer.group_add("Command_", self.channel_name)
        await self.accept()
        await self.channel_layer.group_send("Command_", {
            "type": "send_message",
            "data": "Hello from server"
        })

    async def send_message(self, event):
        await self.send(
            text_data=json.dumps({
                'data': event['data']
            }))

    async def handelCommand(self, command, vitess=0, exec=False):
        if str(command) == self.DECISION.FORWARD and not exec:
            self.forward(vitess=vitess)
        if str(command) == self.DECISION.BACKWARD and not exec:
            self.backward(vitess=vitess)
        if str(command) == self.DECISION.LEFT and not exec:
            self.turn_left(vitess=vitess)
        if str(command) == self.DECISION.RIGHT and not exec:
            self.turn_right(vitess=vitess)
        if str(command) == self.DECISION.IDLE and not exec:
            self.stop()
        ###############################################
        # TODO add argument vitess for command
        if str(command) == self.DECISION.FORWARD and exec:
            self.execforward(vitess=vitess)
        if str(command) == self.DECISION.BACKWARD and exec:
            self.execbackward(vitess=vitess)
        if str(command) == self.DECISION.LEFT and exec:
            self.execturn_left(vitess=vitess)
        if str(command) == self.DECISION.RIGHT and exec:
            self.execturn_right(vitess=vitess)
        if str(command) == self.DECISION.IDLE and exec:
            self.stop()

    async def execforward(self, vitess):
        path = "python3 ./forward.py %d"
        x = subprocess.Popen(path % (vitess))

    async def execbackward(self, vitess):
        path = "python3 ./backward.py %d"
        x = subprocess.Popen(path % (vitess))

    async def execturn_right(self, vitess):
        path = "python3 ./right.py %d"
        x = subprocess.Popen(path % (vitess))

    async def execturn_left(self, vitess):
        path = "python3 ./left.py %d"
        x = subprocess.Popen(path % (vitess))

    def forward(self, vitess: int):
        #TODO add the  ChangeDutyCycle to vitess
        self.EN_LEFT_PWM.ChangeDutyCycle(vitess)
        self.EN_RIGHT_PWM.ChangeDutyCycle(vitess)
        gpio.output(self.PINS.IN1, False)
        gpio.output(self.PINS.IN2, True)
        gpio.output(self.PINS.IN3, True)
        gpio.output(self.PINS.IN4, False)
        sleep(self.SLEEP_TIME)
        # self.clean()

    def backward(self, vitess: int):
        self.EN_LEFT_PWM.ChangeDutyCycle(vitess)
        self.EN_RIGHT_PWM.ChangeDutyCycle(vitess)
        gpio.output(self.PINS.IN1, True)
        gpio.output(self.PINS.IN2, False)
        gpio.output(self.PINS.IN3, False)
        gpio.output(self.PINS.IN4, True)
        sleep(self.SLEEP_TIME)
        # self.clean()

    def turn_right(self, vitess: int):
        self.EN_LEFT_PWM.ChangeDutyCycle(vitess)
        self.EN_RIGHT_PWM.ChangeDutyCycle(100-vitess)
        gpio.output(self.PINS.IN1, False)
        gpio.output(self.PINS.IN2, True)
        gpio.output(self.PINS.IN3, False)
        gpio.output(self.PINS.IN4, True)
        sleep(self.SLEEP_TIME)
        # self.clean()

    def turn_left(self, vitess: int):
        self.EN_LEFT_PWM.ChangeDutyCycle(100-vitess)
        self.EN_RIGHT_PWM.ChangeDutyCycle(vitess)
        gpio.output(self.PINS.IN1, True)
        gpio.output(self.PINS.IN2, False)
        gpio.output(self.PINS.IN3, True)
        gpio.output(self.PINS.IN4, False)
        sleep(self.SLEEP_TIME)
        # self.clean()

    def stop(self):
        self.EN_LEFT_PWM.ChangeDutyCycle(0)
        self.EN_RIGHT_PWM.ChangeDutyCycle(0)
        gpio.output(self.PINS.IN1, False)
        gpio.output(self.PINS.IN2, False)
        gpio.output(self.PINS.IN3, False)
        gpio.output(self.PINS.IN4, False)
        self.clean()

    async def receive(self, text_data):
        text_data_json = json.loads(text_data)
        vitess = 0
        print("decision ", text_data_json['decision'], "\v")
        decision = text_data_json['decision']
        if decision == self.IDLE:
            self.handelCommand(command=self.IDLE, vitess=vitess)
        else:
            vitess = int(text_data_json['vitess'])
            self.handelCommand(command=decision, vitess=vitess)
