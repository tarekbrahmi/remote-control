from channels.generic.websocket import AsyncWebsocketConsumer
import json
import RPi.GPIO as gpio
from time import sleep

class PINS:
    ######## PINS ########
    EN_LEFT = 2
    IN1 = 3
    IN2 = 4

    EN_RIGHT = 17
    IN3 = 27
    IN4 = 22


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
    SLEEP_TIME = .7

    def clean(self):
        gpio.cleanup()

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        gpio.setmode(gpio.BCM)
        gpio.setwarnings(False)
        gpio.setup(self.PINS.IN1, gpio.OUT)
        gpio.setup(self.PINS.IN2, gpio.OUT)
        gpio.setup(self.PINS.IN3, gpio.OUT)
        gpio.setup(self.PINS.IN4, gpio.OUT)
        gpio.setup(self.PINS.EN_LEFT, gpio.OUT)
        gpio.setup(self.PINS.EN_RIGHT, gpio.OUT)
        # GPIO.PWM(channel, frequence)
        self.EN_RIGHT_PWM = gpio.PWM(self.PINS.EN_RIGHT, 100)
        # ici, rapport_cyclique vaut entre 0.0 et 100.0
        self.EN_RIGHT_PWM.start(0)
        # gpio.output(self.PINS.EN_RIGHT, gpio.HIGH)
        self.EN_LEFT_PWM = gpio.PWM(self.PINS.EN_LEFT, 100)
        self.EN_LEFT_PWM.start(0)
        # gpio.output(self.PINS.EN_LEFT, gpio.HIGH)
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

    async def disconnect(self, close_code):
        self.clean()
        await super().disconnect(close_code)

    async def handelCommand(self, command, vitess=100, exec=False):
        print('commande %s' % str(command))
        if str(command) == self.DECISION.FORWARD and not exec:
            # TODO add the  ChangeDutyCycle to vitess-->DONE
            self.EN_LEFT_PWM.ChangeDutyCycle(vitess)
            self.EN_RIGHT_PWM.ChangeDutyCycle(vitess)
            gpio.output(self.PINS.IN1, gpio.HIGH)
            gpio.output(self.PINS.IN2, gpio.LOW)
            gpio.output(self.PINS.IN3, gpio.HIGH)
            gpio.output(self.PINS.IN4, gpio.LOW)
            sleep(self.SLEEP_TIME)
        if str(command) == self.DECISION.BACKWARD and not exec:
            self.EN_LEFT_PWM.ChangeDutyCycle(vitess)
            self.EN_RIGHT_PWM.ChangeDutyCycle(vitess)
            gpio.output(self.PINS.IN1, gpio.LOW)
            gpio.output(self.PINS.IN2, gpio.HIGH)
            gpio.output(self.PINS.IN3, gpio.LOW)
            gpio.output(self.PINS.IN4, gpio.HIGH)
            sleep(self.SLEEP_TIME)
        if str(command) == self.DECISION.LEFT and not exec:
            self.EN_LEFT_PWM.ChangeDutyCycle(vitess)
            self.EN_RIGHT_PWM.ChangeDutyCycle(80)
            gpio.output(self.PINS.IN1, gpio.HIGH)
            gpio.output(self.PINS.IN2, gpio.LOW)
            gpio.output(self.PINS.IN3, gpio.HIGH)
            gpio.output(self.PINS.IN4, gpio.LOW)
            sleep(self.SLEEP_TIME)
        if str(command) == self.DECISION.RIGHT and not exec:
            self.EN_LEFT_PWM.ChangeDutyCycle(vitess)
            self.EN_RIGHT_PWM.ChangeDutyCycle(80)
            gpio.output(self.PINS.IN1, gpio.HIGH)
            gpio.output(self.PINS.IN2, gpio.LOW)
            gpio.output(self.PINS.IN3, gpio.HIGH)
            gpio.output(self.PINS.IN4, gpio.LOW)
            sleep(self.SLEEP_TIME)
        if str(command) == self.DECISION.IDLE and not exec:
            self.stop()
        ###############################################


    def stop(self):
        self.EN_LEFT_PWM.ChangeDutyCycle(0)
        self.EN_RIGHT_PWM.ChangeDutyCycle(0)
        gpio.output(self.PINS.IN1, gpio.LOW)
        gpio.output(self.PINS.IN2, gpio.LOW)
        gpio.output(self.PINS.IN3, gpio.LOW)
        gpio.output(self.PINS.IN4, gpio.LOW)
        return 0

    async def receive(self, text_data):
        text_data_json = json.loads(text_data)
        vitess = 0
        if "decision" in text_data_json:
            decision = text_data_json['decision']
            if decision == self.DECISION.IDLE:
                await self.handelCommand(command=self.DECISION.IDLE, vitess=vitess, exec=False)
            else:
                vitess = int(text_data_json['vitess'])
                await self.handelCommand(command=decision, vitess=vitess, exec=False)
