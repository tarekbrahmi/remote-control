from channels.generic.websocket import AsyncWebsocketConsumer
import json

class CommandConsumer(AsyncWebsocketConsumer):
    """_summary_

    Args:
        AsyncWebsocketConsumer (_type_): _description_
    """
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
            text_data = json.dumps({
                'data': event['data']
            }))
    async def receive(self, text_data):
        text_data_json=json.loads(text_data)
        print("Received data : ", text_data_json)
