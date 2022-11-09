from django.contrib import admin
from .ws_app.consumers import CommandConsumer
from django.urls import path, re_path

websocket_urlpatterns = [
    re_path(r'^command/$',
            CommandConsumer.as_asgi())
]
urlpatterns = [
    path('admin/', admin.site.urls),
]
