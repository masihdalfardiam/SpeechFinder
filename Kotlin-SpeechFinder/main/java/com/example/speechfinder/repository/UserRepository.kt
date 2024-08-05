package com.example.speechfinder.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.speechfinder.data.Data
import com.example.speechfinder.data.LoginDetails
import com.example.speechfinder.data.LoginResponse
import com.example.speechfinder.data.User
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class UserRepository {
    private val client: OkHttpClient

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val BASE_URL = "https://api.speechfinder.ir"

    fun registerUser(user: User): LiveData<Result<Boolean>> {
        val result = MutableLiveData<Result<Boolean>>()
        val mediaType = "application/json".toMediaType()
        val json = Gson().toJson(user)
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$BASE_URL/auth/register/")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                result.postValue(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    result.postValue(Result.success(true))
                } else {
                    result.postValue(Result.failure(Exception("Registration failed")))
                }
            }
        })

        return result
    }


    fun loginUser(loginDetails: LoginDetails): LiveData<Result<Data>> {
        val result = MutableLiveData<Result<Data>>()
        val mediaType = "application/json".toMediaType()
        val json = Gson().toJson(loginDetails)
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$BASE_URL/auth/login/")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                result.postValue(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val loginResponse = Gson().fromJson(responseBody, LoginResponse::class.java)
                        if (loginResponse.ok && loginResponse.data != null) {
                            result.postValue(Result.success(loginResponse.data))
                        } else {
                            result.postValue(Result.failure(Exception("Login failed: ${loginResponse.message}")))
                        }
                    } else {
                        result.postValue(Result.failure(Exception("Empty response body")))
                    }
                } else {
                    result.postValue(Result.failure(Exception("Login failed")))
                }
            }
        })

        return result
    }


    fun verifyEmail(code: String, token: String): LiveData<Result<Boolean>> {
        val result = MutableLiveData<Result<Boolean>>()
        val mediaType = "application/json".toMediaType()
        val json = "{\"code\": \"$code\"}"
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$BASE_URL/auth/verify-email")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Token $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                result.postValue(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    result.postValue(Result.success(true))
                } else {
                    result.postValue(Result.failure(Exception("Email Verification failed")))
                }
            }
        })

        return result
    }
}
