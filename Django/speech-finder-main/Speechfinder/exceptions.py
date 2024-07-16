from rest_framework.views import exception_handler
from rest_framework.response import Response
from rest_framework import status

def custom_exception_handler(exc, context):
    response = exception_handler(exc, context)

    if response is not None:
        error_message = str(exc.detail) if hasattr(exc, 'detail') else str(exc)
        custom_response_data = {
            "ok": False,
            "message": error_message,
            "data": None,
            "errors": response.data
        }
        response.data = custom_response_data

    return response
