import random
import uuid
from django.contrib.auth.models import (
    AbstractBaseUser,
    BaseUserManager,
    PermissionsMixin,
    Group,
    Permission,
)
from django.db import models
from django.utils import timezone


class CustomUserManager(BaseUserManager):  # for app Oauth
    def create_user(self, email, first_name, last_name, password=None):
        if not email:
            raise ValueError("The Phone number field must be set")
        user = self.model(
            email=email,
            first_name=first_name,
            last_name=last_name,
        )
        user.set_password(password)
        user.save(using=self._db)
        return user

    def create_superuser(self, email, first_name, last_name, password=None):
        user = self.create_user(
            email=email,
            first_name=first_name,
            last_name=last_name,
            password=password,
        )
        user.is_staff = True
        user.is_superuser = True
        user.save(using=self._db)
        return user


class User(AbstractBaseUser, PermissionsMixin):
    id = models.AutoField(primary_key=True)
    uuid = models.UUIDField(
        default=uuid.uuid4, blank=False, editable=False, max_length=36, unique=True
    )
    first_name = models.CharField(max_length=30)
    last_name = models.CharField(max_length=30)
    email = models.EmailField(unique=True)
    status = models.IntegerField(default=0)
    is_active = models.BooleanField(default=True)
    is_staff = models.BooleanField(default=False)
    date_joined = models.DateTimeField(default=timezone.now)
    last_login = models.DateTimeField(null=True, blank=True)

    objects = CustomUserManager()

    USERNAME_FIELD = "email"
    REQUIRED_FIELDS = ["first_name", "last_name"]

    def __str__(self):
        return self.email

    class Meta:
        permissions = [("can_add_stuff", "Can add stuff")]
        verbose_name = "User"
        verbose_name_plural = "Users"
        db_table = "users"

    groups = models.ManyToManyField(Group, related_name="custom_user_set", blank=True)
    user_permissions = models.ManyToManyField(
        Permission, related_name="custom_user_set", blank=True
    )


class Sound(AbstractBaseUser, PermissionsMixin):
    id = models.AutoField(primary_key=True)
    user = models.ForeignKey(User, on_delete=models.DO_NOTHING)
    user_words = models.TextField(blank=True)
    text = models.TextField(blank=True)
    created_at = models.DateTimeField(default=timezone.now)

    def __str__(self):
        return self.text

    class Meta:
        db_table = "sounds"


class Code(AbstractBaseUser, PermissionsMixin):
    id = models.AutoField(primary_key=True)
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    code = models.CharField(max_length=6, blank=True)
    created_at = models.DateTimeField(default=timezone.now)

    groups = models.ManyToManyField(Group, related_name="code_groups", blank=True)
    user_permissions = models.ManyToManyField(
        Permission, related_name="code_user_permissions", blank=True
    )

    def save(self, *args, **kwargs):
        if not self.code:
            self.code = "{:06d}".format(random.randint(0, 999999))
        super().save(*args, **kwargs)

    def __str__(self):
        return self.code

    class Meta:
        db_table = "verification_codes"
