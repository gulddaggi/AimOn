from django.urls import path
from .views import AskView, HealthView

urlpatterns = [
    path("ask", AskView.as_view(), name="ask"),
    path("health", HealthView.as_view(), name="health"),
]
