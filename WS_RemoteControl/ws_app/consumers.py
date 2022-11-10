from channels.generic.websocket import AsyncWebsocketConsumer


class CommandConsumer(AsyncWebsocketConsumer):
    async def connect(self):
        self.group_name = "Command_"
        self.groups.append('Command_GROUP_')

        await self.channel_layer.group_add("Command_", self.channel_name)

        await self.accept()
