from django.urls import re_path
from .views import (
    UserLoginView,
    CreateUserView,
    UserEmailVerificationView,
    AudioSender, UserWords, AudioTimes
)




urlpatterns = [
    re_path(r"^auth/register/?$", CreateUserView.as_view(), name="user-register"),
    re_path(r"^auth/login/?$", UserLoginView.as_view(), name="user-login"),
    re_path(r"^auth/verify-email/?$", UserEmailVerificationView.as_view(), name="user-email-verification"),
    re_path(r"^audio_sender/?$", AudioSender.as_view(), name="audio-sender"),
    re_path(r"^user_words/?$", UserWords.as_view(), name="user-words"),
    re_path(r"^audio_times/?$", AudioTimes.as_view(), name="audio-times"),
]
