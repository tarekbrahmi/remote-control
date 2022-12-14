from channels.generic.websocket import AsyncWebsocketConsumer
import json
from time import sleep
import RPi.GPIO as gpio


class PINS:
    ######## PINS ########
    def __init__(self) -> None:
        print("set up the GPIO")
    ENA = 0
    ENB = 0
    IN1 = 17
    IN2 = 22
    IN3 = 23
    IN4 = 24


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

        gpio.setup(self.PINS.IN1, gpio.OUT)
        gpio.setup(self.PINS.IN2, gpio.OUT)
        gpio.setup(self.PINS.IN3, gpio.OUT)
        gpio.setup(self.PINS.IN4, gpio.OUT)

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

    async def handelCommand(self, command, vitess=0):
        if str(command) == self.DECISION.FORWARD:
            self.forward(vitess=vitess)
        if str(command) == self.DECISION.BACKWARD:
            self.backward(vitess=vitess)
        if str(command) == self.DECISION.LEFT:
            self.turn_left(vitess=vitess)
        if str(command) == self.DECISION.RIGHT:
            self.turn_right(vitess=vitess)
        if str(command) == self.DECISION.IDLE:
            self.stop()

    def forward(self, vitess: int):
        gpio.output(self.PINS.IN1, False)
        gpio.output(self.PINS.IN2, True)
        gpio.output(self.PINS.IN3, True)
        gpio.output(self.PINS.IN4, False)
        sleep(self.SLEEP_TIME)
        self.clean()

    def backward(self, vitess: int):
        gpio.output(self.PINS.IN1, True)
        gpio.output(self.PINS.IN2, False)
        gpio.output(self.PINS.IN3, False)
        gpio.output(self.PINS.IN4, True)
        sleep(self.SLEEP_TIME)
        # self.clean()

    def turn_right(self, vitess: int):
        gpio.output(self.PINS.IN1, False)
        gpio.output(self.PINS.IN2, True)
        gpio.output(self.PINS.IN3, False)
        gpio.output(self.PINS.IN4, True)
        sleep(self.SLEEP_TIME)
        # self.clean()

    def turn_left(self, vitess: int):
        gpio.output(self.PINS.IN1, True)
        gpio.output(self.PINS.IN2, False)
        gpio.output(self.PINS.IN3, True)
        gpio.output(self.PINS.IN4, False)
        sleep(self.SLEEP_TIME)
        # self.clean()

    def stop(self):
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
