from rest_framework import generics, permissions, status
from rest_framework.response import Response
from rest_framework.authtoken.models import Token
from rest_framework.serializers import ValidationError
from django.contrib.auth import authenticate
from django.db import IntegrityError
from core.models import User
from core.models import Code
from .serializers import UserSerializer
from core.utils.send_email import send_email
from core.utils.voice_to_text import voice_to_text
import random




class CreateUserView(generics.CreateAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = [permissions.AllowAny]

    def post(self, request, *args, **kwargs):
        try:
            response = self.create(request, *args, **kwargs)
            email = request.data.get("email")
            password = request.data.get("password")

            user = authenticate(request, username=email, password=password)
            token, created = Token.objects.get_or_create(user=user)
            response_data = {
                "ok": True,
                "message": "Register success",
                "data": {"token": token.key, "status": user.status},
                "errors": None,
            }

            code = Code.objects.create(user=user)
            send_email(user.email, "تایید ایمیل", f"کد تایید ایمیل شما: {code.code}")

            return Response(response_data, status=status.HTTP_201_CREATED)

        except ValidationError as e:
            response_data = {
                "ok": False,
                "message": "Registration failed",
                "errors": e.detail,
                "data": None,
            }
            return Response(response_data, status=status.HTTP_400_BAD_REQUEST)

        except IntegrityError as e:
            response_data = {
                "ok": False,
                "message": "Register failed",
                "data": None,
                "errors": "Token creation failed",
            }
            return Response(response_data, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class UserLoginView(generics.CreateAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = [permissions.AllowAny]

    def post(self, request, *args, **kwargs):
        email = request.data.get("email")
        password = request.data.get("password")

        user = authenticate(request, username=email, password=password)
        if user:
            token, created = Token.objects.get_or_create(user=user)
            response_data = {
                "ok": True,
                "message": "Login success",
                "data": {"token": token.key, "status": user.status},
                "errors": None,
            }
            if user.status == 0:
                code = Code.objects.get_or_create(user=user)
                if not "code" in code:
                    code = code[0]
                send_email(user.email, "تایید ایمیل", f"کد تایید ایمیل شما: {code}")
            return Response(response_data, status=status.HTTP_200_OK)
        else:
            response_data = {
                "ok": False,
                "message": "Login failed",
                "data": None,
                "errors": "Invalid credentials",
            }
            return Response(response_data, status=status.HTTP_401_UNAUTHORIZED)


class UserEmailVerificationView(generics.CreateAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer

    def post(self, request, *args, **kwargs):
        sent_code = request.data.get("code")
        code = Code.objects.filter(user=request.user, code=sent_code)

        if code:
            response_data = {
                "ok": True,
                "message": "Verification Successful",
                "data": None,
                "errors": None,
            }
            user: User = request.user
            user.status = 1
            user.save()
            code.delete()
            send_email(
                user.email,
                "ایمیل شما تایید شد",
                f"{user.first_name} عزیز! ایمیل شما با موفقیت تایید شد.",
            )
            return Response(response_data, status=status.HTTP_204_NO_CONTENT)
        else:
            response_data = {
                "ok": False,
                "message": "Verification Failed",
                "data": None,
                "errors": "Invalid Code.",
            }
            return Response(response_data, status=status.HTTP_404_NOT_FOUND)


class AudioSender(generics.CreateAPIView):
    def post(self, request, *args, **kwargs):
        response_data = {
            "ok": True,
            "message": "Audio Sender",
            "data": voice_to_text(),
            "errors": None,
        }
        return Response(response_data, status=status.HTTP_200_OK)


class UserWords(generics.CreateAPIView):
    def post(self, request, *args, **kwargs):
        response_data = {
            "ok": True,
            "message": "User Words",
            "data": ["book", "test", "week", "random"],
            "errors": None,
        }
        return Response(response_data, status=status.HTTP_200_OK)


class AudioTimes(generics.CreateAPIView):
    def post(self, request, *args, **kwargs):
        hours = random.randint(0, 23)
        minutes = random.randint(0, 59)
        response_data = {
            "ok": True,
            "message": "Audio Times",
            "data": f"{hours:02}:{minutes:02}",
            "errors": None,
        }
        return Response(response_data, status=status.HTTP_200_OK)
